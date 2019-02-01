package auth;

import javax.validation.constraints.NotNull;

public class OpenIdValidatorImpl  {

	public class ImmutableOpenIdToken {

		public final String name;
		public final String email;
		public final String picture;

		ImmutableOpenIdToken(String name, String email, String picture) {
			this.name = name;
			this.email = email;
			this.picture = picture;
		}
	}

	public ImmutableOpenIdToken validate(@NotNull String openIdToken) {
		return null;
	}
}
