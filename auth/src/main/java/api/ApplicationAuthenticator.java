package api;

import javax.validation.constraints.NotNull;

public interface ApplicationAuthenticator {
	ApplicationToken create(@NotNull String openIdToken, long userId);
}
