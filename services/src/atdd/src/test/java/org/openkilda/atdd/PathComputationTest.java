/* Copyright 2018 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.atdd;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.openkilda.topo.TopologyHelp;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * PathComputationTest implements a majority of the cucumber tests for the Path Computation Engine
 * (PCE). The interesting scenarios are around the following areas:
 *
 *  - validate that kilda honors the various path computation scenarios
 *  - there should also be path edge cases that are tested, in order to validate the user
 *      experience - eg when a flow is requested that doesn't exist (bandwidth limitation or just
 *      no path). However, a few are already implemented in FlowPathTest.
 *
 * In order to test the scenarios, we use a special topology:
 *
 *      1 - 2 - 3 - 4 - 5 - 6 - 7
 *          2 -- 8 - - 9 -- 6
 *          2 - - - A - - - 6
 *          2 - - - - - - - 6
 *
 * This is to ensure that, as we vary the hops (path length) we also very the the path used.
 *
 *  - For hop count, 2-6 should win.
 *  - For latency, 2-A-6 should win (we'll configure it as such)
 *  - For cost, 2-8-9-6 should win (again, we'll configure it as such)
 *  - For external, 2-3-4-5-6 should win (based on configuration)
 *
 * To run just the tests in this file:
 *
 *      $> make atdd tags="@PCE"
 *
 * Other Notes:
 *  - Look at FlowPathTest for some of the edge cases related to not enough bandwidth, etc.
 */
public class PathComputationTest {
    private static final String fileName = "topologies/pce-spider-topology.json";

    @Given("^a spider web topology with endpoints A and B$")
    public void a_multi_path_topology() throws Throwable {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String json = new String(Files.readAllBytes(file.toPath()));
        assertTrue(TopologyHelp.CreateMininetTopology(json));
        // Should also wait for some of this to come up

    }

    @When("^a flow request is made between A and B with default$")
    public void a_flow_request_is_made_between_A_and_B_with_default() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the path matches the default$")
    public void the_path_matches_the_default() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the path between A and B is pingable$")
    public void the_path_between_A_and_B_is_pingable() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^a flow request is made between A and B with hops$")
    public void a_flow_request_is_made_between_A_and_B_with_hops() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the path matches the hops$")
    public void the_path_matches_the_hops() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^a flow request is made between A and B with cost(\\d+)$")
    public void a_flow_request_is_made_between_A_and_B_with_cost(int arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the path matches the cost(\\d+)$")
    public void the_path_matches_the_cost(int arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

}