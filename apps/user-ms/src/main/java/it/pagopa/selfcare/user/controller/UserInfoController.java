package it.pagopa.selfcare.user.controller;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.controller.response.UsersNotificationResponse;
import it.pagopa.selfcare.user.service.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Authenticated
@Path("/user-info")
@RequiredArgsConstructor
@Slf4j
public class UserInfoController {

    private final UserService userService;

    @Operation(summary = "Retrieve all SC-User for DataLake filtered by optional productId")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<UsersNotificationResponse> getUserInfo(@QueryParam(value = "page") @DefaultValue("0") Integer page,
                                                      @QueryParam(value = "size") @DefaultValue("100") Integer size) {
        return userService.findPaginatedUserNotificationToSend(size, page)
                .map(userNotificationToSends -> {
                    UsersNotificationResponse usersNotificationResponse = new UsersNotificationResponse();
                    usersNotificationResponse.setUsers(userNotificationToSends.stream()
                            .map(userMapper::toUserNotification)
                            .toList());
                    return usersNotificationResponse;
                });
    }
}
