package com.github.rodionmoiseev.gradle.plugins

import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 * @author rodexion
 */
class RunConfigurationTest {
    @Test
    void requiresMainClassCheckTest(){
        def runConf = new RunConfiguration()
        assertTrue(runConf.requiresMainClass)
        runConf.isDefault = true
        assertFalse(runConf.requiresMainClass)
        runConf.isDefault = false
        runConf.type = "my-custom-type"
        assertFalse(runConf.requiresMainClass)
    }
}
