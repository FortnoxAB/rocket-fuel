package api;

import api.auth.Auth;
import rx.Observable;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.util.List;

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

    /**
     * Return a question if found by id
     * @param questionId
     * @return question
     */
    @GET
    @Path("/{questionId}")
    Observable<Question> getQuestionById(@PathParam("questionId") long questionId);

    /**
     * Return a list of latest questions with a limit
     * @param limit
     * @return questions
     */
    @GET
    @Path("/latest")
    Observable<List<Question>> getLatestQuestion(@QueryParam("limit") Integer limit);

    /**
     * Adds a question and links it to the given userId.
     */
    @POST
    Observable<Question> createQuestion(Auth auth, Question question);

    /**
     * Acts like a universal search. It will return questions that can be related to the search term.
     */
    @GET
    Observable<List<Question>> getQuestionsBySearchQuery(@QueryParam("search") String searchQuery);

}
