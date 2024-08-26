package it.pagopa.selfcare.user.controller;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.controller.request.UpdateDescriptionDto;
import it.pagopa.selfcare.user.controller.response.UserInstitutionResponse;
import it.pagopa.selfcare.user.controller.response.UserProductResponse;
import it.pagopa.selfcare.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.openapi.quarkus.user_registry_json.model.Problem;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Authenticated
@Path("/institutions")
@Tag(name = "Institution")
@AllArgsConstructor
public class InstitutionController {

    private final UserService userService;

    @Operation(summary = "The API retrieves user's info including details of roles on products")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserProductResponse.class), mediaType = "application/json")),
            @APIResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = Problem.class), mediaType = "application/problem+json")),
            @APIResponse(responseCode = "401", description = "Not Authorized", content = @Content(schema = @Schema(implementation = Problem.class), mediaType = "application/problem+json")),
            @APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Problem.class), mediaType = "application/problem+json")),
            @APIResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class), mediaType = "application/problem+json"))
    })
    @GET
    @Path(value = "/{institutionId}/users")
    @Tag(name = "support")
    @Tag(name = "support-pnpg")
    @Tag(name = "Institution")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<UserProductResponse> getInstitutionUsers(@PathParam(value = "institutionId") String institutionId) {
        return userService.getUserProductsByInstitution(institutionId);
    }

    @Operation(summary = "The API retrieves users with optional filters in input as query params")
    @GET
    @Path(value = "/{institutionId}/user-institutions")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<UserInstitutionResponse> retrieveUsers(@PathParam(value = "institutionId") String institutionId,
                                                        @QueryParam(value = "userId") String userId,
                                                        @QueryParam(value = "roles") List<String> roles,
                                                        @QueryParam(value = "states") List<String> states,
                                                        @QueryParam(value = "products") List<String> products,
                                                        @QueryParam(value = "productRoles") List<String> productRoles) {
        return userService.findAllUserInstitutions(institutionId, userId, roles, states, products, productRoles);
    }

    @Operation(summary = "The API updates user's onboarded product with createdAt passed in input")
    @PUT
    @Path(value = "/{institutionId}/products/{productId}/createdAt")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateUserProductCreatedAt(@PathParam(value = "institutionId") String institutionId,
                                                    @PathParam(value = "productId") String productId,
                                                    @NotNull @QueryParam(value = "userIds") List<String> userIds,
                                                    @NotNull @QueryParam(value = "createdAt") LocalDateTime createdAt) {
        return userService.updateUserProductCreatedAt(institutionId, userIds, productId, createdAt)
                .map(ignore -> Response
                        .status(HttpStatus.SC_NO_CONTENT)
                        .build());
    }

    @Operation(summary = "The API updates the description in all occurrences of userInstitution, given a certain institutionId.")
    @PUT
    @Path(value = "/{institutionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateInstitutionDescription(@PathParam(value = "institutionId") String institutionId,
                                                      @Valid UpdateDescriptionDto descriptionDto) {
        return userService.updateInstitutionDescription(institutionId, descriptionDto)
                .map(ignore -> Response
                        .status(HttpStatus.SC_NO_CONTENT)
                        .build());
    }
}
