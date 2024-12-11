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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class RetrieveUserGroupSteps extends UserGroupSteps{

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    List<String> userGroupsIds = List.of("6759f8df78b6af202b222d29", "6759f8df78b6af202b222d2a","6759f8df78b6af202b222d2b");


    @Given("I have an empty group ID")
    public void i_have_an_empty_group_ID() {
        userGroupId = "";
    }

    @Given("I have a suspended group ID {string}")
    public void i_have_a_suspended_group_ID(String suspendedGroupId) {
        userGroupId = suspendedGroupId;
    }

    @When("I send a GET request to")
    public void i_send_a_GET_request_to_with_the_group_id(String endpoint) {
        ResponseOptions<Response> response = RestAssured.given()
                .pathParam("id", userGroupId)
                .when()
                .get(endpoint);
        status = response.statusCode();
        if(status == 200) {
            userGroupEntityResponse = response.getBody().as(UserGroupEntity.class);
        }else{
            errorMessage = response.getBody().asString();
        }
    }

    @Then("the response should contain the group details")
    public void the_response_should_contain_the_group_details() {
        // Verifica che la risposta contenga i dettagli del gruppo, per esempio controllando un campo specifico
    //    assert(responseBody.contains("groupName"));  // Supponiamo che la risposta contenga un campo "groupName"
    }

    @When("I send a GET request to {string}")
    public void iSendAGETRequestTo(String arg0) {
        
    }

    @And("the response should contain the suspended group details")
    public void theResponseShouldContainTheSuspendedGroupDetails() {
        
    }

    @Given("I have valid filters institutionId {string} productId {string} and status {string}")
    public void iHaveValidFiltersAndAnd(String institutionId, String productId, String status) {
        
    }

    @And("the response should contain user groups data")
    public void theResponseShouldContainUserGroupsData() {
        
    }

    @Given("I have no filters")
    public void iHaveNoFilters() {
        
    }

    @Given("I have invalid filters {string} and {string}")
    public void iHaveInvalidFiltersAnd(String arg0, String arg1) {
        
    }

    @Given("I have a filter with sorting by {string} but no {string} or {string}")
    public void iHaveAFilterWithSortingByButNoOr(String arg0, String arg1, String arg2) {
        
    }

    @Given("I have a filter with status {string} but no {string}, {string}, or {string}")
    public void iHaveAFilterWithStatusButNoOr(String arg0, String arg1, String arg2, String arg3) {
        
    }

    @Given("I have valid filters {string} and {string}")
    public void iHaveValidFiltersAnd(String arg0, String arg1) {
        
    }

    @And("I set the page number to {int} and page size to {int}")
    public void iSetThePageNumberToAndPageSizeTo(int arg0, int arg1) {
        
    }

    @And("the response should contain a paginated list of user groups")
    public void theResponseShouldContainAPaginatedListOfUserGroups() {
        
    }

    @Given("I have filters {string} and {string}")
    public void iHaveFiltersAnd(String arg0, String arg1) {
        
    }

    @And("the response should contain an empty list")
    public void theResponseShouldContainAnEmptyList() {

    }

    @Before("@FirstRetrieveUserGroupScenario")
    public void beforeFeature() throws IOException {
        List<UserGroupEntity> groupsToInsert = objectMapper.readValue(new File("src/test/resources/dataPopulation/groupEntities.json"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserGroupEntity.class));
        userGroupRepository.insert(groupsToInsert);
    }

    @After("@LastRetrieveUserGroupScenario")
    public void afterFeature() {
        userGroupRepository.deleteAllById(userGroupsIds);
    }

    @And("the response of retrieve operation should contain an error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {
        String[] errorMessageArray = expectedErrorMessage.split(",");
        Arrays.stream(errorMessageArray).forEach(s -> Assertions.assertTrue(errorMessage.contains(s)));
    }

    @And("the response should contain {int} item")
    public void theResponseShouldContainOneItem(int expectedItemsCount) {

    }

    @And("the response should contain valid data")
    public void theResponseShouldContainValidData() {
    }

    @Then("I should receive a response of retrieve user group operation with status code {int}")
    public void i_should_receive_a_response_with_status_code(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, status);
    }
}

