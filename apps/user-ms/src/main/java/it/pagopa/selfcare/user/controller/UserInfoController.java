package it.pagopa.selfcare.user.controller;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.service.UserInfoService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

import static it.pagopa.selfcare.user.util.GeneralUtils.formatQueryParameterList;

@Authenticated
@Path("/user-info")
@RequiredArgsConstructor
@Slf4j
public class UserInfoController {

    private final UserInfoService userInfoService;

    @Operation(summary = "Update users' workContacts in PDV using a random uuid as key and storing it into UserInstitution collection")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Void> updateUsersEmails(@QueryParam(value = "userIds") List<String> userIds,
                                       @QueryParam(value = "page") @DefaultValue("0") Integer page,
                                       @QueryParam(value = "size") @DefaultValue("100") Integer size) {
        return userInfoService.updateUsersEmails(formatQueryParameterList(userIds), size, page);
    }
}
