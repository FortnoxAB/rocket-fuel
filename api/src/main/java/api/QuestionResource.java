 package api;

 import api.auth.Auth;
 import rx.Observable;
 import se.fortnox.reactivewizard.CollectionOptions;

 import javax.ws.rs.*;
 import java.util.List;


/**
 * Manages users questions.
 *
 */
@Path("/api/users/{userId}/questions")
public interface QuestionResource {

    /**
     * Returns all questions for a given user
     */
    @GET
    Observable<List<Question>> getQuestions(@PathParam("userId") long userId, CollectionOptions collectionOptions);

    @GET
    Observable<Question> getQuestion(@PathParam("userId") long userId, @PathParam("questionId") long questionId);


    /**
     * Adds a question and links it to the given userId.
     */
    @POST
    Observable<Void> postQuestion(Auth auth, @PathParam("userId") long userId, Question question);

    /**
     * Updates the question with the given questionId
     */
    @PUT
    @Path("{questionId}")
    Observable<Question> updateQuestion(@PathParam("userId") long userId, @PathParam("questionId") long questionId, Question question);

}
