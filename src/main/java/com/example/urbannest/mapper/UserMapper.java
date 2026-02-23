package com.example.urbannest.mapper;

import com.example.urbannest.dto.Responses.UserResponse;
import com.example.urbannest.model.User;
import com.example.urbannest.util.EncryptionUtil;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "nid", ignore = true)
    UserResponse toUserResponse(User user, @Context String nidEncryptionKey);

    @AfterMapping
    default void decryptNid(User user, @MappingTarget UserResponse response, @Context String nidEncryptionKey) {
        if (user.getNidEncrypted() != null) {
            response.setNid(EncryptionUtil.decrypt(user.getNidEncrypted(), nidEncryptionKey));
        }
    }
}
