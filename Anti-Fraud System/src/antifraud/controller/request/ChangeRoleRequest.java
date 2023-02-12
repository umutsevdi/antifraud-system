package antifraud.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Data
public class ChangeRoleRequest {
    @NotEmpty
    private String username;
    @NotEmpty
    private String role;
}
