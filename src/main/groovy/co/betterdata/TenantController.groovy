package co.betterdata

import co.betterdata.services.OktaTenantService
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import jakarta.inject.Inject

@Controller("/tenant")
class TenantController {


//    @Value('${micronaut.router.static-resources.swagger.paths}')
    @Value('${okta.api-token}')
    String api_token

    @Inject
    OktaTenantService oktaService



    @Get(uri="/list", produces="text/plain")
    def  index(after=null, search=null) {
        println("this is log")
        def res = oktaService.list(after, search)
        println(res)
        process_res(res)
    }

    @Get(uri="/deleteall", produces="text/plain")
    def  deleteall() {
        def groups = oktaService.list()
        groups.data.each{v -> {
            println(v)
            try{

            oktaService.delete("/groups/${v.id}")
            }catch(e){

            }
        }}
        return 'success'
    }

    @Get(uri="/get", produces = "application/json")
    def getSingle(String name){
        def res = oktaService.getTenant(name)
        println(res)
        process_res(res)
    }

    @Post(uri="/add",consumes = MediaType.APPLICATION_JSON, produces="application/json")
    def  add(String name) {
        println name
        def res =oktaService.add(name)
        print(res)
        process_res(res)
    }

    @Post(uri="/admins/assign", consumes = MediaType.APPLICATION_JSON, produces = "application/json")
    def admins_assign(String tenanatName, String userId){
        def res = oktaService.adminAssign(tenanatName, userId)
        process_res(res)
    }

    @Post(uri="/admins/list", consumes = MediaType.APPLICATION_JSON, produces = "application/json")
    def admins_assign(String tenanat){
        def res = oktaService.admins_list(tenanat)
        process_res(res)
    }

    @Post(uri="/apps/deactivate", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def apps_deactivate(tenant, appId){
        def res = oktaService.apps_deactivate(tenant, appId)
        process_res(res)
    }

    @Post(uri="/apps/list", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def apps_list(tenant, authorizer){
        def res = oktaService.apps_list(tenant, authorizer)
        process_res(res)
    }

    @Post(uri="/apps/post", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def apps_post(allUsers, tenant, appId){
        def res = oktaService.apps_post(allUsers, tenant, appId)
        process_res(res)
    }

    @Post(uri="/domains/add", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def domains_add(tenant, body){
        def res = oktaService.domains_add(tenant, body)
        process_res(res)
    }

    @Post(uri="/domains/delete", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def domains_delete(tenant, domain){
        def res = oktaService.domains_delete(tenant, domain)
        process_res(res)
    }

    @Post(uri="/domains/list", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def domains_list(tenant, verified){
        def res = oktaService.domains_list(tenant, verified)
        process_res(res)
    }

    @Post(uri="/domains/verify", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def domains_verify(tenant, verified, dnsVerificationString){
        def res = oktaService.domains_verify(tenant, verified, dnsVerificationString)
        process_res(res)
    }


    def process_res(res){
        println(oktaService.mapToStr(res.data))
        if (res.status < 300) {
            return HttpResponse.status(HttpStatus.OK).body(oktaService.mapToStr(res.data))
        } else{
            return HttpResponse.status(HttpStatus.BAD_REQUEST).body(oktaService.mapToStr(res.data))
        }
    }

}