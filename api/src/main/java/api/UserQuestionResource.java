 package api;

 import api.auth.Auth;
 import rx.Observable;
 import se.fortnox.reactivewizard.CollectionOptions;

 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import java.util.List;


/**
 * Manages a users questions.
 *
 * A question is always linked to a {@link User} and may have {@link Answer}s connected to it.
 *
 */
@Path("/api/users/")
public interface UserQuestionResource {

    /**
     * Returns all questions for a given user
     *
     * Collection options can be used to limit the number questions returned to the client.
     *
     */
    @Path("{userId}/questions")
    @GET
    Observable<List<Question>> getQuestions(@PathParam("userId") long userId, CollectionOptions collectionOptions);

    /**
     * Returns a specific question to the client
     */
    @Path("{userId}/questions/{questionId}")
    @GET
    Observable<Question> getQuestion(@PathParam("userId") long userId, @PathParam("questionId") long questionId);
    
    /**
     * Updates the question with the given questionId
     */
    @PUT
    @Path("me/questions/{questionId}")
    Observable<Question> updateQuestion(Auth auth, @PathParam("questionId") long questionId, Question question);
}
