package it.pagopa.selfcare.user.controller;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.controller.response.UserInstitutionResponse;
import it.pagopa.selfcare.user.service.UserInstitutionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Authenticated
@Path("/v1/users")
@AllArgsConstructor
public class UserController {

    @Inject
    private UserInstitutionService userInstitutionService;

    @Operation(summary = "The API retrieves Users email using institution and product ids filter")
    @GET
    @Path(value = "/emails/{institutionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<String>> getUsersEmailByInstitutionAndProduct(@PathParam(value = "institutionId") String institutionId) {
        return userInstitutionService.getUsersEmailByInstitution(institutionId);
    }

    @Operation(summary = "The API retrieves users with optional filters in input as query params")
    @Path(value = "/institutions/{institutionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<UserInstitutionResponse> retrieveUsers(@PathParam(value = "institutionId") String institutionId,
                                                        @QueryParam(value = "userId") String userId,
                                                        @QueryParam(value = "roles") List<PartyRole> roles,
                                                        @QueryParam(value = "roles") List<OnboardedProductState> states,
                                                        @QueryParam(value = "products") List<String> products,
                                                        @QueryParam(value = "productRoles") List<String> productRoles) {
        return userInstitutionService.retrieveUsers(institutionId, userId, roles, states, products, productRoles);
    }
}
