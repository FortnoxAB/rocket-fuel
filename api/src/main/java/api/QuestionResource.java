package api;

import api.auth.Auth;
import rx.Observable;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("api")
public interface QuestionResource {

    /**
     * Return a question if found by a slack id
     *
     * @param slackId id from slack
     * @return question
     */
    @GET
    @Path("questions/byslackid/{slackId}")
    Observable<Question> getQuestionBySlackThreadId(@PathParam("slackId") String slackId);

    /**
     * Returns a specific question to the client
     */
    @Path("questions/{questionId}")
    @GET
    Observable<Question> getQuestion(Auth auth, @PathParam("questionId") long questionId);

    /**
     * Return a list of latest questions
     *
     * @param limit
     * @return questions
     */
    @GET
    @Path("questions/latest")
    Observable<List<Question>> getLatestQuestions(@QueryParam("limit") Integer limit);

    /**
     * Return a list of the highest voted questions
     *
     * @param limit
     * @return questions
     */
    @GET
    @Path("questions/popular")
    Observable<List<Question>> getPopularQuestions(@QueryParam("limit") Integer limit);

    /**
     * Return a list of the highest voted questions without any answers
     *
     * @param limit
     * @return questions
     */
    @GET
    @Path("questions/popularunanswered")
    Observable<List<Question>> getPopularUnansweredQuestions(@QueryParam("limit") Integer limit);

    /**
     * Return a list of the questions that had an answer accepted the most recently
     *
     * @param limit
     * @return questions
     */
    @GET
    @Path("questions/recentlyaccepted")
    Observable<List<Question>> getRecentlyAcceptedQuestions(@QueryParam("limit") Integer limit);

    /**
     * Adds a question and links it to the given userId.
     */
    @POST
    @Path("questions")
    Observable<Question> createQuestion(Auth auth, Question question);

    /**
     * Acts like a universal search. It will return questions that can be related to the search term.
     */
    @GET
    @Path("questions")
    Observable<List<Question>> getQuestionsBySearchQuery(@QueryParam("search") String searchQuery, @QueryParam("limit") Integer limit);


    /**
     * Returns all questions for a given user
     */
    @Path("users/{userId}/questions")
    @GET
    Observable<List<Question>> getQuestions(@PathParam("userId") long userId, @QueryParam("limit") Integer limit);

    /**
     * Updates the question with the given questionId
     *
     * Only title and question can be updated.
     * Requires the invoker to be the creator of the ques..
     */
    @PUT
    @Path("questions/{questionId}")
    Observable<Question> updateQuestion(Auth auth, @PathParam("questionId") long questionId, Question question);

    /**
     * Deletes a question. The answers connected to the question will be deleted as well.
     * @param questionId the question to delete
     */
    @DELETE
    @Path("questions/{questionId}")
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
    @Path("questions/{questionId}/upvote")
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
    @Path("questions/{questionId}/downvote")
    Observable<Void> downVoteQuestion(Auth auth, @PathParam("questionId") long questionId);


    /**
     * Return a question if found by id
     *
     * @return question
     */
    Observable<Question> getQuestionById(long questionId);
}
