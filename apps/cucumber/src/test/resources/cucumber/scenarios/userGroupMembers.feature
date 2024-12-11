Feature: User Group Members

  @FirstGroupMembersScenario
  Scenario: Successfully add a member to a group
    Given I have a valid group ID "12345" and a valid member ID "67890"
    When I send a PUT request to "/groups/12345/members/67890"
    Then I should receive a response of operation on user group members with status code 204

  Scenario: Attempt to add a member to a non-existent group
    Given I have a non-existent group ID "99999" and a valid member ID "67890"
    When I send a PUT request to "/groups/99999/members/67890"
    Then I should receive a response of operation on user group members with status code 404
    And the response of operation on user group members should contain an error message "Group not found"

  Scenario: Attempt to add a member to a suspended group
    Given I have a suspended group ID "12345" and a valid member ID "67890"
    When I send a PUT request to "/groups/12345/members/67890"
    Then I should receive a response of operation on user group members with status code 409
    And the response of operation on user group members should contain an error message "Cannot modify a suspended group"

  Scenario: Successfully delete a member from a group
    Given I have a valid group ID "12345" and a valid member ID "67890"
    When I send a DELETE request to "/groups/12345/members/67890"
    Then I should receive a response of operation on user group members with status code 204

  Scenario: Attempt to delete a member from a non-existent group
    Given I have a non-existent group ID "99999" and a valid member ID "67890"
    When I send a DELETE request to "/groups/99999/members/67890"
    Then I should receive a response of operation on user group members with status code 404
    And the response of operation on user group members should contain an error message "Group not found"

  Scenario: Attempt to delete a member from a suspended group
    Given I have a suspended group ID "12345" and a valid member ID "67890"
    When I send a DELETE request to "/groups/12345/members/67890"
    Then I should receive a response of operation on user group members with status code 409
    And the response of operation on user group members should contain an error message "Cannot modify a suspended group"

  Scenario: Attempt to delete a member with non-existent member ID
    Given I have a valid group ID "12345" and an invalid member ID "invalid-id"
    When I send a DELETE request to "/groups/12345/members/invalid-id"
    Then I should receive a response of operation on user group members with status code 400
    And the response of operation on user group members should contain an error message "Invalid member ID"

  Scenario: Successfully delete a member from all groups associated with institutionId and productId
    Given I have a valid member ID "67890", institution ID "institution1", and product ID "product1"
    When I send a DELETE request to "/members/67890?institutionId=institution1&productId=product1"
    Then I should receive a response of operation on user group members with status code 204

  Scenario: Attempt to delete a member with a missing institution ID
    Given I have a valid member ID "67890" and a missing institution ID and product ID "product1"
    When I send a DELETE request to "/members/67890?productId=product1"
    Then I should receive a response of operation on user group members with status code 400
    And the response of operation on user group members should contain an error message "Institution ID is required"

  Scenario: Attempt to delete a member with a missing product ID
    Given I have a valid member ID "67890" and institution ID "institution1" and a missing product ID
    When I send a DELETE request to "/members/67890?institutionId=institution1"
    Then I should receive a response of operation on user group members with status code 400
    And the response of operation on user group members should contain an error message "Product ID is required"

  @LastGroupMembersScenario
  Scenario: Attempt to delete a member who is not in any group associated with the given institutionId and productId
    Given I have a valid member ID "67890", institution ID "institution1", and product ID "product1"
    When I send a DELETE request to "/members/67890?institutionId=institution1&productId=product1"
    Then I should receive a response of operation on user group members with status code 204

