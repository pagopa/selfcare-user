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
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import it.pagopa.selfcare.cucumber.dao.UserGroupRepository;
import it.pagopa.selfcare.cucumber.model.UserGroupEntity;
import it.pagopa.selfcare.cucumber.model.UserGroupStatus;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class RetrieveUserGroupSteps extends UserGroupSteps {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    List<String> userGroupsIds = List.of("6759f8df78b6af202b222d29", "6759f8df78b6af202b222d2a", "6759f8df78b6af202b222d2b");

    @Given("I have a valid group ID to retrieve: {string}")
    public void i_have_a_valid_group_ID(String validGroupId) {
        userGroupId = validGroupId;
    }

    @Given("I have a non-existent group ID to retrieve {string}")
    public void i_have_a_non_existent_group_ID(String nonExistentGroupId) {
        userGroupId = nonExistentGroupId;
    }

    @When("I send a GET request to {string}")
    public void iSendAGETRequestTo(String url) {
        ResponseOptions<Response> response = RestAssured.given()
                .pathParam("id", userGroupId)
                .header("Authorization", "Bearer " + token)
                .when()
                .get(url);
        status = response.statusCode();
        if (status == 200) {
            userGroupEntityResponse = response.getBody().as(UserGroupEntity.class);
        } else {
            errorMessage = response.getBody().asString();
        }
    }


    @When("I send a GET request to {string} to retrieve filtered userGroups")
    public void iSendAGETRequestToRetrieveFilteredUserGroups(String url) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .queryParams("productId", userGroupEntityFilter.getProductId(),
                        "institutionId", userGroupEntityFilter.getInstitutionId(),
                        "status", userGroupEntityFilter.getStatus())
                .when()
                .get(url)
                .then()
                .extract();

        status = response.statusCode();
        if (status == 200) {
            userGroupEntityResponsePage = response.body().as(new TypeRef<>() {});
            if (Objects.nonNull(userGroupEntityResponsePage) && !userGroupEntityResponsePage.getContent().isEmpty()) {
                userGroupEntityResponse = userGroupEntityResponsePage.getContent().get(0);
                userGroupId = userGroupEntityResponse.getId();
            }
        } else {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to filter userGroups by status")
    public void iSendAGETRequestToToFilterUserGroupsByStatus(String url) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .queryParams("status", userGroupEntityFilter.getStatus())
                .when()
                .get(url)
                .then()
                .extract();

        status = response.statusCode();
        if (status == 200) {
            userGroupEntityResponsePage = response.body().as(new TypeRef<>() {});
            if (Objects.nonNull(userGroupEntityResponsePage) && !userGroupEntityResponsePage.getContent().isEmpty()) {
                userGroupEntityResponse = userGroupEntityResponsePage.getContent().get(0);
            }
            userGroupId = userGroupEntityResponse.getId();
        } else {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to retrieve not filtered userGroups")
    public void iSendAGETRequestToToRetrieveNotFilteredUserGroups(String url) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get(url)
                .then()
                .extract();

        status = response.statusCode();
        if (status == 200) {
            userGroupEntityResponsePage = response.body().as(new TypeRef<>() {});
            if (Objects.nonNull(userGroupEntityResponsePage) && !userGroupEntityResponsePage.getContent().isEmpty()) {
                userGroupEntityResponse = userGroupEntityResponsePage.getContent().get(0);
            }
            userGroupId = userGroupEntityResponse.getId();
        } else {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to retrieve sorted userGroups")
    public void iSendAGETRequestToToRetrieveSortedUserGroups(String url) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .queryParams("sort", sort)
                .when()
                .get(url)
                .then()
                .extract();

        status = response.statusCode();
        if (status == 200) {
            userGroupEntityResponsePage = response.body().as(new TypeRef<>() {});
            if (Objects.nonNull(userGroupEntityResponsePage) && !userGroupEntityResponsePage.getContent().isEmpty()) {
                userGroupEntityResponse = userGroupEntityResponsePage.getContent().get(0);
            }
            userGroupId = userGroupEntityResponse.getId();
        } else {
            errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to pageable object")
    public void iSendAGETRequestToToPageableObject(String url) {
        ExtractableResponse<?> response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .queryParams("page", pageable.getPageNumber(), "size", pageable.getPageSize())
                .when()
                .get(url)
                .then()
                .extract();

        status = response.statusCode();
        if (status == 200) {
            userGroupEntityResponsePage = response.body().as(new TypeRef<>() {});
            if (Objects.nonNull(userGroupEntityResponsePage) && !userGroupEntityResponsePage.getContent().isEmpty()) {
                userGroupEntityResponse = userGroupEntityResponsePage.getContent().get(0);
            }
            userGroupId = userGroupEntityResponse.getId();
        } else {
            errorMessage = response.body().asString();
        }
    }

    @Given("I have a filter with sorting by {string} but no filter")
    public void iHaveAFilterWithSortingByButNoFilter(String sortBy) {
        sort = sortBy;
    }

    @And("I set the page number to {int} and page size to {int}")
    public void iSetThePageNumberToAndPageSizeTo(int page, int size) {
        pageable = Pageable.ofSize(size).withPage(page);
    }

    @Then("the response should contain the group details")
    public void the_response_should_contain_the_group_details() {
        Assertions.assertEquals(userGroupId, userGroupEntityResponse.getId());
        Assertions.assertEquals("io group", userGroupEntityResponse.getName());
        Assertions.assertEquals("io group description", userGroupEntityResponse.getDescription());
        Assertions.assertEquals("9c8ae123-d990-4400-b043-67a60aff31bc", userGroupEntityResponse.getInstitutionId());
        Assertions.assertEquals("prod-io", userGroupEntityResponse.getProductId());
        Assertions.assertEquals("ACTIVE", userGroupEntityResponse.getStatus().name());
        Assertions.assertEquals(1, userGroupEntityResponse.getMembers().size());
        Assertions.assertEquals("75003d64-7b8c-4768-b20c-cf66467d44c7", userGroupEntityResponse.getMembers().iterator().next());
        Assertions.assertNotNull(userGroupEntityResponse.getCreatedAt());
        Assertions.assertNull(userGroupEntityResponse.getModifiedAt());
        Assertions.assertEquals("4ba2832d-9c4c-40f3-9126-e1c72905ef14", userGroupEntityResponse.getCreatedBy());
        Assertions.assertNull(userGroupEntityResponse.getModifiedBy());
    }

    @And("the response should contain a paginated list of user groups of {int} items on page {int}")
    public void theResponseShouldContainAPaginatedListOfUserGroups(int count,int page) {
        Assertions.assertEquals(count, userGroupEntityResponsePage.getContent().size());
        Assertions.assertEquals(3, userGroupEntityResponsePage.getTotalElements());
        Assertions.assertEquals(2, userGroupEntityResponsePage.getTotalPages());
        Assertions.assertEquals(2, userGroupEntityResponsePage.getSize());
        Assertions.assertEquals(page, userGroupEntityResponsePage.getNumber());
    }

    @Given("I have valid filters institutionId {string} productId {string} and status {string}")
    public void iHaveValidFiltersAndAnd(String institutionId, String productId, String status) {
        userGroupEntityFilter = new UserGroupEntity();
        userGroupEntityFilter.setInstitutionId(institutionId);
        userGroupEntityFilter.setProductId(productId);
        userGroupEntityFilter.setStatus(UserGroupStatus.valueOf(status));
    }

    @Given("I have no filters")
    public void iHaveNoFilters() {
        userGroupEntityFilter = new UserGroupEntity();
    }

    @And("the response should contains groupIds {string}")
    public void theResponseShouldContainGroupIds( String ids) {
        List<String> idsList = Arrays.asList(ids.split(","));
        Assertions.assertEquals(idsList, userGroupEntityResponsePage.getContent().stream().map(UserGroupEntity::getId).toList());
    }

    @And("the response should contain an empty list")
    public void theResponseShouldContainAnEmptyList() {
        Assertions.assertEquals(0, userGroupEntityResponsePage.getContent().size());
    }

    @And("the response should contain {int} item")
    public void theResponseShouldContainOneItem(int expectedItemsCount) {
        Assertions.assertEquals(expectedItemsCount, userGroupEntityResponsePage.getContent().size());
    }

    @And("the response of retrieve operation should contain an error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {
        String[] errorMessageArray = expectedErrorMessage.split(",");
        Arrays.stream(errorMessageArray).forEach(s -> Assertions.assertTrue(errorMessage.contains(s)));
    }

    @Then("I should receive a response of retrieve user group operation with status code {int}")
    public void i_should_receive_a_response_with_status_code(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, status);
    }


    @Given("I have a filter with status {string} but no productId, institutionId or userId")
    public void iHaveAFilterWithStatusButNoOr(String status) {
        userGroupEntityFilter = new UserGroupEntity();
        userGroupEntityFilter.setStatus(UserGroupStatus.valueOf(status));
    }

    @Before("@FirstRetrieveGroupScenario")
    public void beforeFeature() throws IOException {
        List<UserGroupEntity> groupsToInsert = objectMapper.readValue(new File("src/test/resources/dataPopulation/groupEntities.json"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserGroupEntity.class));
        userGroupRepository.insert(groupsToInsert);
    }

    @After("@LastRetrieveGroupScenario")
    public void afterFeature() {
        userGroupRepository.deleteAllById(userGroupsIds);
    }
}

