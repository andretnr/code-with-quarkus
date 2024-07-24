package io.github.nunesdev.social.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nunesdev.social.domain.models.User;
import io.github.nunesdev.social.domain.repositories.UserRepository;
import io.github.nunesdev.social.rest.dtos.CreateUserRequestDTO;
import io.quarkus.redis.client.RedisClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@QuarkusTest
class UserResourceTest {

    @InjectMocks
    UserResource userResource;

    @Mock
    UserRepository userRepository;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    RedisClient redisClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Transactional
    void testCreateUser() throws JsonProcessingException {
        // Arrange
        CreateUserRequestDTO userRequest = new CreateUserRequestDTO();
        userRequest.setName("John Doe");
        userRequest.setAge(30);

        User user = new User();
        user.setName(userRequest.getName());
        user.setAge(userRequest.getAge());
        user.setId(1L);  // Simulate an ID

        Mockito.doAnswer(invocation -> {
            User persistedUser = invocation.getArgument(0);
            persistedUser.setId(1L); // Simulate setting an ID after persist
            return null;
        }).when(userRepository).persist(any(User.class));

        String userJson = "{\"name\":\"John Doe\",\"age\":30,\"id\":1}";
        Mockito.when(objectMapper.writeValueAsString(any(User.class))).thenReturn(userJson);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(redisClient).set(anyList());

        // Act
        Response response = userResource.createUser(userRequest);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        User createdUser = (User) response.getEntity();
        assertEquals("John Doe", createdUser.getName());
        assertEquals(30, createdUser.getAge());
        assertEquals(1L, createdUser.getId());

        verify(userRepository, times(1)).persist(any(User.class));
        verify(objectMapper, times(1)).writeValueAsString(any(User.class));
        verify(redisClient, times(1)).set(any(List.class));
    }
}
