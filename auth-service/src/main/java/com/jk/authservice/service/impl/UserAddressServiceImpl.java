package com.jk.authservice.service.impl;

import com.jk.authservice.dto.request.CreateAddressRequest;
import com.jk.authservice.dto.request.UpdateAddressRequest;
import com.jk.authservice.dto.response.UserAddressResponse;
import com.jk.authservice.entity.User;
import com.jk.authservice.entity.UserAddress;
import com.jk.authservice.repository.UserAddressRepository;
import com.jk.authservice.service.UserAddressService;
import com.jk.authservice.service.UserProfileService;
import com.jk.commonlibrary.exception.ResourceNotFoundException;
import com.jk.commonlibrary.exception.UnauthorizedException;
import com.jk.commonlibrary.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.jk.authservice.mapper.UserMapper.mapToListOfUserAddressResponses;
import static com.jk.authservice.mapper.UserMapper.mapToUserAddressResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserProfileService userProfileService;
    private final UserAddressRepository userAddressRepository;

    @Transactional(readOnly = true)
    @Override
    public List<UserAddressResponse> getAddresses(Long userId) {
        List<UserAddress> userAddressList = userAddressRepository.findByUserId(userId);
        if(userAddressList.isEmpty()){
            log.info("[ADDRESS-SERVICE] No addresses found for user ID: {}", userId);
            return List.of();
        }
        return mapToListOfUserAddressResponses(userAddressList);
    }

    @Transactional
    @Override
    public UserAddressResponse createAddress(CreateAddressRequest request, Long userId) {
        User userEntity = userProfileService.findUserById(userId); // might throw ResourceNotFoundException

        UserAddress userAddress = UserAddress.builder()
                .user(userEntity) // Orphan removal will handle the association
                .addressType(request.getAddressType())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .streetLine1(request.getStreetLine1())
                .streetLine2(request.getStreetLine2()) // might be null
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();
        userAddress = userAddressRepository.save(userAddress);

        log.info("[ADDRESS-SERVICE] Created address with ID {} for user ID: {}", userAddress.getId(), userId);

        return mapToUserAddressResponse(userAddress);
    }

    @Transactional
    @Override
    public UserAddressResponse updateAddress(UpdateAddressRequest request, Long addressId, Long userId) {
        UserAddress userAddress = findAddressById(addressId);

        User userEntity = userProfileService.findUserById(userId);

        if(!userAddress.getUser().getId().equals(userEntity.getId())){
            throw new UnauthorizedException("You are not authorized to update this address");
        }

        if(!request.isAtLeastOneFieldProvided()){
            throw new ValidationException("At least one field must be provided for update");
        }

        updateFields(userAddress, request);
        userAddress = userAddressRepository.save(userAddress);

        log.info("[ADDRESS-SERVICE] Updated address with ID {} for user ID: {}", userAddress.getId(), userId);

        return mapToUserAddressResponse(userAddress);
    }

    @Transactional
    @Override
    public void deleteAddress(Long addressId, Long userId) {
        UserAddress userAddress = findAddressById(addressId);

        User userEntity = userProfileService.findUserById(userId);
        if(!userAddress.getUser().getId().equals(userEntity.getId())){
            throw new UnauthorizedException("You are not authorized to delete this address");
        }
        userAddressRepository.delete(userAddress);

    }

    @Transactional
    @Override
    public UserAddressResponse setDefaultAddress(Long addressId, Long userId) {
        UserAddress userAddress = findAddressById(addressId);
        User userEntity = userProfileService.findUserById(userId);
        if(!userAddress.getUser().getId().equals(userEntity.getId())){
            throw new UnauthorizedException("You are not authorized to set this address as default");
        }
        userAddress.setIsDefault(true);
        userAddress = userAddressRepository.save(userAddress);

        log.info("[ADDRESS-SERVICE] Set address with ID {} as default for user ID: {}", userAddress.getId(), userId);

        return mapToUserAddressResponse(userAddress);
    }


    // ====================================================================
    //                           HELPER METHODS
    // ====================================================================

    private void updateFields(UserAddress oldAddress, UpdateAddressRequest request){
        if(request.getAddressType() != null){
            oldAddress.setAddressType(request.getAddressType());
        }
        if(request.getFullName() != null){
            oldAddress.setFullName(request.getFullName());
        }
        if(request.getPhoneNumber() != null){
            oldAddress.setPhoneNumber(request.getPhoneNumber());
        }
        if(request.getStreetLine1() != null){
            oldAddress.setStreetLine1(request.getStreetLine1());
        }
        if(request.getStreetLine2() != null){
            oldAddress.setStreetLine2(request.getStreetLine2());
        }
        if(request.getCity() != null){
            oldAddress.setCity(request.getCity());
        }
        if(request.getState() != null){
            oldAddress.setState(request.getState());
        }
        if(request.getPostalCode() != null){
            oldAddress.setPostalCode(request.getPostalCode());
        }
        if(request.getCountry() != null){
            oldAddress.setCountry(request.getCountry());
        }
        if(request.getIsDefault() != null){
            oldAddress.setIsDefault(request.getIsDefault());
        }
        log.info("[ADDRESS-SERVICE] Updated address fields for address ID: {}", oldAddress.getId());
    }

    private UserAddress findAddressById(Long addressId){
        return userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
    }
}
