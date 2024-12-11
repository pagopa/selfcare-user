Feature: Get User Group

  @FirstRetrieveGroupScenario
  Scenario: Successfully retrieve a group with a valid ID
    Given I have a valid group ID "6759f8df78b6af202b222d29"
    When I send a GET request to "/v1/user-groups/6759f8df78b6af202b222d29"
    Then I should receive a response of retrieve user group operation with status code 200
    And the response should contain the group details

  Scenario: Attempt to retrieve a non-existent group
    Given I have a non-existent group ID "99999"
    When I send a GET request to "/v1/user-groups/99999"
    Then I should receive a response of retrieve user group operation with status code 404
    And the response of retrieve operation should contain an error message "User group not found"

  Scenario: Successfully retrieve user groups with valid filters
    Given I have valid filters institutionId "9c8ae123-d990-4400-b043-67a60aff31bc" productId "prod-io" and status "ACTIVE"
    When I send a GET request to "/v1/user-groups"
    Then I should receive a response of retrieve user group operation with status code 200
    And the response should contain 1 item
    And the response should contain valid data

  Scenario: Successfully retrieve user groups without any filters
    Given I have no filters
    When I send a GET request to "/v1/user-groups"
    Then I should receive a response of retrieve user group operation with status code 200
    And the response should contain user groups data
    And the response should contain 3 item
    And the response should contain valid data

  Scenario: Attempt to retrieve user groups with a sorting parameter but no productId or institutionId
    Given I have a filter with sorting by "name" but no "institutionId" or "productId"
    When I send a GET request to "/v1/user-groups"
    Then I should receive a response of retrieve user group operation with status code 400
    And the response of retrieve operation should contain an error message "Sorting not allowed without productId or institutionId"

  Scenario: Attempt to retrieve user groups with status filter but no productId, institutionId, or userId
    Given I have a filter with status "ACTIVE" but no "productId", "institutionId", or "userId"
    When I send a GET request to "/v1/user-groups"
    Then I should receive a response of retrieve user group operation with status code 400
    And the response of retrieve operation should contain an error message "At least one of productId, institutionId and userId must be provided with status filter"

  Scenario: Successfully retrieve a paginated list of user groups
    Given I have valid filters "institutionId=123" and "productId=abc"
    And I set the page number to 1 and page size to 10
    When I send a GET request to "/v1/user-groups"
    Then I should receive a response of retrieve user group operation with status code 200
    And the response should contain a paginated list of user groups

  @LastFirstRetrieveGroupScenario
  Scenario: No user groups found for the provided filters
    Given I have filters "institutionId=9999" and "productId=xyz"
    When I send a GET request to "/v1/user-groups"
    Then I should receive a response of retrieve user group operation with status code 200
    And the response should contain an empty list

