package auth;

import java.util.List;

/**
 * Represents a response from a JWk endpoint.
 * <p>
 * Meaning of the parameters returned:
 * id                    kid
 * type                  kyt
 * algorithm             alg
 * usage                 use
 * operations            key_ops
 * certificateUrl        x5u
 * certificateChain      x5c
 * certificateThumbprint x5t
 * additionalAttributes  additional attributes not part of the standard ones
 */
public class JwkResponse {

    public JwkResponse() {
    }

    private List<Jwk> keys;


    public List<Jwk> getKeys() {
        return keys;
    }

    public void setKeys(List<Jwk> keys) {
        this.keys = keys;
    }


}
