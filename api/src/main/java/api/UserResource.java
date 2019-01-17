 package api;

import rx.Observable;

import javax.ws.rs.*;

@Path("/api/user")
public interface UserResource {

    @POST
    Observable<Void> createUser( Long userId, User question);

}
