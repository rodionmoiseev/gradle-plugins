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

/**
 *
 * @author rodion
 */
class IdeaScalaBasePlugin implements Plugin<Project> {
    public static final SCALA_API_CONFIGURATION_NAME = 'scalaApi'

    @Override
    void apply(Project project) {
        project.extensions.create("ideaScala", IdeaScalaPluginExtension, project)
        project.ideaScala.extensions.create("compiler", IdeaScalaCompilerSettingsExtension)
        project.ideaScala.compiler.extensions.create("fsc", IdeaScalaFscSettingsExtension)
        project.ideaScala.compiler.fsc.extensions.create("server", IdeaScalaFscServerSettingsExtension)
        configureConfigurations(project)
    }

    def configureConfigurations(Project project) {
        project.configurations.add(SCALA_API_CONFIGURATION_NAME)
                .setVisible(false)
                .setTransitive(true)
                .setDescription("Compile-time Scala libraries as part of 'scala-library' library in IntelilJ")
    }
}

class IdeaScalaPluginExtension {
    private final Project project
    private String compilerLibName = null
    private String libraryLibName = null

    boolean typeAwareHighlighting = true

    def IdeaScalaPluginExtension(Project project) {
        this.project = project
    }

    String getScalaCompilerLibName() {
        if (null != compilerLibName) {
            return compilerLibName
        }
        return "gradle-scala-compiler-${getScalaVersion(project)}"
    }

    void setScalaCompilerLibName(String compilerLibName) {
        this.compilerLibName = compilerLibName
    }

    String getScalaLibraryLibName() {
        if (null != libraryLibName) {
            return libraryLibName
        }
        return "gradle-scala-library-${getScalaVersion(project)}"
    }

    void setScalaLibraryLibName(String libraryLibName) {
        this.libraryLibName = libraryLibName
    }

    private String getScalaVersion(Project project) {
        def scalaApiDep = project.configurations.getByName(IdeaScalaBasePlugin.SCALA_API_CONFIGURATION_NAME)
                .dependencies.find { it.name == "scala-library" }
        if (null == scalaApiDep) {
            throw new IdeaScalaPluginException("'scala-library' dependency was not found for project '${project.name}'. " +
                    "Please add 'scala-library' dependency to the '${IdeaScalaBasePlugin.SCALA_API_CONFIGURATION_NAME}' " +
                    "configuration in your dependencies clause.")
        }
        return scalaApiDep.version
    }
}

class IdeaScalaCompilerSettingsExtension {
    int maxHeapSize = 512
    String vmParameters = '-Xss1m -server'
    boolean scalacBefore = true
}

class IdeaScalaFscSettingsExtension {
    boolean enable = false
    int maxHeapSize = 512
    String vmParameters = '-Xms128m -Xss1m -server'
    int idleTimeout = 0
    String serverOptions = ''
}

class IdeaScalaFscServerSettingsExtension {
    boolean enable = false
    String host = ''
    int port = -1
    String sharedDirectory = ''
}