 package api;

 import api.auth.Auth;
 import rx.Observable;
 import se.fortnox.reactivewizard.jaxrs.PATCH;

 import javax.ws.rs.*;
 import java.util.List;

/**
 * Manages answers for questions
 */
@Path("/api/users/")
public interface UserAnswerResource {

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
    @Path("me/answers/{answerId}")
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
    @Path("me/answers/{answerId}")
    Observable<Void> deleteAnswer(Auth auth, @PathParam("answerId") long answerId);
}
