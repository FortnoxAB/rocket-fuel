 package api;

import rx.Observable;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

 /**
  * Acts like a universal search. It will return questions that can be related to the search tearm.
  */
 @Path("/api/questions")
public interface QuestionSearchResource {

    @GET
    Observable<List<Question>> getQuestionsBySearchQuery(@QueryParam("search") @NotNull String searchQuery);
}
