package controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.controller.UserController;
import it.pagopa.selfcare.user.controller.response.UserInstitutionResponse;
import it.pagopa.selfcare.user.service.UserInstitutionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
@TestHTTPEndpoint(UserController.class)
public class UserControllerTest {

    @InjectMock
    private UserInstitutionService userInstitutionService;

    /**
     * Method under test: {@link UserController#getUsersEmailByInstitutionAndProduct(String)}
     */
    @Test
    @TestSecurity(user = "userJwt")
    void getUsersEmailByInstitution() {

        Mockito.when(userInstitutionService.getUsersEmailByInstitution(anyString()))
                .thenReturn(Uni.createFrom().item(List.of("test@test.it")));

        var institutionId = "institutionId";

        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("institutionId", institutionId)
                .get("/emails/{institutionId}")
                .then()
                .statusCode(200);
    }

    /**
     * Method under test: {@link UserController#retrieveUsers(String, String, List, List, List, List)} )}
     */
    @Test
    void retrieveUsersNotAuthorized() {

        var institutionId = "institutionId";

        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("institutionId", institutionId)
                .get("/institutions/{institutionId}")
                .then()
                .statusCode(401);
    }

    /**
     * Method under test: {@link UserController#retrieveUsers(String, String, List, List, List, List)} )}
     */
    @Test
    @TestSecurity(user = "userJwt")
    void retrieveUsers() {

        Mockito.when(userInstitutionService.retrieveUsers(any(), any(), any(), any(), any(), any()))
                .thenReturn(Multi.createFrom().item(new UserInstitutionResponse()));

        var institutionId = "institutionId";

        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("institutionId", institutionId)
                .get("/institutions/{institutionId}")
                .then()
                .statusCode(200);
    }
}
