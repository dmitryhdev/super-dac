package co.betterdata

import co.betterdata.services.OktaIDPService
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.Post
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import jakarta.inject.Inject
import org.reactivestreams.Publisher

@Controller("/idps")
class IdpsController {
    @Inject
    OktaIDPService oktaService


    def process_res(res){
        println(oktaService.mapToStr(res.data))
        if (res.status < 300) {
            return HttpResponse.status(HttpStatus.OK).body(oktaService.mapToStr(res.data))
        } else{
            return HttpResponse.status(HttpStatus.BAD_REQUEST).body(oktaService.mapToStr(res.data))
        }
    }

    @Post(uri="/get", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def idps_get(idpId){
        def res = oktaService.idps_get(idpId)
        process_res(res)
    }

    @Post(uri="/list", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def idps_list(tenants){
        def res = oktaService.idps_list(tenants)
        process_res(res)
    }

    @Post(uri="/metadata", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def idps_metadata(idpId){
        def res = oktaService.idps_metadata(idpId)
        process_res(res)
    }

    @Post(uri="/update", consumes = MediaType.APPLICATION_JSON, produces ="application/json")
    def idps_update(idpId, payload){
        def res = oktaService.idps_update(idpId, payload)
        process_res(res)
    }


}
