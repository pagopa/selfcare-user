package it.pagopa.selfcare.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import it.pagopa.selfcare.cucumber.dao.UserGroupRepository;
import it.pagopa.selfcare.cucumber.model.UserGroupEntity;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UserGroupMemberSteps extends UserGroupSteps {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    List<String> userGroupsIds = List.of("6759f8df78b6af202b222d29", "6759f8df78b6af202b222d2a","6759f8df78b6af202b222d2b");

    @Given("I have a valid group ID {string} and a valid member ID {string}")
    public void i_have_a_valid_group_ID_and_a_valid_member_ID(String groupId, String memberId) {
        userGroupId = groupId;
        userGroupMemberId = UUID.fromString(memberId);
    }

    @Given("I have a non-existent group ID {string} and a valid member ID {string}")
    public void i_have_a_non_existent_group_ID_and_a_valid_member_ID(String groupId, String memberId) {
        userGroupId = groupId;
        userGroupMemberId = UUID.fromString(memberId);
    }

    @When("I send a PUT request to {string}")
    public void i_send_a_PUT_request_to_with_the_group_and_member_ids(String url) {
        ResponseOptions<Response> response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("id", userGroupId)
                .pathParam("memberId", userGroupMemberId)
                .when()
                .put(url);

        status = response.statusCode();
        if(status != 204) {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a DELETE request to {string}")
    public void iSendADELETERequestTo(String url) {
        ResponseOptions<Response> response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("id", userGroupId)
                .pathParam("memberId", userGroupMemberId)
                .when()
                .delete(url);

        status = response.statusCode();
        if(status != 204) {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a DELETE request to {string} with query parameters institutionId and productId")
    public void iSendADELETERequestToWithQueryParametersInstitutionIdAndProductId(String url) {
        ResponseOptions<Response> response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("memberId", userGroupMemberId)
                .queryParam("institutionId", userGroupEntityFilter.getInstitutionId())
                .queryParam("productId", userGroupEntityFilter.getProductId())
                .when()
                .delete(url);

        status = response.statusCode();
        if(status != 204) {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a DELETE request to {string} with query parameters institutionId")
    public void iSendADELETERequestToWithQueryParametersInstitutionId(String url) {
        ResponseOptions<Response> response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("memberId", userGroupMemberId)
                .queryParam("institutionId", userGroupEntityFilter.getInstitutionId())
                .when()
                .delete(url);

        status = response.statusCode();
        if(status != 204) {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a DELETE request to {string} with query parameters productId")
    public void iSendADELETERequestToWithQueryParametersProductId(String url) {
        ResponseOptions<Response> response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("memberId", userGroupMemberId)
                .queryParam("productId", userGroupEntityFilter.getProductId())
                .when()
                .delete(url);

        status = response.statusCode();
        if(status != 204) {
            errorMessage = response.body().asString();
        }
    }

    @Given("I have a suspended group ID {string} and a valid member ID {string}")
    public void i_have_a_suspended_group_ID_and_a_valid_member_ID(String groupId, String memberId) {
        userGroupId = groupId;
        userGroupMemberId = UUID.fromString(memberId);
    }

    @Given("I have a valid group ID {string} and an invalid member ID {string}")
    public void i_have_a_valid_group_ID_and_an_invalid_member_ID(String groupId, String memberId) {
        userGroupId = groupId;
        userGroupMemberId = UUID.fromString(memberId);
    }

    @Given("I have a missing group ID and a valid member ID {string}")
    public void i_have_a_missing_group_ID_and_a_valid_member_ID(String memberId) {
        userGroupMemberId = UUID.fromString(memberId);
    }

    @Given("I have a valid group ID {string} and a missing member ID")
    public void i_have_a_valid_group_ID_and_a_missing_member_ID(String groupId) {

    }


    @Given("I have a valid member ID {string}, institution ID {string}, and product ID {string}")
    public void iHaveAValidMemberIDInstitutionIDAndProductID(String memberId, String institutionId, String productId) {
        userGroupMemberId = UUID.fromString(memberId);
        userGroupEntityFilter = new UserGroupEntity();
        userGroupEntityFilter.setInstitutionId(institutionId);
        userGroupEntityFilter.setProductId(productId);
    }

    @Given("I have a valid member ID {string} and institution ID {string} and a missing product ID")
    public void iHaveAValidMemberIDAndInstitutionIDAndAMissingProductID(String memberId, String institutionId) {
        userGroupMemberId = UUID.fromString(memberId);
        userGroupEntityFilter = new UserGroupEntity();
        userGroupEntityFilter.setInstitutionId(institutionId);
    }

    @Given("I have a valid member ID {string} and a missing institution ID and product ID {string}")
    public void iHaveAValidMemberIDAndAMissingInstitutionIDAndProductID(String memberId, String productId) {
        userGroupMemberId = UUID.fromString(memberId);
        userGroupEntityFilter = new UserGroupEntity();
        userGroupEntityFilter.setProductId(productId);
    }


    @And("the response of operation on user group members should contain an error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {
        String[] errorMessageArray = expectedErrorMessage.split(",");
        Arrays.stream(errorMessageArray).forEach(s -> Assertions.assertTrue(errorMessage.contains(s)));
    }

    @Then("I should receive a response of operation on user group members with status code {int}")
    public void i_should_receive_a_response_with_status_code(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, status);
    }

    @Before("@FirstGroupMembersScenario")
    public void beforeFeature() throws IOException {
        List<UserGroupEntity> groupsToInsert = objectMapper.readValue(new File("src/test/resources/dataPopulation/groupEntities.json"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserGroupEntity.class));
        userGroupRepository.insert(groupsToInsert);
    }

    @After("@LastGroupMembersScenario")
    public void afterFeature() {
        userGroupRepository.deleteAllById(userGroupsIds);
    }

}
