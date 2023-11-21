package co.betterdata.services

import io.micronaut.context.annotation.Value
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject
import groovy.json.JsonSlurper
import groovy.json.JsonOutput


@Singleton
public class Okta {
    @Value('${okta.api-token}')
    protected String api_token

    @Value('${okta.client_id}')
    protected String appClientId

    @Value('${okta.dns_verify_prefix}')
    protected String dns_verify_prefix

    @Value('${okta.idp_disco_policy_id}')
    protected String idp_disco_policy_id

    @Value('${okta.base-url}')
    protected String base_url

    @Value('${okta.x5c}')
    protected String x5c

//    @Value('${okta.dac_prefix')
    protected String dac_prefix = "_"

    @Inject
    @Client("/")
    HttpClient client; // Inject the HTTP client

    public def get(String url, params = null){
        url = base_url + url
        println("SSWS $api_token")
        HttpRequest<Object> request = HttpRequest.GET(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "SSWS $api_token")
//        return client.toBlocking().retrieve(request)
        HttpResponse<String> response = client.toBlocking().exchange(request, String.class);
        int statusCode = response.getStatus().getCode();
        String data = response.getBody().orElseGet(() -> "")

//        println("data = ${data}")
        return [
                status: statusCode,
                data: strToMap(data)
        ]
    }

    public def get_general(String url, params = null){

        println("SSWS $api_token")
        HttpRequest<Object> request = HttpRequest.GET(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "SSWS $api_token")
//        return client.toBlocking().retrieve(request)
        HttpResponse<String> response = client.toBlocking().exchange(request, String.class);
        int statusCode = response.getStatus().getCode();
        String data = response.getBody().orElseGet(() -> "")

//        println("data = ${data}")
        return [
                status: statusCode,
                data: strToMap(data)
        ]
    }

    public def post(String url, body = null){
        url = base_url + url
        println("SSWS $api_token")
        HttpRequest<Object> request = HttpRequest.POST(url, mapToStr(body))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "SSWS $api_token")
//        return client.toBlocking().retrieve(request)
        HttpResponse<String> response = client.toBlocking().exchange(request, String.class);
        int statusCode = response.getStatus().getCode();
        String data = response.getBody().orElseGet(() -> "")
//        println("data = ${data}")
        return [
                status: statusCode,
                data: strToMap(data)
        ]
    }

    public def put(String url, body = null){
        url = base_url + url
        println("SSWS $api_token")
        HttpRequest<Object> request = HttpRequest.PUT(url, mapToStr(body))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "SSWS $api_token")
//        return client.toBlocking().retrieve(request)
        HttpResponse<String> response = client.toBlocking().exchange(request, String.class);
        int statusCode = response.getStatus().getCode();
        String data = response.getBody().orElseGet(() -> "")
//        println("data = ${data}")
        return [
                status: statusCode,
                data: strToMap(data)
        ]
    }

    public def delete(String url, body = null){
        url = base_url + url
        println("SSWS $api_token")
        HttpRequest<Object> request = HttpRequest.DELETE(url, mapToStr(body))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "SSWS $api_token")
//        return client.toBlocking().retrieve(request)
        HttpResponse<String> response = client.toBlocking().exchange(request, String.class);
        int statusCode = response.getStatus().getCode();
        String data = response.getBody().orElseGet(() -> "")
//        println("data = ${data}")
        return [
                status: statusCode,
                data: strToMap(data)
        ]
    }

    def strToMap(str){
        def jsonSlurper = new JsonSlurper()
        def map = jsonSlurper.parseText(str)
        return map
    }

    def mapToStr(_map){
        def str = JsonOutput.toJson(_map)
        return str
    }

    public def getGroup(type, name){
        def res = get('/groups?q=' + type + '_'+ name +'&limit=2')
        println('res=' + res.data)
        if (res.status >= 300){
//            throw  new RuntimeException("error occured while getting group")
            return [status: 400, data: "error occured while getting group"]
        }
        res = res.data
        println(res.size())
        println(res[0])
        if (res.size() > 0) {
            def body = res[0]
            body.remove("_links")
            return [status: 200, data: body]
        }
//        if (res.length() > 0){
//            def body = res[0]
//            println(body)
//        }

        return [status: 400, data: "Not Found"]
    }

    public def addKey(x5c) {
        try{
            println("--- add key response ---")
//            def res1 = delete("/idps/credentials/keys/f6adc408-fb33-4fa3-bc3b-916c5fafe19b", "")
//            println(res1)
            def res = get('/idps/credentials/keys')
            println(res.data)
            def filtered = res.data.findAll{it.x5c == [x5c]}
            if (filtered.size() > 0) {
                println("found existing kid: ${filtered[0].kid}")
                return filtered[0].kid
            } else {
                    res = post('/idps/credentials/keys', [x5c: [x5c]])
                    println(res)
                    if (res.status == 200) {
                        def data = res.data
                        def kid = data.kid
                        println("successfully added kid: ${kid}")
                        return kid
                    }
                }
        }catch (HttpClientResponseException e){
            println(e)
            throw  e
        }

    }

    public def getAdminGroup(name){
        return getGroup('ADMINS', name)
    }

    public def getUserGroup(name){
        return getGroup('USERS', name)
    }

    def addUserAdminRole(groupId){
        try {
            def res = post("/groups/${groupId}/roles", [type: "USER_ADMIN"])
            return res
        }catch (e){
            println(e)
            throw e
        }
    }

    def addGroupAdminTarget(groupId, roleId, targetId){
        try{
            def res = post("/groups/${groupId}/roles/${roleId}/targets/groups/${targetId}")
            return res
        }catch(e){
            println(e)
            throw  e
        }
    }

    def uuid(){
        String uuidString = "536e97e9-0d29-43ec-b8d5-a505d3ee6a8f"
        UUID uuid = UUID.fromString(uuidString)
        return  uuid
    }

}
