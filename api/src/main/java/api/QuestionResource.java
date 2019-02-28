package api;

import rx.Observable;

import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("api/questions")
public interface QuestionResource {
    /**
     * Gives a positive vote on a question
     * @param slackId
     * @return
     */
    @PATCH
    @Path("upvote/{slackId}")
    Observable<Void> upVoteQuestion(@PathParam("slackId") String slackId);

    /**
     * Gives a negative vote on a question
     * @param slackId
     * @return
     */
    @PATCH
    @Path("downvote/{slackId}")
    Observable<Void> downVoteQuestion(@PathParam("slackId") String slackId);

    /**
     * Return a question if found by a slack id
     * @param slackId id from slack
     * @return question
     */
    @GET
    @Path("byslackid/{slackId}")
    Observable<Question> getQuestionBySlackThreadId(@PathParam("slackId") String slackId);
}
