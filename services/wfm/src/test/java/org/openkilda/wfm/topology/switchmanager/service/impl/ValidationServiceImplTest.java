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

package org.openkilda.wfm.topology.switchmanager.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.openkilda.messaging.info.meter.MeterEntry;
import org.openkilda.model.Flow;
import org.openkilda.model.FlowSegment;
import org.openkilda.model.Switch;
import org.openkilda.model.SwitchId;
import org.openkilda.persistence.PersistenceManager;
import org.openkilda.persistence.repositories.FlowRepository;
import org.openkilda.persistence.repositories.FlowSegmentRepository;
import org.openkilda.persistence.repositories.RepositoryFactory;
import org.openkilda.wfm.topology.switchmanager.model.ValidateMetersResult;
import org.openkilda.wfm.topology.switchmanager.model.ValidateRulesResult;
import org.openkilda.wfm.topology.switchmanager.service.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.parboiled.common.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ValidationServiceImplTest {

    private static final SwitchId SWITCH_ID_A = new SwitchId("00:10");
    private static final SwitchId SWITCH_ID_B = new SwitchId("00:20");
    private static final long MIN_BURST_SIZE_IN_KBITS = 1024;
    private static final double BURST_COEFFICIENT = 1.05;

    @Test
    public void validateRulesEmpty() {
        ValidationService validationService = new ValidationServiceImpl(persistenceManager().build());
        ValidateRulesResult response = validationService.validateRules(SWITCH_ID_A, new HashSet<>());
        assertTrue(response.getMissingRules().isEmpty());
        assertTrue(response.getProperRules().isEmpty());
        assertTrue(response.getExcessRules().isEmpty());
    }

    @Test
    public void validateRulesSimpleSegmentCookies() {
        ValidationService validationService =
                new ValidationServiceImpl(persistenceManager().withSegmentsCookies(2L, 3L).build());
        ValidateRulesResult response = validationService.validateRules(SWITCH_ID_A, Sets.newHashSet(1L, 2L));
        assertEquals(ImmutableList.of(3L), response.getMissingRules());
        assertEquals(ImmutableList.of(2L), response.getProperRules());
        assertEquals(ImmutableList.of(1L), response.getExcessRules());
    }

    @Test
    public void validateRulesSegmentAndIngressCookies() {
        ValidationService validationService =
                new ValidationServiceImpl(persistenceManager().withSegmentsCookies(2L).withIngressCookies(1L).build());
        ValidateRulesResult response = validationService.validateRules(SWITCH_ID_A, Sets.newHashSet(1L, 2L));
        assertTrue(response.getMissingRules().isEmpty());
        assertEquals(ImmutableSet.of(1L, 2L), new HashSet<>(response.getProperRules()));
        assertTrue(response.getExcessRules().isEmpty());
    }

    @Test
    public void validateRulesIgnoreDefaultRules() {
        ValidationService validationService = new ValidationServiceImpl(persistenceManager().build());
        ValidateRulesResult response =
                validationService.validateRules(SWITCH_ID_A, Sets.newHashSet(0x8000000000000002L));
        assertTrue(response.getMissingRules().isEmpty());
        assertTrue(response.getProperRules().isEmpty());
        assertTrue(response.getExcessRules().isEmpty());
    }

    @Test
    public void validateMetersEmpty() {
        ValidationService validationService = new ValidationServiceImpl(persistenceManager().build());
        ValidateMetersResult response = validationService.validateMeters(SWITCH_ID_A, new ArrayList<>(),
                MIN_BURST_SIZE_IN_KBITS, BURST_COEFFICIENT);
        assertTrue(response.getMissingMeters().isEmpty());
        assertTrue(response.getMisconfiguredMeters().isEmpty());
        assertTrue(response.getProperMeters().isEmpty());
        assertTrue(response.getExcessMeters().isEmpty());
    }

    @Test
    public void validateMetersProperMeters() {
        ValidationService validationService = new ValidationServiceImpl(persistenceManager().build());
        ValidateMetersResult response = validationService.validateMeters(SWITCH_ID_B,
                Lists.newArrayList(new MeterEntry(32, 10000, 10500, "OF_13", new String[]{"KBPS", "BURST", "STATS"})),
                MIN_BURST_SIZE_IN_KBITS, BURST_COEFFICIENT);
        assertTrue(response.getMissingMeters().isEmpty());
        assertTrue(response.getMisconfiguredMeters().isEmpty());
        assertFalse(response.getProperMeters().isEmpty());
        assertEquals(32L, (long) response.getProperMeters().get(0).getMeterId());
        assertTrue(response.getExcessMeters().isEmpty());
    }

    @Test
    public void validateMetersMisconfiguredMeters() {
        ValidationService validationService = new ValidationServiceImpl(persistenceManager().build());
        ValidateMetersResult response = validationService.validateMeters(SWITCH_ID_B,
                Lists.newArrayList(new MeterEntry(32, 10000, 1050, "OF_13", new String[]{"KBPS", "BURST", "STATS"})),
                MIN_BURST_SIZE_IN_KBITS, BURST_COEFFICIENT);
        assertTrue(response.getMissingMeters().isEmpty());
        assertFalse(response.getMisconfiguredMeters().isEmpty());
        assertEquals(1050L, (long) response.getMisconfiguredMeters().get(0).getActual().getBurstSize());
        assertEquals(10500L, (long) response.getMisconfiguredMeters().get(0).getExpected().getBurstSize());
        assertTrue(response.getProperMeters().isEmpty());
        assertTrue(response.getExcessMeters().isEmpty());
    }

    @Test
    public void validateMetersMissingAndExcessMeters() {
        ValidationService validationService = new ValidationServiceImpl(persistenceManager().build());
        ValidateMetersResult response = validationService.validateMeters(SWITCH_ID_B,
                Lists.newArrayList(new MeterEntry(33, 10000, 10500, "OF_13", new String[]{"KBPS", "BURST", "STATS"})),
                MIN_BURST_SIZE_IN_KBITS, BURST_COEFFICIENT);
        assertFalse(response.getMissingMeters().isEmpty());
        assertEquals(32L, (long) response.getMissingMeters().get(0).getMeterId());
        assertTrue(response.getMisconfiguredMeters().isEmpty());
        assertTrue(response.getProperMeters().isEmpty());
        assertFalse(response.getExcessMeters().isEmpty());
        assertEquals(33L, (long) response.getExcessMeters().get(0).getMeterId());
    }

    @Test
    public void validateMetersIgnoreDefaultMeters() {
        ValidationService validationService = new ValidationServiceImpl(persistenceManager().build());
        ValidateMetersResult response = validationService.validateMeters(SWITCH_ID_B,
                Lists.newArrayList(new MeterEntry(2, 0, 0, null, null), new MeterEntry(3, 0, 0, null, null)),
                MIN_BURST_SIZE_IN_KBITS, BURST_COEFFICIENT);
        assertFalse(response.getMissingMeters().isEmpty());
        assertEquals(1, response.getMissingMeters().size());
        assertTrue(response.getMisconfiguredMeters().isEmpty());
        assertTrue(response.getProperMeters().isEmpty());
        assertTrue(response.getExcessMeters().isEmpty());
    }

    private PersistenceManagerBuilder persistenceManager() {
        return new PersistenceManagerBuilder();
    }

    private static class PersistenceManagerBuilder {
        private long[] segmentsCookies = new long[0];
        private long[] ingressCookies = new long[0];

        private PersistenceManagerBuilder withSegmentsCookies(long... cookies) {
            segmentsCookies = cookies;
            return this;
        }

        private PersistenceManagerBuilder withIngressCookies(long... cookies) {
            ingressCookies = cookies;
            return this;
        }

        private PersistenceManager build() {
            List<FlowSegment> flowSegments = new ArrayList<>(segmentsCookies.length);
            for (long cookie: segmentsCookies) {
                FlowSegment flowSegment = mock(FlowSegment.class);
                when(flowSegment.getCookie()).thenReturn(cookie);
                flowSegments.add(flowSegment);
            }
            List<Flow> flows = new ArrayList<>(ingressCookies.length);
            for (long cookie: ingressCookies) {
                Flow flow = mock(Flow.class);
                when(flow.getCookie()).thenReturn(cookie);
                flows.add(flow);
            }

            Switch switchA = Switch.builder()
                    .switchId(SWITCH_ID_A)
                    .description("Nicira, Inc. OF_13 2.5.5")
                    .build();
            Switch switchB = Switch.builder()
                    .switchId(SWITCH_ID_B)
                    .description("Nicira, Inc. OF_13 2.5.5")
                    .build();
            Flow flowA = Flow.builder()
                    .srcSwitch(switchB)
                    .destSwitch(switchA)
                    .bandwidth(10000)
                    .meterId(32)
                    .build();

            FlowSegmentRepository flowSegmentRepository = mock(FlowSegmentRepository.class);
            when(flowSegmentRepository.findByDestSwitchId(any())).thenReturn(flowSegments);
            FlowRepository flowRepository = mock(FlowRepository.class);
            when(flowRepository.findBySrcSwitchId(SWITCH_ID_B)).thenReturn(Lists.newArrayList(flowA));
            when(flowRepository.findBySrcSwitchId(SWITCH_ID_A)).thenReturn(flows);
            RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
            when(repositoryFactory.createFlowSegmentRepository()).thenReturn(flowSegmentRepository);
            when(repositoryFactory.createFlowRepository()).thenReturn(flowRepository);
            PersistenceManager persistenceManager = mock(PersistenceManager.class);
            when(persistenceManager.getRepositoryFactory()).thenReturn(repositoryFactory);
            return persistenceManager;
        }
    }
}
