
Feature: Opera browser support

# - **As** a html web frontend running on Opera Mobile
# - **I want** to call the API with cross-origin requests
# - **So that** that mobile users are not affected
#
# *Description:*
# Opera Mobile appears to be more restrictive on CORS support.
# Unlike Chrome which appears to accept asterisks, Opera seems very picky
# on the Access-Control-Allow-Origin, Access-Control-Request-Method
# and Access-Control-Allow-Headers.
#

  Scenario: the preflight OPTIONS request contains tailored headers for Opera
    Given the API runs
    When I perform an OPTIONS request to the health check with Origin header set to https://test.example.com
    Then the Access-Control-Allow-Origin header is https://test.example.com
    And the Access-Control-Request-Method header contains all of GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD,TRACE
    And the Access-Control-Allow-Headers header contains all of Authorization,X-Module
