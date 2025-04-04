package utc.englishlearning.Encybara.domain.response.auth;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Getter
@Setter
public class ResUpdateAdmin {
    private long id;
    private String email;
    private String password;
    private String name;
    private SpecialFieldEnum field;
    private Instant createdAt;
    private RoleUser role;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUser {
        private long id;
        private String name;
    }
}
