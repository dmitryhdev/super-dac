package co.betterdata
import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/healthcheck")
class HealthCheckController {
    @Get(uri="/", produces="text/plain")
    String index() {
        println("this is health url")
        "health"
    }
}
