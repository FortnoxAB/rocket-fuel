package auth;

import rx.Observable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("")
public interface JwkResource {

    @GET
    Observable<JwkResponse> getJWks();

}
