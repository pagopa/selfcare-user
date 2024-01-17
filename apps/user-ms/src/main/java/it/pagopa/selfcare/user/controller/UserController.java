package it.pagopa.selfcare.user.controller;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.controller.response.OnboardingInfoResponse;
import it.pagopa.selfcare.user.controller.response.UserNotificationResponse;
import it.pagopa.selfcare.user.controller.response.product.UserProductsResponse;
import it.pagopa.selfcare.user.controller.response.UserResponse;
import it.pagopa.selfcare.user.controller.response.notification.UsersNotificationResponse;
import it.pagopa.selfcare.user.mapper.UserMapper;
import it.pagopa.selfcare.user.service.UserEventService;
import it.pagopa.selfcare.user.service.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.ResponseStatus;

@Authenticated
@Path("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserEventService userEventService;


    /**
     * The getInstitutionProductsInfo function returns the onboarding info for a user.
     *
     * @param userId String
     * @param institutionId String
     * @param states String[]
     *
     * @return A uni&lt;onboardinginforesponse&gt; object
     *
     */
    @Operation(summary = "returns onboarding info")
    @GET
    @Path(value = "/{userId}/institution-products")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<OnboardingInfoResponse> getInstitutionProductsInfo(@PathParam(value = "userId") String userId,
                                                                  @QueryParam(value = "institutionId") String institutionId,
                                                                  @QueryParam(value = "states") String[] states) {
        return userService.getUserInfo(userId, institutionId, states)
                .log()
                .replaceWith(new OnboardingInfoResponse());
                //.map(objects -> userMapper.toUserInfoReponse(userId, objects));
    }

    /**
     * The getUserProductsInfo function retrieves the products info and role which the user is enabled.
     *
     * @param userId String
     * @param institutionId String
     * @param states String[]
     *
     * @return A userproductsresponse object
     *
     */
    @Operation(summary = "Retrieves products info and role which the user is enabled")
    @GET
    @Path("/{userId}/products")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UserProductsResponse> getUserProductsInfo(@PathParam(value = "userId") String userId,
                                                         @QueryParam(value = "institutionId") String institutionId,
                                                         @QueryParam(value = "states") String[] states) {
        return userService.retrieveBindings(institutionId, userId, states)
                .log()
                .replaceWith(new UserProductsResponse());
                //.map(userMapper::toUserProductsResponse);
    }

    /**
     * The deleteProducts function is used to delete logically the association institution and product.
     *
     * @param userId String
     * @param institutionId String
     * @param productId String
     *
     * @return A uni&lt;void&gt;
     */
    @Operation(summary = "Delete logically the association institution and product")
    @ResponseStatus(HttpStatus.SC_NO_CONTENT)
    @DELETE
    @Path(value = "/users/{userId}/institutions/{institutionId}/products/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> deleteProducts(@PathParam(value = "userId") String userId,
                                    @PathParam(value = "institutionId") String institutionId,
                                    @PathParam(value = "productId") String productId) {
        return userService.updateUserStatus(userId, institutionId, productId, null, null, OnboardedProductState.DELETED)
                .log()
                .onItem().transformToUni((aBoolean -> Uni.createFrom().nothing()));
    }

    /**
     * The getUserInfo function retrieves a user's information given the userId and optional ProductId.
     *
     * @param userId String
     * @param institutionId String
     * @param productId String
     *
     * @return A uni&amp;lt;userresponse&amp;gt;
     *
     */
    @Operation(summary = "Retrieves user given userId and optional ProductId")
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UserResponse> getUserInfo(@PathParam(value = "userId") String userId,
                                         @QueryParam(value = "institutionId") String institutionId,
                                         @QueryParam(value = "productId") String productId) {
        return userService.retrievePerson(userId, productId, institutionId)
                .log()
                .replaceWith(new UserResponse());
               // .map(userResource -> userMapper.toUserResponse(userResource));
    }

    /**
     * The sendUpdateUserNotificationToQueue function is a service that sends notification when user data get's updated.
     *
     * @param userId String
     * @param institutionId String
     *
     * @return Uni&lt;response&gt;
     *
     */
    @Operation(summary = "Service to send notification when user data get's updated")
    @ResponseStatus(HttpStatus.SC_NO_CONTENT)
    @POST
    @Path("/{id}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> sendUpdateUserNotificationToQueue(@PathParam(value = "userId") String userId,
                                @QueryParam(value = "institutionId") String institutionId) {
        return userEventService.sendUpdateUserNotificationToQueue(userId, institutionId)
                .log();
    }

    /**
     * The getUsers function is a REST endpoint that returns all users in the database.
     * It takes two optional parameters: page and size, which are used to paginate the results.
     * The default values for these parameters are 0 and 100 respectively.
     *
     * @param page Integer
     * @param size Integer
     * @param productId String
     *
     * @return A uni&lt;usersnotificationresponse&gt; object
     *
     */
    @Operation(summary = "Retrieve all users according to optional params in input")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UsersNotificationResponse> getUsers(@QueryParam(value = "page") @DefaultValue("0") Integer page,
                                                   @QueryParam(value = "size") @DefaultValue("100") Integer size,
                                                   @QueryParam(value = "productId") String productId) {
        return userService.findPaginatedUserNotificationToSend(size, page, productId)
                .log()
                .replaceWith(new UsersNotificationResponse());
                //.map(userMapper::toUserNotificationResponse);
    }

    /**
     * The updateUserStatus function updates the status of a user's product.
     *
     * @param userId String
     * @param institutionId String
     * @param productId String
     * @param role PartyRole
     * @param productRole String
     * @param status OnboardedProductState
     *
     * @return A uni&lt;response&gt;
     *
     */
    @Operation(summary = "Update user status with optional filter for institution, product, role and productRole")
    @PUT
    @ResponseStatus(HttpStatus.SC_NO_CONTENT)
    @Path(value = "/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> updateUserStatus(@PathParam(value = "id") String userId,
                                  @QueryParam(value = "institutionId") String institutionId,
                                  @QueryParam(value = "productId") String productId,
                                  @QueryParam(value = "role") PartyRole role,
                                  @QueryParam(value = "productRole") String productRole,
                                  @QueryParam(value = "status") OnboardedProductState status) {
        log.debug("updateProductStatus - userId: {}", userId);
        return userService.updateUserStatus(userId, institutionId, productId, role, productRole, status)
                .log();
    }
}

//REQUIRED SU QUERY PARAM
//VERIFICA SUL MAP DOPO L'UNI VOID
//VERIFICA FUNZIONAMENTO OPERATORE LOG
