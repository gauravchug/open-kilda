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

package org.openkilda.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.openkilda.auth.context.ServerContext;
import org.openkilda.auth.model.RequestContext;
import org.openkilda.log.ActivityLogger;
import org.openkilda.model.SwitchInfo;
import org.openkilda.service.SwitchService;
import org.openkilda.test.MockitoExtension;
import org.openkilda.util.TestIslMock;
import org.openkilda.util.TestSwitchMock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class SwitchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApplicationContext context;

    @Mock
    private SwitchService serviceSwitch;

    @Mock
    private ServerContext serverContext;

    @Mock
    ActivityLogger activityLogger;

    @InjectMocks
    private SwitchController switchController;

    @Autowired
    private ObjectMapper objectMapper;
    @SuppressWarnings("unused")
    private static final String switchUuid = "00:00:00:00:00:00:00:01";

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(switchController).build();
        RequestContext requestContext = new RequestContext();
        requestContext.setUserId(TestIslMock.USER_ID);
        when(serverContext.getRequestContext()).thenReturn(requestContext);
    }

    @Test
    public void testGetAllSwitchesDetails() {
        List<SwitchInfo> switchesInfo = new ArrayList<>();
        try {
            when(serviceSwitch.getSwitches(false)).thenReturn(switchesInfo);
            mockMvc.perform(get("/api/switch/list").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            assertTrue(true);
        } catch (Exception exception) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetSwichLinkDetails() {
        List<SwitchInfo> switchesInfo = new ArrayList<>();
        try {
            when(serviceSwitch.getSwitches(false)).thenReturn(switchesInfo);
            mockMvc.perform(get("/api/switch/links").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void testSwitchMaintenance() throws Exception {
        try {
            SwitchInfo switchInfo = new SwitchInfo();
            switchInfo.setSwitchId(TestSwitchMock.SWITCH_ID);
            switchInfo.setUnderMaintenance(TestSwitchMock.MAINTENANCE_STATUS);
            switchInfo.setEvacuate(TestSwitchMock.EVACUATE_STATUS);
            String inputJson = mapToJson(switchInfo);
            mockMvc.perform(
                    post("/api/switch/under-maintenance/{switchId}", TestSwitchMock.SWITCH_ID)
                            .content(inputJson).contentType(MediaType.APPLICATION_JSON)).andExpect(
                    status().isOk());
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

    }

    protected final String mapToJson(final Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
}
