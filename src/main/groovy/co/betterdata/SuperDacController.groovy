package co.betterdata

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/superDac")
class SuperDacController {


//    @Value('${micronaut.router.static-resources.swagger.paths}')
    @Value('${okta.api-token}')
    String api_token



    @Get(uri="/", produces="text/plain")
    String index() {
        println("this is log")
        "Example Response12 $api_token"
    }

}