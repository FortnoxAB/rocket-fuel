package api;

import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;

import javax.ws.rs.*;

import java.util.List;


/**
 * Retrives questions from the database
 *
 */
@Path("/users/{userId}/questions")
public interface QuestionResource {

    @GET
    Observable<List<Question>> getQuestions(@PathParam("userId") long userId, CollectionOptions collectionOptions);

    @POST
    Observable<Void> postQuestion(@PathParam("userId") long userId, Question question);

    @PUT
    @Path("{questionId}")
    Observable<Question> updateQuestion(@PathParam("userId") long userId, Question question);

}
