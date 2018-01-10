@FCRUD @FAT
Feature: Basic Flow CRUD

  This feature tests basic flow CRUD operations.
  These tests are Functional Acceptance Tests (FAT).

  @MVP1 @SMOKE
  Scenario Outline: Flow Creation on Small Linear Network Topology

  This scenario setups flows across the entire set of switches and checks that these flows were stored in database

    #
    # TODO - This "Given" can be reduced to a single statement. And network topo can be cached. (speed up tests)
    #
    Given a clean controller
    And a nonrandom linear topology of 5 switches
    And topology contains 8 links
    And a clean flow topology
    When flow <flow_id> creation request with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is successful
    Then flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be created
    And flow <flow_id> in UP state
    And rules with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> are installed
    And traffic through <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is forwarded

    Examples:
      | flow_id |      source_switch      | source_port | source_vlan |   destination_switch    | destination_port | destination_vlan | bandwidth |
      # flows with transit vlans and intermediate switches
      | c3none  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      # flows with transit vlans and without intermediate switches
# This test is failing on 2018.01.06 - it is failing
#      | c2none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      # flows without transit vlans and intermediate switches
# This test is failing on 2018.01.06 - it is failing
#      | c1none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |



  @MVP1 @CRUD_CREATE
  Scenario Outline: Flow Creation - flows with transit vlans and intermediate switches

    This scenario setups flows across the entire set of switches and checks that these flows were stored in database

    Given a clean controller
    And a nonrandom linear topology of 5 switches
    And topology contains 8 links
    And a clean flow topology
    When flow <flow_id> creation request with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is successful
    Then flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be created
    And flow <flow_id> in UP state
    And rules with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> are installed
    And traffic through <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is forwarded

    Examples:
      | flow_id |      source_switch      | source_port | source_vlan |   destination_switch    | destination_port | destination_vlan | bandwidth |
      # flows with transit vlans and intermediate switches
      | c3none  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | c3push  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |
      | c3pop   | de:ad:be:ef:00:00:00:02 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | c3swap  | de:ad:be:ef:00:00:00:02 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |

  @MVP1.1 @CRUD_CREATE
  Scenario Outline: Flow Creation - flows with transit vlans and without intermediate switches

  This scenario setups flows across the entire set of switches and checks that these flows were stored in database

    Given a clean controller
    And a nonrandom linear topology of 5 switches
    And topology contains 8 links
    And a clean flow topology
    When flow <flow_id> creation request with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is successful
    Then flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be created
    And flow <flow_id> in UP state
    And rules with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> are installed
    And traffic through <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is forwarded

    Examples:
      | flow_id |      source_switch      | source_port | source_vlan |   destination_switch    | destination_port | destination_vlan | bandwidth |
      # flows with transit vlans and without intermediate switches
      | c2none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | c2push  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |
      | c2pop   | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | c2swap  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |

  @MVP1.1 @CRUD_CREATE
  Scenario Outline: Flow Creation - flows without transit vlans and intermediate switches

  This scenario setups flows across the entire set of switches and checks that these flows were stored in database

    Given a clean controller
    And a nonrandom linear topology of 5 switches
    And topology contains 8 links
    And a clean flow topology
    When flow <flow_id> creation request with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is successful
    Then flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be created
    And flow <flow_id> in UP state
    And rules with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> are installed
    And traffic through <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is forwarded

    Examples:
      | flow_id |      source_switch      | source_port | source_vlan |   destination_switch    | destination_port | destination_vlan | bandwidth |
      # flows without transit vlans and intermediate switches
      | c1none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |
      | c1push  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |       100        |   10000   |
      | c1pop   | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |
      | c1swap  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |       200        |   10000   |



  @MVP1.1 @CRUD_READ
  Scenario Outline: Flow Reading on Small Linear Network Topology

    This scenario setups flows across the entire set of switches and checks that these flows could be read from database

    Given a clean controller
    And a nonrandom linear topology of 5 switches
    And topology contains 8 links
    And a clean flow topology
    When flow <flow_id> creation request with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is successful
    And flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be created
    And flow <flow_id> in UP state
    Then flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be read

    Examples:
      | flow_id |      source_switch      | source_port | source_vlan |   destination_switch    | destination_port | destination_vlan | bandwidth |
      # flows with transit vlans and intermediate switches
      | r3none  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | r3push  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |
      | r3pop   | de:ad:be:ef:00:00:00:02 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | r3swap  | de:ad:be:ef:00:00:00:02 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |
      # flows with transit vlans and without intermediate switches
      | r2none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | r2push  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |
      | r2pop   | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | r2swap  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |
      # flows without transit vlans and intermediate switches
      | r1none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |
      | r1push  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |       100        |   10000   |
      | r1pop   | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |
      | r1swap  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |       200        |   10000   |

  @MVP1.1 @CRUD_UPDATE
  Scenario Outline: Flow Updating on Small Linear Network Topology

  This scenario setups flows across the entire set of switches, then updates them and checks that flows were updated in database

    Given a clean controller
    And a nonrandom linear topology of 5 switches
    And topology contains 8 links
    And a clean flow topology
    When flow <flow_id> creation request with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is successful
    And flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be created
    And flow <flow_id> in UP state
    Then flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be updated with <new_bandwidth>
    And flow <flow_id> in UP state
    And rules with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> are updated with <new_bandwidth>
    And traffic through <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is forwarded

    Examples:
      | flow_id |      source_switch      | source_port | source_vlan |   destination_switch    | destination_port | destination_vlan | bandwidth | new_bandwidth |
      # flows with transit vlans and intermediate switches
      | u3none  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |     20000     |
      | u3push  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |     20000     |
      | u3pop   | de:ad:be:ef:00:00:00:02 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |     20000     |
      | u3swap  | de:ad:be:ef:00:00:00:02 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |     20000     |
      # flows with transit vlans and without intermediate switches
      | u2none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |     20000     |
      | u2push  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |     20000     |
      | u2pop   | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |     20000     |
      | u2swap  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |     20000     |
      # flows without transit vlans and intermediate switches
      | u1none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |     20000     |
      | u1push  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |       100        |   10000   |     20000     |
      | u1pop   | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |     20000     |
      | u1swap  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |       200        |   10000   |     20000     |

  @MVP1 @CRUD_DELETE
  Scenario Outline: Flow Deletion on Small Linear Network Topology

    This scenario will setup flows across the entire set of switches, then deletes them and checks
    that flows were deleted from database.

    # TODO: as part of FAT, verify intermediary caches are clear (currently looks at DB only)
    # TODO: these tests don't check the switches/speaker and whether flows are removed

    Given a clean controller
    And a nonrandom linear topology of 5 switches
    And topology contains 8 links
    And a clean flow topology
    When flow <flow_id> creation request with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is successful
    And flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be created
    And flow <flow_id> in UP state
    Then flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be deleted
    And rules with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> are deleted
    And traffic through <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is not forwarded

    Examples:
      | flow_id |      source_switch      | source_port | source_vlan |   destination_switch    | destination_port | destination_vlan | bandwidth |
      # flows with transit vlans and intermediate switches
      | d3none  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | d3push  | de:ad:be:ef:00:00:00:02 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |
      | d3pop   | de:ad:be:ef:00:00:00:02 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | d3swap  | de:ad:be:ef:00:00:00:02 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |
      # flows with transit vlans and without intermediate switches
      | d2none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | d2push  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |
      | d2pop   | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | d2swap  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |
      # flows without transit vlans and intermediate switches
      | d1none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |
      | d1push  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |       100        |   10000   |
      | d1pop   | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |        0         |   10000   |
      | d1swap  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:03 |         2        |       200        |   10000   |

  @MVP1.1 @CRUD_NEGATIVE
  Scenario Outline: Flow Creation Negative Scenario for Vlan Conflicts

  This scenario setups flow across the entire set of switches and checks that no new flow with conflicting vlan could be installed

    Given a clean controller
    And a nonrandom linear topology of 5 switches
    And topology contains 8 links
    And a clean flow topology
    When flow <flow_id> creation request with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> is successful
    And flow <flow_id> with <source_switch> <source_port> <source_vlan> and <destination_switch> <destination_port> <destination_vlan> and <bandwidth> could be created
    Then flow <flow_id>_conflict creation request with <source_switch> <source_port> 200 and <destination_switch> <destination_port> 200 and <bandwidth> is failed

    Examples:
      | flow_id  |      source_switch      | source_port | source_vlan |   destination_switch    | destination_port | destination_vlan | bandwidth |
      | vc2none  | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | vc2push1 | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |
      | vc2push2 | de:ad:be:ef:00:00:00:03 |      1      |      0      | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |
      | vc2pop1  | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | vc2pop2  | de:ad:be:ef:00:00:00:03 |      1      |     200     | de:ad:be:ef:00:00:00:04 |         2        |        0         |   10000   |
      | vs2swap1 | de:ad:be:ef:00:00:00:03 |      1      |     100     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |
      | vs2swap2 | de:ad:be:ef:00:00:00:03 |      1      |     200     | de:ad:be:ef:00:00:00:04 |         2        |       100        |   10000   |
      | vs2swap3 | de:ad:be:ef:00:00:00:03 |      1      |     200     | de:ad:be:ef:00:00:00:04 |         2        |       200        |   10000   |
