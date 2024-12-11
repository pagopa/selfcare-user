package it.pagopa.selfcare.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import it.pagopa.selfcare.cucumber.dao.UserGroupRepository;
import it.pagopa.selfcare.cucumber.model.UserGroupEntity;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UpdateUserGroupSteps extends UserGroupSteps {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    List<String> userGroupsIds = List.of("6759f8df78b6af202b222d29", "6759f8df78b6af202b222d2a","6759f8df78b6af202b222d2b");

    @Given("I retrieve to update Group a group Id for product {string} and institution {string}")
    public void iRetrieveAGroupIdForProductAndInstitution(String productId, String institutionId) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .queryParams("productId", productId, "institutionId", institutionId)
                .when()
                .get("/v1/user-groups")
                .then()
                .extract();

        status = response.statusCode();
        if(status == 200) {
            userGroupEntityResponsePage = response.body().as(new TypeRef<>() {});
            if(Objects.nonNull(userGroupEntityResponsePage) && !userGroupEntityResponsePage.getContent().isEmpty()) {
                userGroupEntityResponse = userGroupEntityResponsePage.getContent().get(0);
            }
            userGroupId = userGroupEntityResponse.getId();
        }else {
            errorMessage = response.body().asString();
        }
    }

    @Then("I should receive a response with status code {int}")
    public void i_should_receive_a_response_with_status_code(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, status);
    }

    // Step per verificare un messaggio di errore nella risposta
    @Then("the response of update operation should contain an error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {
        String[] errorMessageArray = expectedErrorMessage.split(",");
        Arrays.stream(errorMessageArray).forEach(s -> Assertions.assertTrue(errorMessage.contains(s)));
    }


    @When("I send a POST request to suspendAPI with retrieved groupId")
    public void iSendAPOSTRequestToSuspendAPIWithRetrieveGroupId() {
        callSuspendApi();
    }


    @When("I send a POST request to suspendAPI without groupId")
    public void iSendAPOSTRequestToSuspendAPIWithoutGroupId() {
        callSuspendApi();
    }


    @When("I send a POST request to suspendAPI with not existent groupId")
    public void iSendAPOSTRequestToSuspendAPIWithNonExistentGroupId() {
        callSuspendApi();
    }

    private void callSuspendApi() {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/v1/user-groups/" + this.userGroupId + "/suspend")
                .then()
                .extract();

        this.status = response.statusCode();
        if(status != 204) {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a POST request to deleteAPI with retrieve groupId")
    public void iSendAPOSTRequestToDeleteAPIWithRetrieveGroupId() {
        callDeleteApi();
    }

    @When("I send a POST request to deleteAPI with not existent groupId")
    public void iSendAPOSTRequestToDeleteAPIWithNonExistentGroupId() {
        callDeleteApi();
    }

    private void callDeleteApi() {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/v1/user-groups/" + userGroupId)
                .then()
                .extract();

        status = response.statusCode();
        if(status != 204) {
            errorMessage = response.body().asString();
        }
    }


    @When("I send a POST request to activateAPI with retrieve groupId")
    public void iSendAPOSTRequestToActivateAPIWithRetrieveGroupId() {
        callActivateApi();
    }

    @When("I send a POST request to activateAPI with not existent groupId")
    public void iSendAPOSTRequestToActivateAPIWithNotExistentGroupId() {
        callActivateApi();
    }

    @When("I send a POST request to activateAPI without groupId")
    public void iSendAPOSTRequestToActivateAPIWithoutGroupId() {
        callActivateApi();
    }

    private void callActivateApi() {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/v1/user-groups/" + userGroupId + "/activate")
                .then()
                .extract();

        status = response.statusCode();
        if(status != 204) {
            errorMessage = response.body().asString();
        }
    }


    @And("I have data to update:")
    public void iHaveDataToUpdate(List<UserGroupEntity> userGroupEntityList) {
        if (userGroupEntityList != null && userGroupEntityList.size() == 1)
            this.userGroupDetails = userGroupEntityList.get(0);
    }

    @When("I send a PUT request to updateAPI with the retrieved group data")
    public void iSendAPUTRequestToUpdateAPIWithTheRetrievedGroupData() {
        callUpdateApi();
    }

    @When("I send a PUT request to updateAPI with the group data")
    public void iSendAPUTRequestToUpdateAPIWithTheGroupData() {
        callUpdateApi();
    }

    private void callUpdateApi() {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(userGroupDetails)
                .when()
                .put("/v1/user-groups/" + userGroupId)
                .then()
                .extract();

        status = response.statusCode();
        if(status == 201) {
            userGroupEntityResponse = response.body().as(UserGroupEntity.class);
            userGroupId = userGroupEntityResponse.getId();
        }else {
            errorMessage = response.body().asString();
        }
    }

    @Given("I have a valid group ID {string}")
    public void i_have_a_valid_group_ID(String validGroupId) {
        userGroupId = validGroupId;
    }

    @Given("I have a non-existent group ID {string}")
    public void i_have_a_non_existent_group_ID(String nonExistentGroupId) {
        userGroupId = nonExistentGroupId;
    }

    @And("the retrieved group should be changed status to {string}")
    public void theRetrievedGroupShouldBeChangedStatusTo(String changedStatus) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/v1/user-groups/" + userGroupId)
                .then()
                .extract();

        status = response.statusCode();
        if (status == 200) {
            userGroupEntityResponse = response.body().as(new TypeRef<>() {
            });
            Assertions.assertEquals(changedStatus, userGroupEntityResponse.getStatus().name());
            Assertions.assertNotNull(userGroupEntityResponse.getModifiedAt());
            Assertions.assertNotNull(userGroupEntityResponse.getModifiedBy());
        }
    }

    @And("the retrieved group should be updated")
    public void theRetrievedGroupShouldBeUpdated() {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/v1/user-groups/" + userGroupId)
                .then()
                .extract();

        status = response.statusCode();
        if (status == 200) {
            updatedUserGroupEntity = response.body().as(new TypeRef<>() {
            });
            Assertions.assertNotEquals(userGroupEntityResponse.getName(), updatedUserGroupEntity.getName());
            Assertions.assertNotEquals(userGroupEntityResponse.getDescription(), updatedUserGroupEntity.getDescription());
            Assertions.assertNotEquals(userGroupEntityResponse.getMembers(), updatedUserGroupEntity.getMembers());
            Assertions.assertNotNull(updatedUserGroupEntity.getModifiedAt());
            Assertions.assertNotNull(updatedUserGroupEntity.getModifiedBy());
        }
    }

    @Before("@FirstUpdateScenario")
    public void beforeFeature() throws IOException {
        List<UserGroupEntity> groupsToInsert = objectMapper.readValue(new File("src/test/resources/dataPopulation/groupEntities.json"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserGroupEntity.class));
        userGroupRepository.insert(groupsToInsert);
    }

    @After("@LastUpdateScenario")
    public void afterFeature() {
        userGroupRepository.deleteAllById(userGroupsIds);
    }

}
