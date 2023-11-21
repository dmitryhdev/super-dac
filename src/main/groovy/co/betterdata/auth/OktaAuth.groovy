package co.betterdata.auth

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
import io.micronaut.http.exceptions.HttpException
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import jakarta.inject.Inject
import org.reactivestreams.Publisher


@Filter("/**")
class OktaAuth implements HttpServerFilter {

    @Value('${okta.issuer_url}')
    protected String issuer

    @Override
    Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        try{
            if(request.getUri().getPath().contains("swagger")) {
                Publisher<MutableHttpResponse<?>> response = chain.proceed(request)
                return response
            }
            if(request.getUri().getPath().contains("healthcheck")) {
                Publisher<MutableHttpResponse<?>> response = chain.proceed(request)
                return response
            }
            def jwtToken = request.getHeaders().get("Authorization").split(" ")[1]

            if (!jwtToken) throw new HttpStatusException(HttpStatus.FORBIDDEN, "Request rejected");
            def verifier = JwtVerifiers.accessTokenVerifierBuilder()
                    .setIssuer(issuer)
            def jwt = verifier.decode(jwtToken)
            def tenants = jwt.claims.tenants
            if (tenants && tenants.size()>0){
                tenants.each{tenant -> {
                    def parts = tenant.split(":")
                    def allowedUrl1 = "idps/${parts[0]}"
                    def allowedUrl2 = "tenants/${parts[1]}"
                    if(request.getUri().getPath().contains(allowedUrl1) || request.getUri().getPath().contains(allowedUrl2)){
                        Publisher<MutableHttpResponse<?>> response = chain.proceed(request)
                        return response
                    }
                }}
            }
            if (jwt.claims.groups && jwt.claims.groups.includes('SUPERUSERS')) {
                // Only superusers can read/add tenants
                def allowedUrl = "tenants/"
                if(request.getUri().getPath().contains(allowedUrl)){
                    Publisher<MutableHttpResponse<?>> response = chain.proceed(request)
                    return response
                }
            }
            //        LOG.info("Before request processing")
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Request rejected");
        }catch(e){
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Request rejected");
        }

    }
}