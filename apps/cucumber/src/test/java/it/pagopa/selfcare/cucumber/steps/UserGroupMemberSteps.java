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

    }

    @Given("I have a non-existent group ID {string} and a valid member ID {string}")
    public void i_have_a_non_existent_group_ID_and_a_valid_member_ID(String groupId, String memberId) {

    }

    @Given("I have a suspended group ID {string} and a valid member ID {string}")
    public void i_have_a_suspended_group_ID_and_a_valid_member_ID(String groupId, String memberId) {

    }

    @Given("I have a valid group ID {string} and an invalid member ID {string}")
    public void i_have_a_valid_group_ID_and_an_invalid_member_ID(String groupId, String memberId) {

    }

    @Given("I have a missing group ID and a valid member ID {string}")
    public void i_have_a_missing_group_ID_and_a_valid_member_ID(String memberId) {

    }

    @Given("I have a valid group ID {string} and a missing member ID")
    public void i_have_a_valid_group_ID_and_a_missing_member_ID(String groupId) {

    }

    @When("I send a PUT request to {string}")
    public void i_send_a_PUT_request_to_with_the_group_and_member_ids(String groupId, String memberId) {
        ResponseOptions<Response> response = RestAssured.given()
                .pathParam("id", groupId)
                .pathParam("memberId", memberId)
                .when()
                .put("/groups/{id}/members/{memberId}");

    }

    @Given("I have a valid member ID {string}, institution ID {string}, and product ID {string}")
    public void iHaveAValidMemberIDInstitutionIDAndProductID(String arg0, String arg1, String arg2) {

    }

    @Given("I have a valid member ID {string} and institution ID {string} and a missing product ID")
    public void iHaveAValidMemberIDAndInstitutionIDAndAMissingProductID(String arg0, String arg1) {

    }

    @Given("I have a valid member ID {string} and a missing institution ID and product ID {string}")
    public void iHaveAValidMemberIDAndAMissingInstitutionIDAndProductID(String arg0, String arg1) {
    }

    @When("I send a DELETE request to {string}")
    public void iSendADELETERequestTo(String arg0) {
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

    @And("the response of operation on user group members should contain an error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {
        String[] errorMessageArray = expectedErrorMessage.split(",");
        Arrays.stream(errorMessageArray).forEach(s -> Assertions.assertTrue(errorMessage.contains(s)));
    }

    @Then("I should receive a response of operation on user group members with status code {int}")
    public void i_should_receive_a_response_with_status_code(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, status);
    }
}
