package io.github.nunesdev.social.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nunesdev.social.domain.models.User;
import io.github.nunesdev.social.domain.repositories.UserRepository;
import io.github.nunesdev.social.rest.dtos.CreateUserRequestDTO;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.redis.client.RedisClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.Objects;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserRepository userRepository;

    private final RedisClient redisClient;

    private final ObjectMapper objectMapper;

    @Inject
    public UserResource(UserRepository userRepository, RedisClient redisClient, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.redisClient = redisClient;
        this.objectMapper = objectMapper;
    }

    @POST
    @Transactional
    public Response createUser(CreateUserRequestDTO userRequest) throws JsonProcessingException {
        User user = new User();
        user.setName(userRequest.getName());
        user.setAge(userRequest.getAge());
        userRepository.persist(user);
        String userJson = objectMapper.writeValueAsString(user);
        redisClient.set(Arrays.asList(user.getId().toString(), userJson));
       // redisClient.set(Arrays.asList(user.getId().toString(), user.toString()));
        return Response.ok(user).build();
    }

    @GET
    public Response listAllUsers(){
        PanacheQuery<User> query = userRepository.findAll();
        return Response.ok(query.list()).build();
    }

    @GET
    @Path("/redis/{id}")
    public Response listAllRedisUsers(@PathParam("id") Long id) throws JsonProcessingException {
        io.vertx.redis.client.Response response = redisClient.get(id.toString());
        if(Objects.isNull(response)){
            return Response.noContent().build();
        }
        User user = objectMapper.readValue(response.toString(), User.class);
        return Response.ok(user).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
     public Response deleteUser(@PathParam("id") Long id) {
        User user = userRepository.findById(id);
        if(Objects.isNull(user)){
            return Response.noContent().build();
        }
        userRepository.delete(user);
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequestDTO userRequest){

        User user = userRepository.findById(id);
       if(Objects.isNull(user)){
           return Response.status(Response.Status.NOT_FOUND).build();
       }
        user.setName(userRequest.getName());
        user.setAge(userRequest.getAge());
        userRepository.persist(user);

        return Response.ok(user).build();
    }

}
