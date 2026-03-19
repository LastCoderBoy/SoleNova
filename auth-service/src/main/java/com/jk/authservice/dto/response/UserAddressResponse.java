package com.jk.authservice.dto.response;

import com.jk.authservice.enums.AddressType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressResponse {

    private Long id;
    private AddressType addressType;
    private String fullName;
    private String phoneNumber;
    private String streetLine1;
    private String streetLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
}
