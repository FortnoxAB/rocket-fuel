 package api;

 import api.auth.Auth;
 import rx.Observable;
 import se.fortnox.reactivewizard.jaxrs.PATCH;

 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
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
     * @param questionId the question that acts as parent for the answer
     * @param answerId the answers unique id
     * @param answer the new state of the
     * @return nothing
     */
    @PUT
    @Path("me/questions/{questionId}/answers/{answerId}")
    Observable<Void> updateAnswer(Auth auth, @PathParam("questionId") long questionId, @PathParam("answerId") long answerId, Answer answer);
}
