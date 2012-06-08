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
import org.gradle.api.XmlProvider
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.scala.ScalaBasePlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

/**
 *
 * @author rodion
 */
class IdeaScalaProjectPlugin implements Plugin<Project> {
    private static final Logger logger = Logging.getLogger(IdeaScalaProjectPlugin)

    @Override
    void apply(Project project) {
        project.plugins.apply(IdeaPlugin.class)
        project.plugins.apply(IdeaScalaBasePlugin.class)

        createScalaToolsConfIfDoesNotExist(project)
        configureIdeaProject(project)
    }

    def configureIdeaProject(Project project) {
        if (null == project.idea.project) {
            //This appears to be an IntelliJ sub-module. Inform the user:
            logger.warn("WARN: Could not configure global Scala compiler settings " +
                    "for '${project.name}' because it not the parent project. 'idea-scala-project' plugin can " +
                    "only be configured for the parent (master) project.")
            return
        }

        project.idea.project.ipr.withXml { XmlProvider provider ->
            checkScalaToolsIsNotEmpty(project)

            //Scala Compiler Settings
            def fsc = project.ideaScala.compiler.fsc
            def scalacSettingsComp = provider.node.appendNode('component')
            scalacSettingsComp.@name = 'ScalacSettings'
            ['COMPILER_LIBRARY_NAME': project.ideaScala.scalaCompilerLibName,
                    'COMPILER_LIBRARY_LEVEL': 'Project',
                    'SCALAC_BEFORE': String.valueOf(project.ideaScala.compiler.scalacBefore),
                    'MAXIMUM_HEAP_SIZE': String.valueOf(fsc.maxHeapSize),
                    'VM_PARAMETERS': fsc.vmParameters,
                    'IDLE_TIMEOUT': String.valueOf(fsc.idleTimeout),
                    'FSC_OPTIONS': fsc.serverOptions,
                    'INTERNAL_SERVER': String.valueOf(!fsc.server.enable),
                    'REMOTE_HOST': fsc.server.host,
                    'REMOTE_PORT': (fsc.server.port != -1 ? String.valueOf(fsc.server.port) : ''),
                    'SHARED_DIRECTORY': fsc.server.sharedDirectory].each { name, value ->
                scalacSettingsComp.appendNode('option', ['name': name, 'value': value])
            }

            //Add Scala compiler and public API to project libraries
            def libraryTableComp = provider.node.appendNode('component')
            libraryTableComp.@name = 'libraryTable'
            def scalaCompilerLibNode = libraryTableComp.appendNode('library')
            scalaCompilerLibNode.@name = project.ideaScala.scalaCompilerLibName
            def scalaCompilerLibClassesNode = scalaCompilerLibNode.appendNode('CLASSES')
            project.configurations.getByName(ScalaBasePlugin.SCALA_TOOLS_CONFIGURATION_NAME).files.each { scalaCompilerLibJar ->
                scalaCompilerLibClassesNode.appendNode('root', [url: "jar://${scalaCompilerLibJar}!/"])
            }
            def scalaAPILibNode = libraryTableComp.appendNode('library')
            scalaAPILibNode.@name = project.ideaScala.scalaLibraryLibName
            def scalaAPILibClassesNode = scalaAPILibNode.appendNode('CLASSES')
            project.configurations.getByName(IdeaScalaBasePlugin.SCALA_API_CONFIGURATION_NAME).files.each { scalaAPILibJar ->
                scalaAPILibClassesNode.appendNode('root', [url: "jar://${scalaAPILibJar}!/"])
            }

            //Enable Scala Type-aware highlighting (remove this chunk of code to disable)
            def typeAwareHLVal = String.valueOf(project.ideaScala.typeAwareHighlighting)
            def highlightingAdvisorComp = provider.node.appendNode('component')
            highlightingAdvisorComp.@name = 'HighlightingAdvisor'
            ['SUGGEST_TYPE_AWARE_HIGHLIGHTING': 'false',
                    'TYPE_AWARE_HIGHLIGHTING_ENABLED': typeAwareHLVal].each { name, value ->
                highlightingAdvisorComp.appendNode('option', ['name': name, 'value': value])
            }
        }
    }

    def createScalaToolsConfIfDoesNotExist(Project project) {
        def scalaTools = ScalaBasePlugin.SCALA_TOOLS_CONFIGURATION_NAME
        try {
            project.configurations.getByName(scalaTools)
        } catch (UnknownConfigurationException) {
            project.configurations.add(scalaTools)
                    .setVisible(false)
                    .setTransitive(true)
                    .setDescription("Scala compiler libraries to be included in the 'scala-compiler' library in IntelilJ")
        }
    }

    def checkScalaToolsIsNotEmpty(Project project) {
        def scalaTools = ScalaBasePlugin.SCALA_TOOLS_CONFIGURATION_NAME
        if (project.configurations.getByName(scalaTools).dependencies.isEmpty()) {
            logger.warn("WARN: '${scalaTools}' configuration for project '${project.name}' is empty. " +
                    "Your module library '" + project.ideaScala.scalaCompilerLibName + "' will appear empty. " +
                    "Please setup '${scalaTools}' as described here: " +
                    "http://gradle.org/docs/current/userguide/scala_plugin.html#N12952")
        }
    }
}