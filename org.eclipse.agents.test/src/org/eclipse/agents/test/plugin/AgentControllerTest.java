/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.agents.test.plugin;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.services.agent.GeminiService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.agent.KiroService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * Unit tests for AgentController registration.
 * Tests verify that KiroService is properly registered alongside other agents.
 * 
 * Requirements validated:
 * - 2.1: AgentController includes KiroService in agentServices array
 * - 2.2: getAgents() returns array containing KiroService
 * - 2.3: KiroService coexists with GeminiService
 */
@TestInstance(Lifecycle.PER_CLASS)
public class AgentControllerTest {

    private AgentController agentController;

    @BeforeAll
    public void setup() {
        // Disable tracing to avoid Eclipse runtime dependencies in unit tests
        Tracer.disableTracing = true;
        agentController = AgentController.instance();
    }

    /**
     * Test that KiroService is in the getAgents() array.
     * Validates: Requirements 2.1, 2.2
     */
    @Test
    public void testKiroServiceIsRegistered() {
        IAgentService[] agents = agentController.getAgents();
        boolean foundKiro = false;
        
        for (IAgentService agent : agents) {
            if (agent instanceof KiroService) {
                foundKiro = true;
                break;
            }
        }
        
        Assert.assertTrue("KiroService should be registered in AgentController", foundKiro);
    }

    /**
     * Test that GeminiService is still in the getAgents() array.
     * Validates: Requirement 2.3
     */
    @Test
    public void testGeminiServiceIsRegistered() {
        IAgentService[] agents = agentController.getAgents();
        boolean foundGemini = false;
        
        for (IAgentService agent : agents) {
            if (agent instanceof GeminiService) {
                foundGemini = true;
                break;
            }
        }
        
        Assert.assertTrue("GeminiService should be registered in AgentController", foundGemini);
    }

    /**
     * Test that both GeminiService and KiroService coexist in the agents array.
     * Validates: Requirement 2.3
     */
    @Test
    public void testBothServicesCoexist() {
        IAgentService[] agents = agentController.getAgents();
        boolean foundKiro = false;
        boolean foundGemini = false;
        
        for (IAgentService agent : agents) {
            if (agent instanceof KiroService) {
                foundKiro = true;
            }
            if (agent instanceof GeminiService) {
                foundGemini = true;
            }
        }
        
        Assert.assertTrue("Both KiroService and GeminiService should coexist", 
                foundKiro && foundGemini);
    }

    /**
     * Test that getAgents() returns at least 2 agents.
     * Validates: Requirements 2.1, 2.2, 2.3
     */
    @Test
    public void testAgentsArrayHasAtLeastTwoAgents() {
        IAgentService[] agents = agentController.getAgents();
        Assert.assertTrue("getAgents() should return at least 2 agents", 
                agents.length >= 2);
    }
}
