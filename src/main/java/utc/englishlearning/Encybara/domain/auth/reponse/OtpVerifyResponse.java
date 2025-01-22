package utc.englishlearning.Encybara.domain.auth.reponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyResponse {
    private String message;
    private String token; // Chỉ sử dụng nếu type là "forgotPassword"

    public OtpVerifyResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }
}
