 package api;

 import api.auth.Auth;
 import rx.Observable;
 import se.fortnox.reactivewizard.CollectionOptions;

 import javax.ws.rs.*;
 import java.util.List;


/**
 * Manages a users questions.
 *
 * A question is always linked to a {@link User} and may have {@link Answer}s connected to it.
 *
 */
@Path("/api/users/")
public interface QuestionResource {

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
     * Adds a question and links it to the given userId.
     */
    @Path("me/questions")
    @POST
    Observable<Void> postQuestion(Auth auth, Question question);

    /**
     * Updates the question with the given questionId
     */
    @PUT
    @Path("me/questions/{questionId}")
    Observable<Question> updateQuestion(Auth auth, @PathParam("questionId") long questionId, Question question);

}
