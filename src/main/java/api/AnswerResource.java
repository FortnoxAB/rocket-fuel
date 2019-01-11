package api;

import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.PATCH;

import javax.ws.rs.*;
import java.util.List;

/**
 * Manages answers for questions
 */
@Path("users/{userId}/questions/{questionId}/answers")
public interface AnswerResource {

    /**
     * Returns all answers for a given question
     * @param userId UserId for the owner of the answer
     * @param questionId the question that we want answers from
     * @return answers for a question
     */
    @GET
    Observable<Answer> getAnswers(@PathParam("userId") long userId, @PathParam("questionId") long questionId);

    /**
     * Creates a answer for a given question
     * @param userId UserId for the owner of the answer
     * @param questionId The question shall be linked to the answer
     * @param answer the answer body
     * @return nothing
     */
    @POST
    Observable<Void> createAnswer(@PathParam("userId") long userId, @PathParam("questionId") long questionId, Answer answer);

    /**
     * Updates a answer
     * @param userId the owner of the answer
     * @param questionId the question that acts as parent for the answer
     * @param answerId the answers unique id
     * @param answer the new state of the
     * @return nothing
     */
    @PUT
    @Path("{answerId}")
    Observable<Void> updateAnswer(@PathParam("userId") long userId, @PathParam("questionId") long questionId, @PathParam("answerId") long answerId, Answer answer);

    /**
     * Marks a given answer as answered. The method will notify the question as well and mark the question as answered.
     * @param userId the owner of the question
     * @param questionId the question id
     * @param answerId the answer id
     * @return nothing
     */
    @PATCH
    @Path("{answerId}/answered")
    Observable<Void> markAsAnswered(@PathParam("userId") long userId,@PathParam("questionId") long questionId, @PathParam("answerId") long answerId);
}
