 package api;

 import api.auth.Auth;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.PATCH;

 import javax.ws.rs.*;
import java.util.List;

/**
 * Manages answers for questions
 */
@Path("/api")
public interface AnswerResource {

    /**
     * Returns all answers for a given question
     * @param userId UserId for the owner of the answer
     * @param questionId the question that we want answers from
     * @return answers for a question
     */
    @Path("{userId}/questions/{questionId}/answers")
    @GET
    Observable<List<Answer>> getAnswers(@PathParam("userId") long userId, @PathParam("questionId") long questionId);

    /**
     * Creates a answer for a given question
     * @param questionId The question shall be linked to the answer
     * @param answer the answer body
     * @return nothing
     */
    @Path("me/questions/{questionId}/answers")
    @POST
    Observable<Void> createAnswer(Auth auth, @PathParam("questionId") long questionId, Answer answer);

    /**
     * Updates a answer
     * @param questionId the question that acts as parent for the answer
     * @param answerId the answers unique id
     * @param answer the new state of the
     * @return nothing
     */
    @PUT
    @Path("me/questions/{questionId}/answers/{answerId}")
    Observable<Void> updateAnswer(Auth auth, @PathParam("questionId") long questionId, @PathParam("answerId") long answerId, Answer answer);

    /**
     * Marks a given answer as answered. The method will notify the question as well and mark the question as answered.
     * @param questionId the question id
     * @param answerId the answer id
     * @return nothing
     */
    @PATCH
    @Path("me/questions/{questionId}/answers/{answerId}/answered")
    Observable<Void> markAsAnswered(Auth auth, @PathParam("questionId") long questionId, @PathParam("answerId") long answerId);
}
