package com.jk.authservice.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name contains invalid characters")
    private String firstName;

    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name contains invalid characters")
    private String lastName;

    @AssertTrue(message = "At least one field must be provided for update")
    public boolean isAtLeastOneFieldProvided() {
        return firstName != null || lastName != null ;
    }
}
