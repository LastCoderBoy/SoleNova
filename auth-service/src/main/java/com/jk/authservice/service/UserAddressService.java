package com.jk.authservice.service;

import com.jk.authservice.dto.request.CreateAddressRequest;
import com.jk.authservice.dto.request.UpdateAddressRequest;
import com.jk.authservice.dto.response.UserAddressResponse;

import java.util.List;

public interface UserAddressService {
    List<UserAddressResponse> getAddresses(Long userId);

    UserAddressResponse createAddress(CreateAddressRequest request, Long userId);

    UserAddressResponse updateAddress(UpdateAddressRequest request, Long addressId, Long userId);

    void deleteAddress(Long addressId, Long userId);

    UserAddressResponse setDefaultAddress(Long addressId, Long userId);
}
