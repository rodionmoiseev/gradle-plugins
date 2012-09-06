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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin

/**
 *
 * @author rodion
 */
class IdeaUtilsBasePlugin implements Plugin<Project> {
    static final String RUN_CONFIG_EXTENSION_NAME = "runConfigurations"
    static final String VCS_EXTENSION_NAME = "vcs"
    static final String COPYRIGHT_EXTENSION_NAME = "copyright"
    static final String MISC_EXTENSION_NAME = "misc"

    @Override
    void apply(Project project) {
        project.plugins.apply(IdeaPlugin.class)
        project.idea.project.extensions.create(VCS_EXTENSION_NAME, VcsExtension)
        project.idea.project.extensions.create(COPYRIGHT_EXTENSION_NAME, CopyrightExtension)
        project.idea.project.extensions.create(MISC_EXTENSION_NAME, MiscExtension)
        def runConfigs = project.container(RunConfiguration)
        runConfigs.all { rootProject = project }
        project.idea.project.extensions.add(RUN_CONFIG_EXTENSION_NAME, runConfigs)
    }
}

enum RunConfigType {
    Application("Application"),
    JUnit("Junit"),
    Specs("SpecsRunConfiguration");

    private final String internalType;

    def RunConfigType(String internalType) {
        this.internalType = internalType;
    }

    public String getInternalType() {
        return internalType
    }
}