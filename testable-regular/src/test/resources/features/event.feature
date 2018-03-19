Feature: Event recording

# - **As** an monitored device
# - **I want** to record events
# - **So that** they can be digested into reports later
#
# *Description:*
# A device goes through various states, such as from being used to being idle.
# The new state is sent to the application.
#
# The application does not need to know the device in advance before accepting the events.
# Not all events received by the application should be recorded.
# If two consecutive events from the same device describe the same state, then that event is discarded.
#

  Scenario: the first event of a device is recorded
    Given there are initially 0 events for device A
    And there are initially 2 events for device B
    When I send an event for device A with state some_random_state
    Then there are 1 events for device A
    And there are 2 events for device B
    And the first event for device A has state some_random_state

  Scenario: the same event sent twice in a row is only recorded once
    Given there are initially 10 events for device A
    When I send an event for device A with state some_random_state
    And I send another event for device A with state some_random_state
    Then there are 11 events for device A
    And the last event for device A has state some_random_state