package it.pagopa.selfcare.user.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.service.UserInfoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyInt;

@QuarkusTest
@TestHTTPEndpoint(UserInfoController.class)
class UserInfoControllerTest {

    @InjectMock
    private UserInfoService userInfoService;

    /**
     * Method under test: {@link UserInfoController#updateUsersEmails(Integer, Integer)}}
     */
    @Test
    @TestSecurity(user = "userJwt")
    void updateUsersEmails() {

        int size = 1, page = 1;

        Mockito.when(userInfoService.updateUsersEmails(anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().nullItem());

        given()
                .when()
                .contentType(ContentType.JSON)
                .get("?size=" + size +"&page=" + page)
                .then()
                .statusCode(204);
    }

    /**
     * Method under test: {@link UserInfoController#updateUsersEmails(Integer, Integer)}}
     */
    @Test
    void updateUsersEmailsNotAuthorized() {

        given().when()
                .contentType(ContentType.JSON)
                .queryParam("page", 0)
                .queryParam("size", 100)
                .get()
                .then()
                .statusCode(401);
    }

}
