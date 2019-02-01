package impl;

import api.ApplicationAuthenticator;
import api.ApplicationToken;

import javax.validation.constraints.NotNull;

public class ApplicationAuthenticatorImpl implements ApplicationAuthenticator {

	@Override
	public ApplicationToken create(@NotNull String openIdToken, long userId) {

		// parse openid with jwt
		// validate the openId thing
		// map values over from openId token to Application token
		ApplicationToken applicationToken = new ApplicationToken();


		return null;
	}

}
