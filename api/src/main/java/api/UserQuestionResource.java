 package api;

 import api.auth.Auth;
 import rx.Observable;

 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.DELETE;
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
     * Only title and question can be updated.
     *
     */
    @Path("{userId}/questions")
    @GET
    Observable<List<Question>> getQuestions(@PathParam("userId") long userId);

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

    /**
     * Deletes a question. The answers connected to the question will be deleted as well.
     * @param questionId the question to delete
     */
    @DELETE
    @Path("me/questions/{questionId}")
    Observable<Void> deleteQuestion(Auth auth, long questionId);
}
