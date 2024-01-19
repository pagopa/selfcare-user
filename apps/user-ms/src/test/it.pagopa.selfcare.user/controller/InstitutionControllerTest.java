package controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Multi;
import it.pagopa.selfcare.user.controller.InstitutionController;
import it.pagopa.selfcare.user.controller.response.UserProductResponse;
import it.pagopa.selfcare.user.service.UserInstitutionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.anyString;

@QuarkusTest
@TestHTTPEndpoint(InstitutionController.class)
class InstitutionControllerTest {

    @InjectMock
    private UserInstitutionService userInstitutionService;

    /**
     * Method under test: {@link InstitutionController#getInstitutionUsers(String)}
     */
    @Test
    void getInstitutionUsersNotAuthorized() {

        var institutionId = "institutionId";

        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("institutionId", institutionId)
                .get("/{institutionId}/users")
                .then()
                .statusCode(401);
    }

    /**
     * Method under test: {@link InstitutionController#getInstitutionUsers(String)}
     */
    @Test
    @TestSecurity(user = "userJwt")
    void getInstitutionUsers() {

        Mockito.when(userInstitutionService.getUserProductsByInstitution(anyString()))
                .thenReturn(Multi.createFrom().item(new UserProductResponse()));

        var institutionId = "institutionId";

        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("institutionId", institutionId)
                .get("/{institutionId}/users")
                .then()
                .statusCode(200);
    }
}
