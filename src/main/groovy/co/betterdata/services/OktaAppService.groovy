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

class OktaAppService extends OktaTenantService {
    def listApps(claims, authorizer){
        def res
        def filtered =[]
        def groups = claims.groups
        try{
            if (groups.includes('SUPERUSERS')) {
                def url = base_url + '/api/v1/apps?limit=100';
                res = get(url);
                filtered = res.data.filter(app -> {
                    return (app.label.startsWith(dac_prefix));
                });
            } else {
                def tenants = claims.tenants
                if (tenants && tenants.length > 0) {
                    tenants.each { tenant ->
                        {
                            res = getTenantApps(
                                    tenant.split(':')[1],
                                    authorizer
                            );
                            filtered = filtered.concat(res.data);
                        }
                    }
                }
            }
        }catch(e){
            throw e
        }
        def data = filtered.map(app -> {
            return [
                id: app.id,
                APPUSERS_groupId: app.APPUSERS_groupId,
                name: app.label ? app.label.split(dac_prefix)[1] : app.name,
                created: app.created,
                lastUpdated: app.lastUpdated,
                logo: app.logo ? app.logo : app._links.logo,
                settings: app.settings
            ]
        });
        return [
            status: res.status,
            data: data
        ]
    }

    def apps_list(claim, authorizer){
        try{
            def res = listApps(claim, authorizer)
            return res
        } catch (e){
            return [
                    status: e.response.status,
                    data: e.response.data
            ]
        }
    }
}
