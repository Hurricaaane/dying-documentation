Feature: Bootstrapping

# - **As** an external application
# - **I want** to call a REST API
# - **So that** I can interact with this application
#
# *Description:*
#

  Scenario: the web application is responding
    Given the API runs
    When I visit the root path
    Then I get any response

  Scenario: the web application is healthy
    Given the API runs
    When I visit the health check
    Then the status code is 200
