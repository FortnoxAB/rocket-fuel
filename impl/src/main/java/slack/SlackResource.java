package slack;

import com.github.seratch.jslack.api.model.Message;
import rx.Observable;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/internal/slack")
public interface SlackResource {

    @GET
    @Path("user/{userId}")
    Observable<String> getUserEmail(@PathParam("userId") String userId);

    @PUT
    @Path("message")
    Observable<Void> postMessageToSlack(String channel, String message, String threadTs);

    @GET
    @Path("message")
    Observable<Message> getMessageFromSlack(String channel, String mainMessageId);
}
