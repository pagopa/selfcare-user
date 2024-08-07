package it.pagopa.selfcare.user.controller;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.user.controller.request.AddUserRoleDto;
import it.pagopa.selfcare.user.controller.request.CreateUserDto;
import it.pagopa.selfcare.user.controller.response.*;
import it.pagopa.selfcare.user.controller.response.product.SearchUserDto;
import it.pagopa.selfcare.user.mapper.UserMapper;
import it.pagopa.selfcare.user.model.LoggedUser;
import it.pagopa.selfcare.user.model.UpdateUserRequest;
import it.pagopa.selfcare.user.controller.response.UserInstitutionWithActions;
import it.pagopa.selfcare.user.model.constants.OnboardedProductState;
import it.pagopa.selfcare.user.service.UserRegistryService;
import it.pagopa.selfcare.user.service.UserService;
import it.pagopa.selfcare.user.service.utils.OPERATION_TYPE;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.List;

import static it.pagopa.selfcare.user.util.GeneralUtils.formatQueryParameterList;

@Authenticated
@Tag(name = "User")
@Path("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    @Inject
    CurrentIdentityAssociation currentIdentityAssociation;

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRegistryService userRegistryService;

    @Operation(summary = "The API retrieves Users' emails using institution id and product id")
    @GET
    @Path(value = "/emails")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<String>> getUsersEmailByInstitutionAndProduct(@NotNull @QueryParam(value = "institutionId") String institutionId,
                                                                  @NotNull @QueryParam(value = "productId") String productId) {
        return userService.getUsersEmails(institutionId, productId);
    }

    /**
     * The getUserInfo function retrieves a user's information given the userId and optional ProductId.
     *
     * @param userId        String
     * @param institutionId String
     * @param productId     String
     * @return A uni&amp;lt;userresponse&amp;gt;
     */
    @Operation(summary = "Retrieves user given userId and optional ProductId")
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "User")
    @Tag(name = "external-v2")
    @Tag(name = "support")
    public Uni<UserResponse> getUserInfo(@PathParam(value = "id") String userId,
                                         @QueryParam(value = "institutionId") String institutionId,
                                         @QueryParam(value = "productId") String productId) {
        return userService.retrievePerson(userId, productId, institutionId)
                .map(user -> userMapper.toUserResponse(user, institutionId));
    }

    @Operation(summary = "Retrieves products info and role which the user is enabled")
    @GET
    @Path("/{userId}/institutions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UserInfoResponse> getUserProductsInfo(@PathParam(value = "userId") String userId,
                                                     @QueryParam(value = "institutionId") String institutionId,
                                                     @QueryParam(value = "states") String[] states) {
        return userService.retrieveBindings(institutionId, userId, states)
                .map(userMapper::toUserInfoResponse);
    }

    /**
     * getUserById function returns all the information of a user retrieved from pdv given the userId
     *
     * @param userId String
     * @return A uni UserDetailResponse
     */
    @Operation(summary = "Retrieves user's information from pdv: name, familyName, email, fiscalCode and workContacts")
    @GET
    @Path("/{id}/details")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UserDetailResponse> getUserDetailsById(@PathParam(value = "id") String userId,
                                                      @QueryParam(value = "institutionId") String institutionId,
                                                      @QueryParam(value = "field") String field) {
        return userService.getUserById(userId, institutionId, field);
    }

    @Operation(summary = "Search user by fiscalCode")
    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UserDetailResponse> searchUserByFiscalCode(@RequestBody SearchUserDto dto,
                                                          @QueryParam(value = "institutionId") String institutionId) {
        return userService.searchUserByFiscalCode(dto.getFiscalCode(), institutionId);
    }

    /**
     * The deleteProducts function is used to delete logically the association institution and product.
     *
     * @param userId        String
     * @param institutionId String
     * @param productId     String
     * @return A uni&lt;void&gt;
     */
    @Operation(summary = "Delete logically the association institution and product")
    @DELETE
    @Path(value = "/{userId}/institutions/{institutionId}/products/{productId}")
    public Uni<Void> deleteProducts(@PathParam(value = "userId") String userId,
                                    @PathParam(value = "institutionId") String institutionId,
                                    @PathParam(value = "productId") String productId) {
        return userService.deleteUserInstitutionProduct(userId, institutionId, productId);
    }

    /**
     * The updateUserStatus function updates the status of a user's product.
     *
     * @param userId        String
     * @param institutionId String
     * @param productId     String
     * @param role          PartyRole
     * @param productRole   String
     * @param status        OnboardedProductState
     * @return A uni&lt;response&gt;
     */
    @Operation(summary = "Update user status with optional filter for institution, product, role and productRole")
    @PUT
    @Path(value = "/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateUserStatus(@PathParam(value = "id") String userId,
                                          @QueryParam(value = "institutionId") String institutionId,
                                          @QueryParam(value = "productId") String productId,
                                          @QueryParam(value = "role") PartyRole role,
                                          @QueryParam(value = "productRole") String productRole,
                                          @QueryParam(value = "status") OnboardedProductState status) {
        log.debug("updateProductStatus - userId: {}", userId);
        return userService.updateUserStatusWithOptionalFilter(userId, institutionId, productId, role, productRole, status)
                .map(ignore -> Response
                        .status(HttpStatus.SC_NO_CONTENT)
                        .build());
    }

    /**
     * Retreive all the users given a list of userIds
     *
     * @param userIds List<String></String>
     */
    @Operation(
            summary = "Retrieve all users given their userIds"
    )
    @GET
    @Path("/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<UserInstitutionResponse>> findAllByIds(@QueryParam(value = "userIds") List<String> userIds) {
        return userService.findAllByIds(formatQueryParameterList(userIds));
    }

    @Operation(summary = "Retrieve all SC-User for DataLake filtered by optional productId")
    @GET
    @Path(value = "/notification")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UsersNotificationResponse> getUsers(@QueryParam(value = "page") @DefaultValue("0") Integer page,
                                                   @QueryParam(value = "size") @DefaultValue("100") Integer size,
                                                   @QueryParam(value = "productId") String productId) {
        return userService.findPaginatedUserNotificationToSend(size, page, productId)
                .map(userNotificationToSends -> {
                    UsersNotificationResponse usersNotificationResponse = new UsersNotificationResponse();
                    usersNotificationResponse.setUsers(userNotificationToSends.stream()
                            .map(userMapper::toUserNotification)
                            .toList());
                    return usersNotificationResponse;
                });
    }

    @Operation(summary = "The API retrieves paged users with optional filters in input as query params")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<UserInstitutionResponse> retrievePaginatedAndFilteredUser(@QueryParam(value = "institutionId") String institutionId,
                                                                           @QueryParam(value = "userId") String userId,
                                                                           @QueryParam(value = "roles") List<PartyRole> roles,
                                                                           @QueryParam(value = "states") List<String> states,
                                                                           @QueryParam(value = "products") List<String> products,
                                                                           @QueryParam(value = "productRoles") List<String> productRoles,
                                                                           @QueryParam(value = "page") @DefaultValue("0") Integer page,
                                                                           @QueryParam(value = "size") @DefaultValue("100") Integer size) {
        return userService.findPaginatedUserInstitutions(institutionId, userId, roles, states, products, productRoles, page, size);
    }

    /**
     * The updateUserRegistryAndSendNotification function is a service that sends notification when user data get's updated.
     *
     * @param userId        String
     * @param institutionId String
     * @return Uni&lt;response&gt;
     */
    @Operation(summary = "Service to update user in user-registry and send notification when user data gets updated")
    @ResponseStatus(HttpStatus.SC_NO_CONTENT)
    @PUT
    @Path("/{id}/user-registry")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateUserRegistryAndSendNotification(@PathParam(value = "id") String userId,
                                                               @QueryParam(value = "institutionId") String institutionId,
                                                               UpdateUserRequest updateUserRequest) {
        return userRegistryService.updateUserRegistry(updateUserRequest, userId, institutionId)
                .map(ignore -> Response
                        .status(HttpStatus.SC_NO_CONTENT)
                        .build());
    }

    /**
     * The updateUserProductStatus function is a service to update user product status.
     *
     * @param userId        String
     * @param institutionId String
     * @param productId     String
     * @param status        OnboardedProductState
     * @return Uni&lt;void&gt;
     */
    @Operation(summary = "Service to update user product status")
    @ResponseStatus(HttpStatus.SC_NO_CONTENT)
    @PUT
    @Path("/{id}/institution/{institutionId}/product/{productId}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> updateUserProductStatus(@PathParam("id") String userId,
                                             @PathParam("institutionId") String institutionId,
                                             @PathParam("productId") String productId,
                                             @QueryParam("productRole") String productRole,
                                             @NotNull @QueryParam("status") OnboardedProductState status,
                                             @Context SecurityContext ctx) {
        return readUserIdFromToken(ctx)
                .onItem().transformToUni(loggedUser -> userService.updateUserProductStatus(userId, institutionId, productId, status, productRole, loggedUser));
    }


    /**
     * The createOrUpdateByUserId function is used to update existing user adding userRole.
     *
     * @param userId  Sting
     * @param userDto CreateUserDto
     */
    @Operation(summary = "The createOrUpdateByUserId function is used to update existing user adding userRole.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "User created or updated!"),
            @APIResponse(responseCode = "201", description = "User already has the active role for that product!"),
    })
    @POST
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createOrUpdateByUserId(@PathParam("userId") String userId,
                                                @Valid AddUserRoleDto userDto,
                                                @Context SecurityContext ctx) {
        return readUserIdFromToken(ctx)
                .onItem().transformToUni(loggedUser -> userService.createOrUpdateUserByUserId(userDto, userId, loggedUser))
                .onItem().ifNotNull().transform(ignore -> Response.status(HttpStatus.SC_CREATED).build())
                .onItem().ifNull().continueWith(Response.status(HttpStatus.SC_OK).build());

    }

    /**
     * The createOrUpdateByFiscalCode function is used to create a new user or update an existing one.
     * The function takes in a CreateUserDto object, which contains the following fields:
     * - fiscalCode (String): The tax code of the user. This field is required and must be unique for each user.
     * - email (String): The email address of the user. This field is optional and can be null or empty string if not provided by caller.
     * - phoneNumber (String): The phone number of the user, including country code prefix (+39). This field is optional and can be null or empty string if not provided by
     *
     * @param userDto CreateUserDto
     */
    @APIResponses({
            @APIResponse(responseCode = "200", description = "User created or updated!"),
            @APIResponse(responseCode = "201", description = "User already has the active role for that product!"),
    })
    @Operation(summary = "The createOrUpdateByFiscalCode function is used to create a new user or update an existing one.")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createOrUpdateByFiscalCode(@Valid CreateUserDto userDto,
                                                  @Context SecurityContext ctx) {
        return readUserIdFromToken(ctx)
                .onItem().transformToUni(loggedUser -> userService.createOrUpdateUserByFiscalCode(userDto, loggedUser))
                .map(response -> Response
                        .status(OPERATION_TYPE.CREATED_OR_UPDATED.equals(response.getOperationType()) ? HttpStatus.SC_CREATED : HttpStatus.SC_OK)
                        .entity(response.getUserId()).build());
    }

    /**
     * The retrieveUsers function is used to retrieve a list of users from the UserInstitution collection and userRegistry.
     *
     * @param institutionId The ID of the institution, passed as a path parameter in the URL.
     * @param userId        The ID of the user, passed as a path parameter in the URL.
     * @param personId      The ID of the person, passed as a query parameter in the URL.
     * @param roles         A list of roles, passed as a query parameter in the URL.
     * @param states        A list of states, passed as a query parameter in the URL.
     * @param products      A list of products, passed as a query parameter in the URL.
     * @param productRoles  A list of product roles, passed as a query parameter in the URL.
     * @return A stream of UserDataResponse objects containing the requested user data.
     */
    @Operation(summary = "The retrieveUsers function is used to retrieve a list of users from the UserInstitution collection and userRegistry.\n" +
            "At first it try to retrieve a UserInstitution document associated with a logged user (admin)\n" +
            "If this userInstitution object is not null, so user has AdminRole, it try to retriew the userInstitutions filtered by given institutionId, roles, states, products and productRoles\n" +
            "and optional given personId, otherwise it do the same query using the logged user id instead of personId.\n" +
            "After that it retrieve personal user data, foreach userId retrieved, from userRegistry and return a stream of UserDataResponse objects containing the requested user data.")
    @GET
    @Path(value = "/{userId}/institution/{institutionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<UserDataResponse> retrieveUsers(@PathParam(value = "institutionId") String institutionId,
                                                 @PathParam(value = "userId") String userId,
                                                 @QueryParam(value = "personId") String personId,
                                                 @QueryParam(value = "roles") List<String> roles,
                                                 @QueryParam(value = "states") List<String> states,
                                                 @QueryParam(value = "products") List<String> products,
                                                 @QueryParam(value = "productRoles") List<String> productRoles) {

        return userService.retrieveUsersData(institutionId, personId, roles, states, products, productRoles, userId);
    }

    @Operation(summary = "Retrieves userInstitution data with list of actions permitted for each user's product")
    @GET
    @Path("/{userId}/institutions/{institutionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UserInstitutionWithActions> getUserInstitutionWithPermission(@PathParam(value = "userId") String userId,
                                                                            @PathParam(value = "institutionId") String institutionId,
                                                                            @QueryParam(value = "productId") String productId) {
        return userService.getUserInstitutionWithPermission(userId, institutionId, productId);
    }

    private Uni<LoggedUser> readUserIdFromToken(SecurityContext ctx) {
        return currentIdentityAssociation.getDeferredIdentity()
                .onItem().transformToUni(identity -> {
                    if (ctx.getUserPrincipal() == null || !ctx.getUserPrincipal().getName().equals(identity.getPrincipal().getName())) {
                        return Uni.createFrom().failure(new InternalServerErrorException("Principal and JsonWebToken names do not match"));
                    }

                    if (identity.getPrincipal() instanceof DefaultJWTCallerPrincipal jwtCallerPrincipal) {
                        String uid = jwtCallerPrincipal.getClaim("uid");
                        String familyName = jwtCallerPrincipal.getClaim("family_name");
                        String name = jwtCallerPrincipal.getClaim("name");
                        return Uni.createFrom().item(
                                LoggedUser.builder()
                                        .uid(uid)
                                        .familyName(familyName)
                                        .name(name)
                                        .build()
                        );
                    }
                    return Uni.createFrom().nullItem();
                });
    }
}

