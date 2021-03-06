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

package org.openkilda.wfm.topology.network.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.openkilda.model.SwitchId;
import org.openkilda.wfm.topology.network.model.Endpoint;
import org.openkilda.wfm.topology.network.model.LinkStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NetworkPortServiceTest {
    @Mock
    private IPortCarrier carrier;

    private final SwitchId alphaDatapath = new SwitchId(1);

    @Before
    public void setup() {
        resetMocks();
    }

    private void resetMocks() {
        reset(carrier);
    }

    @Test
    public void newPort() {
        NetworkPortService service = new NetworkPortService(carrier);
        Endpoint port1 = Endpoint.of(alphaDatapath, 1);
        Endpoint port2 = Endpoint.of(alphaDatapath, 2);

        service.setup(port1, null);
        service.updateOnlineMode(port1, false);
        service.setup(port2, null);
        service.updateOnlineMode(port2, false);

        service.remove(port1);
        service.remove(port2);

        verify(carrier).setupUniIslHandler(Endpoint.of(alphaDatapath, 1), null);
        verify(carrier).removeUniIslHandler(Endpoint.of(alphaDatapath, 1));

        verify(carrier).setupUniIslHandler(Endpoint.of(alphaDatapath, 2), null);
        verify(carrier).removeUniIslHandler(Endpoint.of(alphaDatapath, 2));

        //System.out.println(mockingDetails(carrier).printInvocations());
    }

    @Test
    public void inOperationalUpDownPort() {
        NetworkPortService service = new NetworkPortService(carrier);
        Endpoint port1 = Endpoint.of(alphaDatapath, 1);
        Endpoint port2 = Endpoint.of(alphaDatapath, 2);

        service.setup(port1, null);
        service.updateOnlineMode(port1, true);
        service.setup(port2, null);
        service.updateOnlineMode(port2, true);

        verify(carrier).setupUniIslHandler(Endpoint.of(alphaDatapath, 2), null);

        resetMocks();

        // Port 1 from Unknown to UP then DOWN

        service.updateLinkStatus(port1, LinkStatus.UP);
        service.updateLinkStatus(port1, LinkStatus.DOWN);

        verify(carrier).enableDiscoveryPoll(Endpoint.of(alphaDatapath, 1));
        verify(carrier).disableDiscoveryPoll(Endpoint.of(alphaDatapath, 1));
        verify(carrier).notifyPortPhysicalDown(Endpoint.of(alphaDatapath, 1));

        resetMocks();

        // Port 2 from Unknown to DOWN then UP

        service.updateLinkStatus(port2, LinkStatus.DOWN);
        service.updateLinkStatus(port2, LinkStatus.UP);

        verify(carrier).notifyPortPhysicalDown(Endpoint.of(alphaDatapath, 2));
        verify(carrier).enableDiscoveryPoll(Endpoint.of(alphaDatapath, 2));

        //System.out.println(mockingDetails(carrier).printInvocations());
    }

    @Test
    public void inUnOperationalUpDownPort() {
        NetworkPortService service = new NetworkPortService(carrier);
        Endpoint port1 = Endpoint.of(alphaDatapath, 1);

        service.setup(port1, null);
        service.updateOnlineMode(port1, true);

        resetMocks();

        service.updateOnlineMode(port1, false);

        // Port 1 from Unknown to UP then DOWN

        service.updateLinkStatus(port1, LinkStatus.UP);
        service.updateLinkStatus(port1, LinkStatus.DOWN);

        verify(carrier, never()).enableDiscoveryPoll(Endpoint.of(alphaDatapath, 1));
        verify(carrier, never()).disableDiscoveryPoll(Endpoint.of(alphaDatapath, 1));
        verify(carrier, never()).notifyPortPhysicalDown(Endpoint.of(alphaDatapath, 1));

        resetMocks();

        service.updateOnlineMode(port1, true);

        service.updateLinkStatus(port1, LinkStatus.UP);
        service.updateLinkStatus(port1, LinkStatus.DOWN);

        verify(carrier).enableDiscoveryPoll(Endpoint.of(alphaDatapath, 1));
        verify(carrier).disableDiscoveryPoll(Endpoint.of(alphaDatapath, 1));
        verify(carrier).notifyPortPhysicalDown(Endpoint.of(alphaDatapath, 1));

        // System.out.println(mockingDetails(carrier).printInvocations());
    }
}
