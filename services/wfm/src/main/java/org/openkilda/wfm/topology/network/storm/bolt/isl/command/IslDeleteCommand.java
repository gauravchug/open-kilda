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

package org.openkilda.wfm.topology.network.storm.bolt.isl.command;

import org.openkilda.messaging.info.event.DeactivateIslInfoData;
import org.openkilda.wfm.topology.network.model.Endpoint;
import org.openkilda.wfm.topology.network.model.IslReference;
import org.openkilda.wfm.topology.network.storm.bolt.isl.IslHandler;

public class IslDeleteCommand extends IslCommand {

    public IslDeleteCommand(DeactivateIslInfoData payload) {
        super(new Endpoint(payload.getSource()),
                new IslReference(new Endpoint(payload.getSource()),
                        new Endpoint(payload.getDestination())));
    }

    @Override
    public void apply(IslHandler handler) {
        handler.processIslRemove(getReference());
    }
}
