package auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class JwkTest {

    @Test
    public void shouldBeAbleToDeserializeFromJson() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();

        final String json = "{\n" +
                "      \"kid\": \"7d680d8c70d44e947133cbd499ebc1a61c3d5abc\",\n" +
                "      \"e\": \"AQAB\",\n" +
                "      \"kty\": \"RSA\",\n" +
                "      \"alg\": \"RS256\",\n" +
                "      \"n\": \"2K7epoJWl_B68lRUi1txaa0kEuIK4WHiHpi1yC4kPyu48d046yLlrwuvbQMbog2YTOZdVoG1" +
                "D4zlWKHuVY00O80U1ocFmBl3fKVrUMakvHru0C0mAcEUQo7ItyEX7rpOVYtxlrVk6G8PY4EK61EB-Xe35P0zb2A" +
                "MZn7Tvm9-tLcccqYlrYBO4SWOwd5uBSqc_WcNJXgnQ-9sYEZ0JUMhKZelEMrpX72hslmduiz-LMsXCnbS7jDGcU" +
                "uSjHXVLM9tb1SQynx5Xz9xyGeN4rQLnFIKvgwpiqnvLpbMo6grhJwrz67d1X6MwpKtAcqZ2V2v4rQsjbblNH7Gz" +
                "F8ZsfOaqw\",\n" +
                "      \"use\": \"sig\"\n" +
                "    }";
        final Jwk jwk = objectMapper.readValue(json, Jwk.class);

        assertThat(jwk).isNotNull();
        assertThat(jwk.getId()).isEqualTo("7d680d8c70d44e947133cbd499ebc1a61c3d5abc");
        assertThat(jwk.getAlgorithm()).isEqualTo("RS256");
        assertThat(jwk.getUsage()).isEqualTo("sig");

        Map<String, Object> additionalAttributes = jwk.getAdditionalAttributes();
        assertThat(additionalAttributes.get("e")).isEqualTo("AQAB");
        assertThat((String) additionalAttributes.get("n")).contains("bMo6grhJwrz67d1X6MwpKtAcqZ2V2v");
    }

}
