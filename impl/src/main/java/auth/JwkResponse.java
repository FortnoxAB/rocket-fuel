package auth;

import java.util.List;

public class JwkResponse {

    public JwkResponse() {
        // used by jackson
    }

    private List<Jwk> keys;


    public List<Jwk> getKeys() {
        return keys;
    }

    public void setKeys(List<Jwk> keys) {
        this.keys = keys;
    }


}
