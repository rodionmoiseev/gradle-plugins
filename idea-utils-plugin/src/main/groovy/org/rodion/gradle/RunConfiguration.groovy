/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.rodion.gradle

import org.gradle.api.Project

/**
 * <p>Created: 9/5/12 6:37 PM</p>
 * @author rodion
 */
class RunConfiguration {
    //For internal use
    private final String _configName
    private Project project = null

    Project module = null
    boolean isDefault = false
    String type = RunConfigType.Application.name()
    String name = null //defaults to run configuration closure name
    String mainClass = null //required
    String vmOptions = ""
    String programArguments = ""
    private File _workingDirectory = null //set to root project directory by default
    List<String> buildArtifacts = []

    public RunConfiguration(String name) {
        this._configName = name
        this.name = name
    }

    def setRootProject(Project project) {
        this.project = project
    }

    RunConfigType getRunConfigType() {
        return RunConfigType.values().find { it.name().equals(type)}
    }

    String getConfigName() {
        return _configName
    }

    File getWorkingDirectory() {
        return getOrModuleOrDefault(_workingDirectory, module?.projectDir, project.projectDir)
    }

    def setWorkingDirectory(File workingDirectory) {
        _workingDirectory = workingDirectory
    }

    String getUseModuleClasspath() {
        return getOrModuleOrDefault(null, module?.name, ""/*only valid for single-module projects*/)
    }

    private <T> T getOrModuleOrDefault(T value, T moduleValue, T defaultValue) {
        if (null != value) {
            return value
        }
        if (null != moduleValue) {
            return moduleValue
        }
        return defaultValue
    }
}
