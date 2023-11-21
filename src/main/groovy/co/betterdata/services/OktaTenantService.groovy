package co.betterdata.services

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.exceptions.HttpException
import jakarta.inject.Singleton
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject

@Singleton
public class OktaTenantService extends  Okta{

    def listTenants(after){
        try{
            def url = "/idps?limit=30"
            def str = 'Hello, world!'
            def parts = str.split(',')
            println parts // prints ['Hello', ' world!']
            if (after) url += "&after=${after}"
            def res = get(url)
            if (res.status == 200 && res.data.size() > 0){
                return [
                        status: 200,
                        data: res.data.filter{idp -> idp.name.startWith(dac_prefix)}.map{idp ->  [
                                id: idp.id,
                                name: (idp.name.split as String).split(dac_prefix)[1].split("_")[0],
                                created: idp.created
                        ]},
                        headers: res.headers
                ]
            }
        }catch (e){
            println(e)
            throw e
        }
    }

    def searchTenants(search){
        try{
            def res = get("/groups?q=ADMINS_${search}")
            if (res.data.size() == 0){
                return [
                        status: 200,
                        data: []
                ]
            }
            return [
                    status: res.status,
                    data: res.data.map{grp -> [
                            id: grp.profile.description.tenantId,
                            name: (grp.profile.name as String).split('ADMINS_')[1],
                            ADMINS_groupId: grp.id,
                            created: grp.created
                    ]}
            ]
        }catch(e){
            println(e)
            throw  e
        }
    }

    def  list(after, search){
        def res
        if (after){
            res = listTenants(after)
        }else  if(search){
            res = searchTenants(search)
        }else{
            res = listTenants(null)
        }
        return [
                status: res.status,
                data: res.data
        ]

    }

    def addTenantAdmin(tenantName, userId){
        try{
            def pre = getAdminGroup(tenantName)
            def grps = get("/users/${userId}/groups")

            def filtered = grps.data.findAll{(it.profile.name as String).startsWith("USERS_${tenantName}")}
            if (filtered.size() <=0){
                throw IOException("error")
            }
            def res = put("/groups/${pre.data.id}/users/${userId}")
            return [
                    status: res.status,
                    data: [
                            tenant: tenantName,
                            userId: pre.data.id,
                            assigned: res.data.lastUpdated
                    ]
            ]
        }catch (e){
            println(e)
            throw  e
        }
    }

    def adminAssign(tenantName, userId){
        def res = addTenantAdmin(tenantName, userId)
        return [
                status: res.status,
                data: res.data
        ]
    }

    def getTenantAdmins(tenant){
        try{
            def pre = getAdminGroup(tenant)
            def res = get("/groups/${pre.data.id}/users")
            return [
                    status: res.status,
                    data: res.data.map{grp -> {
                        grp.remove("_links")
                        grp.remove("type")
                        grp.profile.remove("mobilePhone")
                        grp.profile.remove("secondEmail")
                        grp.profile.remove("login")
                        return grp
                    }}
            ]
        }catch (e){
            println(e)
            throw e
        }
    }

    def admins_list(tenant){
        def res = getTenantAdmins(tenant)
        return [
                status: res.status,
                data: res.data
        ]
    }

    def unassignTenantApp(tenant, appId){
        try{
            def grp = get("/groups?q=APPUSERS_${tenant}")
            if (grp.status == 200 && grp.data.size() == 1){
                def res=delete("/groups/${grp.data[0 as String].id}")
                return res
            }else{
                throw io.micronaut.http.exceptions.HttpException("group search returned non unique result")
            }
        }catch (e){
            println(e)
            throw e
        }
    }

    def apps_deactivate(tenant, appId){
        def res = unassignTenantApp(tenant, appId)
        return [
                status: res.status,
                data: res.data
        ]
    }

    def filterAppsById(ids, allUsersApps){
        try{
            def res = get("/apps?limit=100")
            def filtered = []
            res.data.each {app -> {
                if(ids[app.id]){
                    app.groupId = ids[app.id]
                    filtered.push(app)
                }
            }}
            return [
                    status: res.status,
                    data: filtered.map{app -> [
                            id: app.id,
                            APPUSERS_groupId: app.groupId,
                            name: app.label.split(lib.DAC_PREFIX)[1],
                            created: app.created,
                            lastUpdated: app.lastUpdated,
                            logo: app._links.logo,
                            settings: allUsersApps.includes(app.id) ? { allUsers: true } : { allUsers: false }
                    ]}
            ]
        }catch(e){
            println(e)
            throw  e
        }
    }

    def  getTenantApps(tenant, claims){
        getAdminGroup(tenant)
        def allUserApps = []
        def tenants = []
        if (claims.tenans){
            tenants = claims.tenants
        }
        tenants.each{t -> {
            def userGroupId = t.toString().split(":")[2]
            def res = get("/groups/${userGroupId}/apps")
            allUserApps = allUserApps + res.data.map{app -> app.id}
        }}

        def res = get("/groups?q=APPUSERS_${tenant}&expand=stats")
        def filtered = res.data.findAll(grp -> grp._embedded.stats.appsCount > 0)
        def ids = [:]
        filtered.map{grp -> {
            ids.put((grp.profile.name as String).split("_")[2], grp.id)
        }}
        return filterAppsById(ids, allUserApps)
    }

    def apps_list(tenant, authorizer){
        def res = getTenantApps(tenant, authorizer)
        return [
                status: res.status,
                data: res.data
        ]
    }

    def assignTenantApp(tenant, appId){
        def res
        try{
            get("/apps/${appId}")

            def pre = get("/groups?q=QPPUSERS_${tenant}_${appId}")
            if (pre.data.size() > 1){
                throw HttpException("group search returned non unique result")
            } else if( pre.data.size() == 0){
                res = post("/groups", [
                        profile: [
                                name: "APPUSERS_${tenant}_${appId}"
                        ]
                ])
            } else{
                res = [
                        status: pre.status,
                        data: pre.data[0 as String]
                ]
            }
        }catch (e){
            println(e)
            throw e
        }
        try{
            def _final = put("/apps/${appId}/groups/${res.data.id}")
            def tnt = getAdminGroup(tenant)
            def roles = get("/groups/${tnt.data.id}/roles")
            def role = roles.data.findAll{role -> {
                return role.type == "USER_ADMIN"
            }}
            addGroupAdminTarget(tnt.data.id, role[0].id, res.data.id)
            return [
                    status: _final.status,
                    data: [
                            tenant: tenant,
                            appId: appId,
                            lastUpdated: res.data.lastUpdated
                    ]
            ]
        }catch(e){
            throw e
        }
    }

    def assignGroupToApp(appId, tenant){
        try{
            def userGroup = getUserGroup(tenant)
            def res = put("/apps/${appId}/groups/${userGroup.data.id}")
            return [
                    status: res.status,
                    data: [
                            tenant: tenant,
                            appId: appId,
                            lastUpdated: res.data.lastUpdated
                    ]
            ]
        }catch(e){
            throw e
        }
    }

    def unassignGroupFromApp(appId, tenant) {
        try {
            def usersGroup = getUsersGroup(tenant);

            def res = delete(
                    '/apps/' + appId + '/groups/' + usersGroup.data.id
            );
            return [
                    status: 200,
                    data  : [
                            tenant     : tenant,
                            appId      : appId,
                            lastUpdated: res.data.lastUpdated
                    ]
            ];
        } catch (e) {
            throw e;
        }
    }

    def apps_post(allUsers, tenant, appId){
        def res
        try{
            if (allUsers && allUsers == 'true') {
                res = assignGroupToApp(
                        appId,
                        tenant
                );
            } else if (allUsers && allUsers == 'false') {
                res = unassignGroupFromApp(
                        appId,
                        tenant
                );
            }
            if (!res) {
                res = assignTenantApp(tenant, appId);
            }
            return [
                    status: res.status,
                    data: res.data
            ]
        }catch(e){
            throw e
        }
    }

    def addTenantDomainCheckDupAndFilter(ruleId, domain) {

        def url = "/policies/${idp_disco_policy_id}/rules"
        def res = get(url)
        def filteredRule;
        res.data.each {rule -> {
            if (ruleId && rule.id == ruleId) filteredRule = rule;

            if(rule.status == 'ACTIVE'){
                rule.condition.userIdentifier.patterns.each{pattern -> {
                    if (pattern.matchType == 'SUFFIX' && domain == pattern.value){
                        throw HttpException("Requested Domain already exists")
                    }
                }}
            }
        }}
        return filteredRule
    }

    def addTenantDomain(tenant, requestPayload, obj) {
        try {
            def grp = obj
            if (!grp) grp = getAdminGroup(tenant)

            def domain;
            def meta = grp.data.profile.description
            def whichId;
            def uid
            def activate
            if(requestPayload.verified){
                whichId = 'routingRuleId'
                activate = '?activate=true'
                domain = requestPayload.domain
            }else{
                whichId = 'inactiveRoutingRuleId';
                activate = '?activate=false';
                uid = uuid();
                domain = uid + '.' + requestPayload.domain;
            }

            if (meta[whichId]){
                def payload = addTenantDomainCheckDupAndFilter(meta[whichId], requestPayload.domain)
                payload.remove("_links")
                payload.remove("id")
                payload.remove("created")
                payload.remove("lastUpdated")
                payload.conditions.userIdentifier.patterns.push([
                        matchType: 'SUFFIX',
                        value: domain
                ])
                put("/policies/${idp_disco_policy_id}/rules/${meta[whichId]}", payload)
            }else{
                addTenantDomainCheckDupAndFilter(null, requestPayload.domain)
                def rule = post("/policies/${idp_disco_policy_id}/rules/${activate}", [
                        type: "IDP_DISCOVERY",
                        name: dac_prefix + tenant + (requestPayload.verified ? "" : "_unverified"),
                        actions: [
                            idp: [
                                providers: [[
                                                type: "SAML2",
                                                id: meta.tenantId
                                            ]]
                            ]
                        ],
                        conditions: [
                            userIdentifier: [
                                patterns: [[
                                               matchType: "SUFFIX",
                                               value: domain
                                           ]],
                                type: "IDENTIFIER"
                            ]
                        ]
                ])
                def payload2 = grp.data
                payload2.remove("created")
                payload2.remove("lastUpdated")
                payload2.remove("lastMembershipUpdated")
                def desc = payload2.profile.description
                desc[whichId] = rule.data.id
                payload2.profile.description = desc
                put("/groups/${payload2.id}", payload2)
            }
            return [
                    status: 201,
                    data: [
                            domain: requestPayload.domain,
                            dnsVerificationString: uid
                    ]
            ]
        }catch(e){
            throw e
        }
    }

    def domains_add(tenant, body){
        try {
            def res = addTenantDomain(tenant, strToMap(body), null)
            return [
                    status: res.status,
                    data: res.data
            ]
        }catch(e){
            throw e
        }
    }

    def deleteTenantDomainHelper(domain, routingRuleId) {
        try{
            def url = '/policies/' + idp_disco_policy_id + '/rules/' + routingRuleId
            def rule = get(url)
            def payload = rule.data
            def before = payload.conditions.userIdentifier.patterns
            def domains = payload.conditions.userIdentifier.patterns.findAll{pattern -> {
                if(pattern.matchtype != 'SUFFIX')
                    return true
                else{
                    def test = payload.status == 'ACTIVE'? pattern.value: pattern.value.substring(pattern.value.indexOf('.') + 1);
                    return test != domain;
                }
            }}
            if (domains.size() > 0){
                payload.conditions.userIdentifier.patterns = domains
                payload.remove("id")
                payload.remove("_links")
                payload.remove("created")
                payload.remove("lastUpdated")
                put(url, payload)
            }else{
                delete("/policies/${idp_disco_policy_id}/rules/${routingRuleId}")
            }
            return [
                    domains: domain,
                    status: before.size() != domains.size() ? 204:404
            ]
        }catch (e){
            throw e
        }

    }

    def deleteTenantDomain(tenant, domain, obj) {
        try{
            def grp = obj
            if(!grp) grp = getAdminGroup(tenant)
            def meta = grp.data.profile.desription

            def res;
            def whichId;
            if (meta.routingRuleId){
                res = deleteTenantDomainHelper(domain, meta.routingRuleId)
                if (res.status == 204){
                    whichId = "routingRuleId"
                }else if (meta.inactiveRoutingRuleId){
                    res = deleteTenantDomainHelper(domain, meta.inactiveRoutingRuleId)
                    if (res.status === 204) whichId = 'inactiveRoutingRuleId';
                    else throw HttpException("Not Found")
                } else{
                    throw HttpException("Not Found")
                }
            } else if (meta.inactiveRoutingRuleId) {
                res = deleteTenantDomainHelper(domain, meta.inactiveRoutingRuleId)
                if (res.status == 204) whichId= 'inactiveRoutingRuleId'
                else throw HttpException("Not Found")
            }else{
                throw HttpException("Not Found")
            }

            if(res.domains.size() == 0){
                def payload = grp.data
                payload.remove("created")
                payload.remove("lastUpdated")
                payload.remove("lastMembershipUpdated")
                def desc = payload.profile.description
                desc.remove(whichId)
                payload.profile.description = desc
                put("/groups/${payload.id}", payload)
            }

            return [
                    status: 204
            ]
        }catch(e){
            throw e
        }
    }

    def domains_delete(tenant, domain){
        def res = deleteTenantDomain(tenant, domain, null)
        return [
                status: res.status,
                data: res.data
        ]
    }

    def getTenantDomainsHelper(routingRuleId) {
        if (!routingRuleId) return [];
        try{
            def res = get("/policies/${idp_disco_policy_id}/rules/${routingRuleId}")
            def domains = res.data.conditions.userIdentifier.patterns.findAll{domain -> {
                return domain.matchType == 'SUFFIX'
            }}.map{domain -> {
                return res.data.status == "ACTIVE"? [
                        domain: domain.value,
                        verified: true
                ]: [
                        domain: domain.value.substring(domain.value.indexOf('.') + 1),
                        verified: false,
                        dnsVerificationString: domain.value.split('.' as Closure)[0]
                ]
            }}
            return domains
        }catch(e){
            if (e.response.status == 404){
                return []
            }
            throw e
        }
    }

    def getTenantDomains(tenant, verified) {
        try{
            def pre = getAdminGroup(tenant)
            def meta = pre.data.profile.description
            if (verified == null){
                def unverifiedDomains = getTenantDomainsHelper(meta.inactiveRoutingRuleId)
                def allDomains = getTenantDomainsHelper(meta.inactiveRoutingRuleId)
                return [
                        status: 200,
                        data: allDomains + unverifiedDomains
                ]
            } else if (verified){
                return [
                        status: 200,
                        data: getTenantDomainsHelper((meta.routingRuleId))
                ]
            }else {
                return [
                        status: 200,
                        data: getTenantDomainsHelper(meta.inactiveRoutingRuleId)
                ]
            }
        }catch(e){
            throw e
        }
    }

    def domains_list(tenant, verified){
        try{
            def res
            if (verified) {
                if (verified == 'false'){
                    res = getTenantDomains(tenant, false)
                }else if(verified == 'true'){
                    res = getTenantDomains(tenant, true)
                }else{
                    res = getTenantDomains(tenant, null)
                }
            } else {
                res = getTenantDomains(tenant, null)
            }
            return res
        }catch(e){
            throw e
        }
    }

    def verifyDomain(tenant, domain, dnsVerificationString) {
        try{
            def grp = getAdminGroup(tenant)
            def meta = grp.data.profile.description
            if(meta.inactiveRoutingRuleId) {
                def dnsLookup = get_general("https://dns.google/resolve?name=${dns_verify_prefix}.${domain}&type=16")
                println("DNS Lookup ${mapToStr(dnsLookup.data)}")

                def verified = false
                if (dnsLookup.data.Answer) {
                    dnsLookup.data.Answer.each { dns ->
                        {
                            if (dns.data == dnsVerificationString) verified = true
                        }
                    }
                }
                if (verified) {
                    deleteTenantDomain(tenant, domain, grp)
                    addTenantDomain(tenant, [
                            domain  : domain,
                            verified: true
                    ], grp)
                    return [
                            status: 200,
                            data  : [
                                    verified: true,
                                    domain  : domain
                            ]
                    ]
                } else {
                    return [
                            status: 200,
                            data  : [
                                    verified: false,
                                    domain  : domain
                            ]
                    ]
                }
            }else{
                throw HttpException("not found")
            }
        }catch (e){
            throw e
        }
    }

    def domains_verify(tenant, domain, dnsVerificationString){
        def res = verifyDomain(tenant, domain, dnsVerificationString)
        return res
    }

    def addGroup(name, tenantId){
        try{
            def res = post('/groups', [
                    profile: [
                            name: name,
                            description: mapToStr([tenantId: tenantId])
                    ]
            ])
            return res
        }catch (e){
            println(e)
            throw  e
        }
    }

    def add(name){
        if (name == null || name.isEmpty()){
            throw  new RuntimeException("name can't be null")
        }
        def res = getAdminGroup(name)
        println(res)
        if ( res["status"] == 200){
            return [
                    status: 400,
                    data: [
                            message: "tenant ${name} is already exists"
                    ]
            ]
        }
        def kid = addKey(this.x5c)
        println("kid = ${kid}")
//        res = post('/idps', mapToStr([
//              type: "SAML2",
//                name: this.dac_prefix + name,
//                status: "INACTIVE",
//                protocal: [
//                        type: "SAML2",
//                        endpoints: [
//                                sso: [
//                                        url: "https://idp.example.com",
//                                        binding: "HTTP-POST",
//                                        destination: "https://idp.example.com"
//                                ],
//                                acs: [
//                                        binding: "HTTP-POST",
//                                        type: "INSTANCE"
//                                ]
//                        ],
//                        algorithms: [],
//                        credentials: [
//                                trust: [
//                                        issuer: "https://idp.example.com",
//                                        kid: kid
//                                ]
//                        ]
//                ],
//                policy: [
//                        provisioning: [
//                                action: "AUTO",
//                                groups: [
//                                        action: "NONE"
//                                ]
//                        ],
//                        subject: [
//                                userNameTemplate: [
//                                        template: "idpuser.subjectNameId"
//                                ],
//                                matchType: "USERNAME"
//                        ],
//                        maxClockSkew: 120000
//                ]
//        ]))
        println('------')
        def allUsers = addGroup("USERS_${name}", name)
        def admins = addGroup("ADMINS_${name}", name)
        println(allUsers)
        println(admins)
        def role = addUserAdminRole(admins.data.id)
        println(role)
        addGroupAdminTarget(admins.data.id, role.data.id, allUsers.data.id)
        println("----2")
        addGroupAdminTarget(admins.data.id, role.data.id, admins.data.id)
        println("----3")
        assignGroupToApp(admins.data.id, res.data.id, name, allUsers.data.id)
        println("----4")
        updateTenantJitGroupAndInactivate(res.data.id, allUsers.data.id)
        println(res)
        return [
                status: 201,
                data: [
                        id: res.data.id,
                        ADMINS_groupId: admins.data.id,
                        USERS_groupId: allUsers.data.id,
                        ADMINS_roleId: role.data.id,
                        name: name,
                        created: res.data.created,
                        lastUpdated: res.data.lastUpdated
                ]
        ]
    }

    def assignGroupToApp(groupId, tenantId, tenantName, usersGroupId) {
        try {
            def res = put("/apps/${appClientId}/groups/${groupId}", [
                    profile: [
                            tenants: ["${tenantId}:${tenantName}:${usersGroupId}"]
                    ]
            ])
            return  res
        }catch (e){
            println(e)
            throw e
        }
    }

    def updateTenantJitGroupAndInactivate(id, groupId) {
        try {
            def res = get("/idps/${id}")
            def payload = res.data
            payload.remove("_links")
            payload.remove("id")
            payload.remove("created")
            payload.remove("laspUpdated")
            payload.policy.provisioning.groups.action = "ASSIGN"
            payload.policy.provisioning.groups.assignments = [groupId]
            payload.status = "INACTIVE"
            def res2 = put("/idps/${id}", payload)
            return res2
        }catch(e){
            println(e)
            throw  e
        }
    }

    def getTenant(name){
        try{
            def res = getAdminGroup(name)
            return [
                    status: res.status,
                    data: [
                            id: res.data.profile.description.tenantId,
                            name: tenant,
                            ADMINS_groupId: res.data.id,
                            created: res.data.created
                    ]
            ]
        }catch (e){
            println(e)
            throw e
        }
    }

    OktaTenantService() {

    }

}
