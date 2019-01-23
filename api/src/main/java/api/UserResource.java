 package api;

 import rx.Observable;

 import javax.validation.constraints.NotNull;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;

@Path("/api/user")
public interface UserResource {

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

}
