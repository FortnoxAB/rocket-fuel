package api;

import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;

import javax.ws.rs.*;
import java.util.List;

@Path("user")
public interface UserResource {

    @POST
    Observable<Void> createUser( Long userId, User question);

}
