package auth;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Jwk {

    @JsonProperty("kid")
    private String id;
    @JsonProperty("kty")
    private String type;
    @JsonProperty("alg")
    private String algorithm;
    @JsonProperty("use")
    private String usage;
    @JsonProperty("key_ops")
    private List<String> operations;
    @JsonProperty("x5u")
    private String certificateUrl;
    @JsonProperty("x5c")
    private List<String> certificateChain;
    @JsonProperty("x5t")
    private String certificateThumbprint;

    private Map<String, Object> additionalAttributes = new HashMap<>();

    public Jwk() {
        // used by jackson
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return type
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * @return usage
     */
    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    /**
     * @return operations
     */
    public List<String> getOperations() {
        return operations;
    }

    public void setOperations(List<String> operations) {
        this.operations = operations;
    }

    /**
     * @return certificateUrl
     */
    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    /**
     * @return certificateChain
     */
    public List<String> getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(List<String> certificateChain) {
        this.certificateChain = certificateChain;
    }

    /**
     * @return certificateThumbprint
     */
    public String getCertificateThumbprint() {
        return certificateThumbprint;
    }

    public void setCertificateThumbprint(String certificateThumbprint) {
        this.certificateThumbprint = certificateThumbprint;
    }


    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    @JsonAnySetter
    public void setAdditionalAttributes(String key, String value) {
        this.additionalAttributes.put(key, value);
    }

}
