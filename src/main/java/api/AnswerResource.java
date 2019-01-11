package api;

import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.PATCH;

import javax.ws.rs.*;
import java.util.List;

/**
 *
 */
@Path("users/{userId}/questions/{questionId}/answers")
public interface AnswerResource {

    @GET
    Observable<Answer> getAnswers(@PathParam("userId") long userId, @PathParam("questionId") long questionId);

    @POST
    Observable<Void> createAnswer(@PathParam("userId") long userId, @PathParam("questionId") long questionId, Answer answer);

    @PUT
    @Path("{answerId}")
    Observable<Void> updateAnswer(@PathParam("userId") long userId, @PathParam("questionId") long questionId, @PathParam("answerId") long answerId, Answer answer);

    @PATCH
    @Path("{answerId}/answered")
    Observable<Void> markAsAnswered(@PathParam("userId") long userId,@PathParam("questionId") long questionId, @PathParam("answerId") long answerId);
}
