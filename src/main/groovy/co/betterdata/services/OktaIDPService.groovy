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

class OktaIDPService extends Okta {
    def getIdp(id){
        def result;
        try{
            def res = get("/idps/${id}")
            result.data = res.data
            result.data.name = result.data.name.split(dac_prefix)[1];
            delete result.data._links.metadata;
            delete result.data._links.users;
            delete result.data._links.deactivate;

            def key = get("/idps/credentials/keys/${kid}")
            def index = key.data.x5c.findIndex(cert -> {
                return (cert == process.env.TEMPLATE_CERT);
            });
            if (index < 0) result.data.x5c = key.data.x5c;
            result.status = 200;

            def kid = result.data.protocol.credentials.trust.kid;
        }catch(e){
            throw e
        }
        return result
    }

    def idps_get(idpId){
        def res = getIdp(idpId)
        return [
                status: res.status,
                data: res.data
        ]
    }

    def idps_list(tenants){
        try{
            def authorized = tenants;
            def data = [];
            authorized = strToMap(authorized);
            authorized.each{idp -> {
                def id = idp.split(':')[0];
                def res = await getIdp.getIdp(id);
                response.statusCode = res.status;
                data.push(res.data);
            }}
            response.body = JSON.stringify(data);
        }catch(e){
            return [
                    status: e.response.status,
                    data: e.response.data
            ]
        }
    }

    def idps_metadata(idpId){
        try{
            def res = get("/idps/${idpId}/metadata.xml")
            return res
        }catch(e){
            return [
                    status: e.response.status,
                    data: e.response.data
            ]
        }
    }

    def updateIdp(id, payload){
        def result
        def kid = payload.protocol.credentials.trust.kid;

        if (payload.protocol.credentials.trust.kid == '')
            payload.x5c = x5c;

        if (payload.x5c) {
            try {
                kid = addKey(payload.x5c);
            } catch (e) {
                // do nothing;
            }
            delete payload.x5c;
        }
        try {
            payload.protocol.credentials.trust.kid = kid;
            if (!payload.name.startsWith(lib.DAC_PREFIX)) payload.name = lib.DAC_PREFIX + payload.name;

            def res = put('/idps/' + id, payload);
            result.status = res.status;
            def data = res.data;
            delete data._links;
            result.data = data;
        } catch (e) {
            throw e;
        }
        return result;
    }

    def idps_update(idpId, payload){
        try {
            def res = updateIdp(idpId, payload);
            return res

        } catch (e) {
            return [
                    status: e.response.status,
                    data: e.response.data
            ]
        }

    }
}
