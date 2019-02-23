package auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Jwk {

    private String kid;
    private String kty;
    private String e;
    private String n;

    private String alg;
    private String use;
    private List<String> key_ops;
    private String x5u;
    private List<String> x5c;
    private String x5t;
    private Map<String, Object> additionalAttributes = new HashMap<>();

    public Jwk() {

    }

    /**
     * @return id
     */
    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * @return type
     */
    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    /**
     * @return algorithm
     */
    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    /**
     * @return usage
     */
    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    /**
     * @return operations
     */
    public List<String> getKey_ops() {
        return key_ops;
    }

    public void setKey_ops(List<String> key_ops) {
        this.key_ops = key_ops;
    }

    /**
     * @return x5u
     */
    public String getX5u() {
        return x5u;
    }

    public void setX5u(String x5u) {
        this.x5u = x5u;
    }

    /**
     * @return certificateChain
     */
    public List<String> getX5c() {
        return x5c;
    }

    public void setX5c(List<String> x5c) {
        this.x5c = x5c;
    }

    /**
     * @return certificateThumbprint
     */
    public String getX5t() {
        return x5t;
    }

    public void setX5t(String x5t) {
        this.x5t = x5t;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        additionalAttributes.put("e", e);
        this.e = e;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        additionalAttributes.put("n", n);
        this.n = n;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        // this.additionalAttributes = additionalAttributes;
    }

}
