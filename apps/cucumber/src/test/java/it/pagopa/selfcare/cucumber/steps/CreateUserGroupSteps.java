package it.pagopa.selfcare.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import it.pagopa.selfcare.cucumber.dao.UserGroupRepository;
import it.pagopa.selfcare.cucumber.model.UserGroupEntity;
import it.pagopa.selfcare.cucumber.model.UserGroupStatus;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CreateUserGroupSteps extends UserGroupSteps{

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    List<String> userGroupsIds = List.of("6759f8df78b6af202b222d29", "6759f8df78b6af202b222d2a","6759f8df78b6af202b222d2b");

    @DataTableType
    public UserGroupEntity convertRequest(Map<String, String> entry) {
        UserGroupEntity userGroupEntity = new UserGroupEntity();
        userGroupEntity.setInstitutionId(entry.get("institutionId"));
        userGroupEntity.setProductId(entry.get("productId"));
        userGroupEntity.setName(entry.get("name"));
        userGroupEntity.setDescription(entry.get("description"));
        userGroupEntity.setStatus(Optional.ofNullable(entry.get("status")).map(s -> UserGroupStatus.valueOf(entry.get("status"))).orElse(null));
        userGroupEntity.setMembers(Optional.ofNullable(entry.get("members")).map(s -> Set.of(entry.get("members").split(","))).orElse(null));
        return userGroupEntity;
    }

    @When("I send a POST request to {string} with the given details")
    public void iSendAPOSTRequestToWithTheGivenDetails(String url) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(userGroupDetails)
                .when()
                .post(url)
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

    @When("I send a POST request to {string} with the given details without authentication")
    public void iSendAPOSTRequestToWithTheGivenDetailsWithoutAuthentication(String url) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + null)
                .body(userGroupDetails)
                .when()
                .post(url)
                .then()
                .extract();

        status = response.statusCode();
    }

    @Given("the following user group details:")
    public void givenUserGroupDetails(List<UserGroupEntity> userGroupEntityList) {
        if (userGroupEntityList != null && userGroupEntityList.size() == 1)
            this.userGroupDetails = userGroupEntityList.get(0);
    }

    // Step per verificare lo stato della risposta
    @Then("the response status should be {int}")
    public void verifyResponseStatus(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, status);
    }

    // Step per verificare che la risposta contenga un gruppo utente valido
    @Then("the response should contain a valid user group resource with name {string}")
    public void verifyUserGroupName(String expectedName) {
        Assertions.assertEquals(expectedName, userGroupEntityResponse.getName());
    }

    // Step per verificare un messaggio di errore nella risposta
    @Then("the response should contain an error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {
        String[] errorMessageArray = expectedErrorMessage.split(",");
        Arrays.stream(errorMessageArray).forEach(s -> Assertions.assertTrue(errorMessage.contains(s)));
    }

    // Step per verificare che la risposta contenga un determinato campo
    @Then("the response should contain the productId {string}")
    public void verifyProductId(String expectedProductId) {
        Assertions.assertEquals(expectedProductId, userGroupEntityResponse.getProductId());
    }

    @Then("the response should contain the institutionId {string}")
    public void verifyInstitutionId(String expectedInstitutionId) {
        Assertions.assertEquals(expectedInstitutionId, userGroupEntityResponse.getInstitutionId());
    }

    @And("the response should contain the status {string}")
    public void theResponseShouldContainTheStatus(String expectedStatus) {
        Assertions.assertEquals(expectedStatus, userGroupEntityResponse.getStatus().name());
    }

    @And("the response should contain {int} members")
    public void theResponseShouldContainMembers(int expectedMembersCount) {
        Assertions.assertEquals(expectedMembersCount, userGroupEntityResponse.getMembers().size());
    }

    @And("the response should contain the createdBy {string}")
    public void theResponseShouldContainTheCreatedBy(String expectedCreatedBy) {
        Assertions.assertEquals(expectedCreatedBy, userGroupEntityResponse.getCreatedBy());
    }

    @And("the response should contain the createdAt notNull")
    public void theResponseShouldContainTheCreatedAtNotNull() {
        Assertions.assertNotNull(userGroupEntityResponse.getCreatedAt());
    }


    @And("the response should contain the modified data null")
    public void theResponseShouldContainTheModifiedDataNull() {
        Assertions.assertNull(userGroupEntityResponse.getModifiedAt());
        Assertions.assertNull(userGroupEntityResponse.getModifiedBy());
    }

    @And("the response should contain the description {string}")
    public void theResponseShouldContainTheDescription(String expectedDescription) {
        Assertions.assertEquals(expectedDescription, userGroupEntityResponse.getDescription());
    }

    @Before("@DuplicateGroupName")
    public void beforeScenarioOfCreateDuplicatedGroup() throws IOException {
        List<UserGroupEntity> groupsToInsert = objectMapper.readValue(new File("src/test/resources/dataPopulation/groupEntities.json"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserGroupEntity.class));
        userGroupRepository.insert(groupsToInsert);
    }

    @After("@DuplicateGroupName")
    public void afterScenarioOfCreateDuplicatedGroup() {
        userGroupRepository.deleteAllById(userGroupsIds);
    }

    @After("@CreateNewGroup")
    public void afterScenarioOfCreateGroup() {
        userGroupRepository.deleteById(userGroupId);
    }
}

