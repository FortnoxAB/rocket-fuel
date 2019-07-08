package api;

import api.auth.Auth;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.PATCH;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@Path("api/answers")
public interface AnswerResource {

    /**
     * Creates an answer to a question
     */
    @POST
    @Path("question/{questionId}")
    Observable<Answer> answerQuestion(Auth auth, Answer answer, @PathParam("questionId") long questionId);

    /**
     * Returns all answers for a given question
     *
     * @param questionId the question that we want answers from
     * @return answers for a question
     */
    @Path("question/{questionId}")
    @GET
    Observable<List<Answer>> getAnswers(Auth auth, @PathParam("questionId") long questionId);

    /**
     * Marks a given answer as answered. The method will mark the question as well as answered.
     * @param answerId the answer id
     * @return nothing
     */
    @PATCH
    @Path("accept/{answerId}")
    Observable<Void> markAsAcceptedAnswer(Auth auth, @PathParam("answerId") long answerId);

    /**
     * Returns the answer matching a certain slackId
     *
     * @return answers for a question
     */
    @Path("byslackid/{slackId}")
    @GET
    Observable<Answer> getAnswerBySlackId(@PathParam("slackId") String slackId);

    @Path("{answerId}")
    @GET
    Observable<Answer> getAnswerById(@PathParam("answerId") long answerId);
}
