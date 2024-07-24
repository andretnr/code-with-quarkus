package io.github.nunesdev.social.rest.dtos;

import io.github.nunesdev.social.domain.models.User;

public class RedisResponseDTO {
        private User value;

    public RedisResponseDTO(User value) {
        this.value = value;
    }

    public User getValue() {
        return value;
    }

    public void setValue(User value) {
        this.value = value;
    }
}
