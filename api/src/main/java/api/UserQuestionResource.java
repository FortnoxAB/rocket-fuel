 package api;

 import api.auth.Auth;
 import rx.Observable;

 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
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
     */
    @Path("{userId}/questions")
    @GET
    Observable<List<Question>> getQuestions(@PathParam("userId") long userId);

    /**
     * Returns a specific question to the client
     */
    @Path("me/questions/{questionId}")
    @GET
    Observable<Question> getQuestion(Auth auth, @PathParam("questionId") long questionId);

    /**
     * Updates the question with the given questionId
     *
     * Only title and question can be updated.
     * Requires the invoker to be the creator of the ques..
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
    Observable<Void> deleteQuestion(Auth auth, @PathParam("questionId") long questionId);


    /**
     * Upvotes (+1) a question.
     * Upvoting an existing downvote will result in a neutral (0) vote.
     * Upvoting an existing upvote will result in error.
     * @param auth
     * @param questionId
     * @return
     */
    @POST
    @Path("me/questions/{questionId}/upvote")
    Observable<Void> upVoteQuestion(Auth auth, @PathParam("questionId") long questionId);

    /**
     * Downvotes (-1) a question.
     * Downvoting an existing upvote will result in a neutral (0) vote.
     * Downvoting and existing downvote will result in error.
     * @param auth
     * @param questionId
     * @return
     */
    @POST
    @Path("me/questions/{questionId}/downvote")
    Observable<Void> downVoteQuestion(Auth auth, @PathParam("questionId") long questionId);
}
