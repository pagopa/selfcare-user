package it.pagopa.selfcare.user.controller;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constants.RelationshipState;
import it.pagopa.selfcare.user.controller.response.OnboardingInfoResponse;
import it.pagopa.selfcare.user.controller.response.UserProductsResponse;
import it.pagopa.selfcare.user.controller.response.UserResponse;
import it.pagopa.selfcare.user.controller.response.UsersNotificationResponse;
import it.pagopa.selfcare.user.mapper.UserMapper;
import it.pagopa.selfcare.user.service.UserEventService;
import it.pagopa.selfcare.user.service.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Authenticated
@Path("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserEventService userEventService;

    @Operation(summary = "")
    @GET
    @Path(value = "/{userId}/institution-products")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<OnboardingInfoResponse> getInstitutionProductsInfo(@PathParam(value = "userId") String userId,
                                                                  @QueryParam(value = "institutionId") String institutionId,
                                                                  @QueryParam(value = "states") String[] states) {
        return userService.getUserInfo(userId, institutionId, states)
                .log()
                .map(objects -> userMapper.toUserInfoReponse(userId, objects));
    }

    @GET
    @Path("/{userId}/products")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UserProductsResponse> getUserProductsInfo(@PathParam(value = "userId") String userId,
                                                         @QueryParam(value = "institutionId") String institutionId,
                                                         @QueryParam(value = "states") String[] states) {
        return userService.retrieveBindings(institutionId, userId, states, null)
                .log()
                .map(objects -> userMapper.toUserProductsResponse(userId, objects));
    }

    @Operation(summary = "The API retrieves paged onboarding using optional filter, order by descending creation date")
    @DELETE
    @Path(value = "/users/{userId}/institutions/{institutionId}/products/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> deleteProducts(@PathParam(value = "userId") String userId,
                                    @PathParam(value = "institituonId") String institutionId,
                                    @PathParam(value = "productId") String productId) {
        return userService.updateUserStatus(userId, institutionId, productId, null, null, RelationshipState.DELETED)
                .log()
                .onItem().transformToUni((aBoolean -> Uni.createFrom().nothing()));
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UserResponse> getUserInfo(@PathParam(value = "userId") String userId,
                                         @QueryParam(value = "institutionId") String institutionId,
                                         @QueryParam(value = "productId") String productId) {
        return userService.retrievePerson(userId, productId, institutionId)
                .log()
                .map(o -> userMapper.toUserResponse(o, institutionId));
    }

    @POST
    @Path("/{id}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateUser(@PathParam(value = "userId") String userId,
                                @QueryParam(value = "institutionId") String institutionId) {
        return userEventService.sendUpdateUserNotificationToQueue(userId, institutionId)
                .log()
                .map(ignore -> Response
                        .status(HttpStatus.SC_NO_CONTENT)
                        .build());
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UsersNotificationResponse> getUsers(@QueryParam(value = "page") @DefaultValue("0") Integer page,
                                                   @QueryParam(value = "size") @DefaultValue("100") Integer size,
                                                   @QueryParam(value = "productId") String productId) {
        return userService.findAll(size, page, productId)
                .log()
                .map(userMapper::toUserNotificationResponse);
    }

    @PUT
    @Path(value = "/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getUsers(@PathParam(value = "id") String userId,
                                  @QueryParam(value = "institutionId") String institutionId,
                                  @QueryParam(value = "productId") String productId,
                                  @QueryParam(value = "role") PartyRole role,
                                  @QueryParam(value = "productRole") String productRole,
                                  @QueryParam(value = "status") RelationshipState status) {
        return userService.updateUserStatus(userId, institutionId, productId, role, productRole, status)
                .map(ignore -> Response
                        .status(HttpStatus.SC_NO_CONTENT)
                        .build());
    }

}
