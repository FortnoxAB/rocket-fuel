package api;

import api.auth.Auth;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.PATCH;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;
@Path("api")
public interface AnswerResource {

    /**
     * Creates an answer to a question
     */
    @POST
    @Path("questions/{questionId}/answers")
    Observable<Answer> createAnswer(Auth auth, Answer answer, @PathParam("questionId") long questionId);

    /**
     * Returns all answers for a given question
     *
     * @param questionId the question that we want answers from
     * @return answers for a question
     */
    @Path("questions/{questionId}/answers")
    @GET
    Observable<List<Answer>> getAnswers(Auth auth, @PathParam("questionId") long questionId);

    /**
     * Marks a given answer as answered. The method will mark the question as well as answered.
     * @param answerId the answer id
     * @return nothing
     */
    @PATCH
    @Path("answers/{answerId}/accept")
    Observable<Void> markAsAcceptedAnswer(Auth auth, @PathParam("answerId") long answerId);

    /**
     * Updates a answer
     *
     * Requires the invoker to be the creator of the answer.
     *
     * Only title and answer can be answered.
     *
     * @param answerId the answers unique id
     * @param answer the new state of the answer
     * @return nothing
     */
    @PUT
    @Path("answers/{answerId}")
    Observable<Void> updateAnswer(Auth auth, @PathParam("answerId") long answerId, Answer answer);

    /**
     * Deletes an answer
     *
     * Requires the invoker to be the creator of the answer.
     * @param auth
     * @param answerId
     * @return nothing
     */
    @DELETE
    @Path("answers/{answerId}")
    Observable<Void> deleteAnswer(Auth auth, @PathParam("answerId") long answerId);

    /**
     * Upvotes (+1) an answer.
     * Upvoting an existing downvote will result in a neutral (0) vote.
     * Upvoting an existing upvote will result in error.
     * @param auth
     * @param answerId
     * @return
     */
    @POST
    @Path("answers/{answerId}/upvote")
    Observable<Void> upVoteAnswer(Auth auth, @PathParam("answerId") long answerId);

    /**
     * Downvotes (-1) an answer.
     * Downvoting an existing upvote will result in a neutral (0) vote.
     * Downvoting and existing downvote will result in error.
     * @param auth
     * @param answerId
     * @return
     */
    @POST
    @Path("answers/{answerId}/downvote")
    Observable<Void> downVoteAnswer(Auth auth, @PathParam("answerId") long answerId);

    /**
     * Returns the answer matching a certain slackId
     *
     * @return answers for a question
     */
    Observable<Answer> getAnswerBySlackId(String slackId);

    Observable<Answer> getAnswerById(long answerId);
}
