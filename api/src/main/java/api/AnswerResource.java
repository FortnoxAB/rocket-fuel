package api;

import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.PATCH;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("api/answers")
public interface AnswerResource {

    /**
     * Returns all answers for a given question
     *
     * @param questionId the question that we want answers from
     * @return answers for a question
     */
    @Path("questions/{questionId}")
    @GET
    Observable<Answer> getAnswers(@PathParam("questionId") long questionId);

    /**
     * Returns the answer matching a certain slackId
     *
     * @return answers for a question
     */
    @Path("byslackid/{slackId}")
    @GET
    Observable<Answer> getAnswerBySlackId(@PathParam("slackId") String slackId);


    /**
     * Votes on answer with slackid = threadId.
     */
    @PATCH
    @Path("upvote/{slackId}")
    Observable<Void> upVoteAnswer(@PathParam("slackId") String slackId);

    /**
     * Removes vote on answer
     *
     * @param slackId the slackid to downvote
     *
     * @return
     */
    @PATCH
    @Path("downvote/{slackId}")
    Observable<Void> downVoteAnswer(@PathParam("slackId") String slackId);
}
