 package api;

 import api.auth.Auth;
 import rx.Observable;

 import javax.validation.constraints.NotNull;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;

@Path("/api/user")
public interface UserResource {

    /**
     * returns the current signed in user.
     *
     * @param auth
     * @return
     */
    @Path("me")
    @GET
    Observable<User> getCurrent(Auth auth);
    /**
     * Creates a user
     * @param user user that will be created/updated.
     * @return
     */
    @POST
    Observable<Integer> createUser(Auth auth, User user);

    /**
     * Returns the user by email
     * @param email that belongs to the user
     * @return
     */
    @GET
    @Path("email/{email}")
    Observable<User> getUserByEmail(@NotNull @PathParam("email") String email, @QueryParam("createIfMissing") boolean createIfMissing);

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
    Observable<User> signIn(@HeaderParam("authorizationToken") @NotNull String authorizationToken);

    /**
     * Signs out the user by telling client that the cookie shall be removed and is invalid.
     *
     * Set-Cookie header is used to perform the action.
     *
     * @return the userId
     */
    @DELETE
    @Path("signOut")
    Observable<Long> signOut(Auth auth);
}
