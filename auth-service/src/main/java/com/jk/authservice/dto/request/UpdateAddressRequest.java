package com.jk.authservice.dto.request;

import com.jk.authservice.enums.AddressType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAddressRequest {

    private AddressType addressType;

    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,20}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 255)
    private String streetLine1;

    @Size(max = 255)
    private String streetLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Size(min = 2, max = 2, message = "Country must be ISO 3166-1 alpha-2 code")
    private String country;

    private Boolean isDefault;

    @AssertTrue(message = "At least one field must be provided for update")
    public boolean isAtLeastOneFieldProvided() {
        return addressType != null || fullName != null || phoneNumber != null ||
                streetLine1 != null || streetLine2 != null || city != null ||
                state != null || postalCode != null || country != null || isDefault != null;
    }
}
