/* Copyright 2019 Telstra Open Source
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

package org.openkilda.wfm.topology.flowhs.fsm.reroute.actions;

import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteContext;
import org.openkilda.wfm.topology.flowhs.fsm.reroute.FlowRerouteFsm;

import lombok.extern.slf4j.Slf4j;
import org.squirrelframework.foundation.fsm.AnonymousAction;

@Slf4j
public class HandleNotRemovedPathsAction
        extends AnonymousAction<FlowRerouteFsm, FlowRerouteFsm.State, FlowRerouteFsm.Event, FlowRerouteContext> {

    @Override
    public void execute(FlowRerouteFsm.State from, FlowRerouteFsm.State to,
                        FlowRerouteFsm.Event event, FlowRerouteContext context, FlowRerouteFsm stateMachine) {
        if (stateMachine.getOldPrimaryForwardPath() != null && stateMachine.getOldPrimaryReversePath() != null) {
            log.warn("Failed to remove paths {}/{}", stateMachine.getOldPrimaryForwardPath(),
                    stateMachine.getOldPrimaryReversePath());
        }
        if (stateMachine.getOldProtectedForwardPath() != null
                && stateMachine.getOldProtectedReversePath() != null) {
            log.warn("Failed to remove paths {}/{}", stateMachine.getOldProtectedForwardPath(),
                    stateMachine.getOldProtectedReversePath());
        }
    }
}
