 package api;

 import api.auth.ApplicationToken;
 import api.auth.Auth;
 import rx.Observable;

 import javax.validation.constraints.NotNull;
 import javax.ws.rs.*;

@Path("/api/user")
public interface UserResource {

    /**
     * returns the current signed in user.
     *
     * @param auth
     * @return
     */
    @GET
    Observable<String> getCurrent(Auth auth);
    /**
     * Creates a user
     * @param user user that will be created/updated.
     * @return
     */
    @PUT
    Observable<Integer> createUser(User user);

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

    /**
     * Returns a application token, that shall be used when the client uses endpoints
     * that needs authentication. The token contains valuable information about the user.
     *
     * A cookie named application will be returned in the request, containing the token, so that
     * further requests will be authenticated, if the client is a browser.
     * @param authorizationToken a OpenId jwt token
     * @return a application token as a jwt
     */
    @GET
    @Path("authenticate")
    Observable<ApplicationToken> generateToken(@HeaderParam("authorizationToken") @NotNull String authorizationToken);

}
