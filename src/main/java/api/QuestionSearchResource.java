package api;

import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@Path("questions")
public interface QuestionSearchResource {

    @GET
    Observable<List<Question>> getQuestions(CollectionOptions collectionOptions);
}
