package vn.iotstar.models;

import vn.iotstar.entity.User;

import java.util.Date;

public record UserResponse(
        Integer id,
        String fullName,
        String email,
        String images,
        Date createdAt,
        Date updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getImages(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}