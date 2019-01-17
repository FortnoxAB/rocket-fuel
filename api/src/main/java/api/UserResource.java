 package api;

import rx.Observable;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;

@Path("/api/user")
public interface UserResource {

    /**
     * Creates or updates a user. if the users email already is registered, the user will be updated.
     * If the email cannot be found, a new user will be created.
     * @param userId of the user that will be created/updated
     * @param user user that will be created/updated.
     * @return
     */
    @PUT
    Observable<Void> createUser(long userId, User user);

    /**
     * Returns the user by email
     * @param email that belongs to the user
     * @return
     */
    @GET
    @Path("email/{email}")
    Observable<User> getUserByEmail(@NotNull @PathParam("email") String email);

    /**
     * Returns the user by userId
     * @param userId that belongs to the user
     * @return
     */
    @GET
    @Path("id/{userId}")
    Observable<User> getUserById(@PathParam("userId") long userId);

}
