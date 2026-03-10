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

import java.io.File;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.services.agent.AbstractService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.agent.KiroService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * Unit tests for KiroService.
 * Tests verify the basic identity and configuration methods of the service.
 * 
 * Requirements validated:
 * - 1.1: KiroService extends AbstractService
 * - 1.2: getName() returns "Kiro"
 * - 1.3: getId() returns "kiro"
 * - 1.4: getFolderName() returns "kiro"
 * - 1.5: getDefaultStartupCommand() returns expected array
 */
@TestInstance(Lifecycle.PER_CLASS)
public class KiroServiceTest {

    private KiroService kiroService;

    @BeforeAll
    public void setup() {
        // Disable tracing to avoid Eclipse runtime dependencies in unit tests
        Tracer.disableTracing = true;
        kiroService = new KiroService();
    }

    /**
     * Test that KiroService extends AbstractService.
     * Validates: Requirement 1.1
     */
    @Test
    public void testKiroServiceExtendsAbstractService() {
        Assert.assertTrue("KiroService should extend AbstractService",
                kiroService instanceof AbstractService);
    }

    /**
     * Test that KiroService implements IAgentService.
     * Validates: Requirement 1.1
     */
    @Test
    public void testKiroServiceImplementsIAgentService() {
        Assert.assertTrue("KiroService should implement IAgentService",
                kiroService instanceof IAgentService);
    }

    /**
     * Test that getName() returns "Kiro".
     * Validates: Requirement 1.2
     */
    @Test
    public void testGetNameReturnsKiro() {
        Assert.assertEquals("getName() should return 'Kiro'",
                "Kiro", kiroService.getName());
    }

    /**
     * Test that getId() returns "kiro".
     * Validates: Requirement 1.3
     */
    @Test
    public void testGetIdReturnsKiro() {
        Assert.assertEquals("getId() should return 'kiro'",
                "kiro", kiroService.getId());
    }

    /**
     * Test that getFolderName() returns "kiro".
     * Validates: Requirement 1.4
     */
    @Test
    public void testGetFolderNameReturnsKiro() {
        Assert.assertEquals("getFolderName() should return 'kiro'",
                "kiro", kiroService.getFolderName());
    }

    /**
     * Test that getDefaultStartupCommand() returns the expected command array.
     * Validates: Requirement 1.5
     */
    @Test
    public void testGetDefaultStartupCommandReturnsExpectedArray() {
        String userHome = System.getProperty("user.home");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String expectedPath;
        if (isWindows) {
            expectedPath = userHome + File.separator + ".kiro" + File.separator + "bin" + File.separator + "kiro-cli.exe";
        } else {
            expectedPath = userHome + "/.local/bin/kiro-cli";
        }
        String[] expected = new String[] { expectedPath, "acp" };
        String[] actual = kiroService.getDefaultStartupCommand();
        
        Assert.assertArrayEquals("getDefaultStartupCommand() should return expected array",
                expected, actual);
    }

    /**
     * Test that getDefaultStartupCommand() returns an array with exactly 2 elements.
     * Validates: Requirement 1.5
     */
    @Test
    public void testGetDefaultStartupCommandHasTwoElements() {
        String[] command = kiroService.getDefaultStartupCommand();
        Assert.assertEquals("getDefaultStartupCommand() should return array with 2 elements",
                2, command.length);
    }

    /**
     * Test that getDefaultStartupCommand() second element is "acp".
     * Validates: Requirement 1.5
     */
    @Test
    public void testGetDefaultStartupCommandSecondElementIsAcp() {
        String[] command = kiroService.getDefaultStartupCommand();
        Assert.assertEquals("Second element should be 'acp'",
                "acp", command[1]);
    }
}
