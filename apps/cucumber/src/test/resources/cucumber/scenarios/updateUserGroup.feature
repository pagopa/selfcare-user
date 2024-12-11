Feature: Update User Group

  @FirstUpdateScenario
  Scenario: Successfully suspend a group with a valid ID
    Given I retrieve to update Group a group Id for product "prod-io" and institution "9c8ae123-d990-4400-b043-67a60aff31bc"
    When I send a POST request to suspendAPI with retrieved groupId
    Then I should receive a response with status code 204
    And the retrieved group should be changed status to "SUSPENDED"

  Scenario: Attempt to suspend a group with a non-existent ID
    Given I have a non-existent group ID "99999"
    When I send a POST request to suspendAPI with not existent groupId
    Then I should receive a response with status code 404
    And the response of update operation should contain an error message "Not Found"

  Scenario: Successfully activate a group with a valid ID
    Given I retrieve to update Group a group Id for product "prod-io" and institution "9c8ae123-d990-4400-b043-67a60aff31bc"
    When I send a POST request to activateAPI with retrieve groupId
    Then I should receive a response with status code 204
    And the retrieved group should be changed status to "ACTIVE"

  Scenario: Attempt to activate a group with a non-existent ID
    Given I have a non-existent group ID "99999"
    When I send a POST request to activateAPI with not existent groupId
    Then I should receive a response with status code 404
    And the response of update operation should contain an error message "Not Found"

  Scenario: Successfully delete a group with a valid ID
    Given I retrieve to update Group a group Id for product "prod-io" and institution "9c8ae123-d990-4400-b043-67a60aff31bc"
    When I send a POST request to deleteAPI with retrieve groupId
    Then I should receive a response with status code 204
    And the retrieved group should be changed status to "DELETED"

  Scenario: Attempt to delete a group with a non-existent ID
    Given I have a non-existent group ID "99999"
    When I send a POST request to deleteAPI with not existent groupId
    Then I should receive a response with status code 404
    And the response of update operation should contain an error message "Not Found"

  Scenario: Successfully update a group with a valid ID and valid data
    Given I retrieve to update Group a group Id for product "prod-io" and institution "9c8ae123-d990-4400-b043-67a60aff31bc"
    And I have data to update:
      | name          | description          | members                                                                    |
      | updated Name  | updatedDescription   | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30  |
    When I send a PUT request to updateAPI with the retrieved group data
    Then I should receive a response with status code 200
    And the retrieved group should be updated

  Scenario: Attempt to update a non-existent group
    Given I have a non-existent group ID "99999"
    And I have data to update:
      | name        | description  | members                                                                   |
      | Group Name  | TestGroup    | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a PUT request to updateAPI with the group data
    Then I should receive a response with status code 404
    And the response of update operation should contain an error message "Not Found"

  @LastUpdateScenario
  Scenario: Attempt to update a suspended group
    Given I retrieve to update Group a group Id for product "prod-interop" and institution "9c8ae123-d990-4400-b043-67a60aff31bc"
    And I have data to update:
      | name        | description  | members                                                                   |
      | Group Name  | TestGroup    | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a PUT request to updateAPI with the retrieved group data
    Then I should receive a response with status code 400
    And the response of update operation should contain an error message "Trying to modify suspended group"



