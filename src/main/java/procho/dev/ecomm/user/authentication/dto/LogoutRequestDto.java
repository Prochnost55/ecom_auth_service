package procho.dev.ecomm.user.authentication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequestDto {
    private String token;
    private String userId;
}
