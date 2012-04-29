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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaBasePlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

/**
 *
 * @author rodion
 */
class IdeaScalaPlugin implements Plugin<Project> {
    private static final Logger logger = Logging.getLogger(IdeaScalaPlugin)
    public static final SCALA_API_CONFIGURATION_NAME = 'scalaApi'

    @Override
    void apply(Project project) {
        project.plugins.apply(ScalaPlugin.class)
        project.plugins.apply(IdeaPlugin.class)

        project.extensions.create("ideaScala", IdeaScalaPluginExtension, project)
        project.ideaScala.extensions.create("compiler", IdeaScalaCompilerSettingsExtension)
        project.ideaScala.compiler.extensions.create("fsc", IdeaScalaFscSettingsExtension)
        project.ideaScala.compiler.fsc.extensions.create("server", IdeaScalaFscServerSettingsExtension)
        configureConfigurations(project)
        configureIdeaProject(project)
        configureIdeaModule(project)
    }

    def configureConfigurations(Project project) {
        def scalaApiConf = project.configurations.add(SCALA_API_CONFIGURATION_NAME)
                .setVisible(false)
                .setTransitive(true)
                .setDescription("Compile-time Scala libraries as part of 'scala-library' library in IntelilJ")
        project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).extendsFrom(scalaApiConf)
    }

    def configureIdeaProject(Project project) {
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
            project.configurations.scalaTools.files.each { scalaCompilerLibJar ->
                scalaCompilerLibClassesNode.appendNode('root', [url: "jar://${scalaCompilerLibJar}!/"])
            }
            def scalaAPILibNode = libraryTableComp.appendNode('library')
            scalaAPILibNode.@name = project.ideaScala.scalaLibraryLibName
            def scalaAPILibClassesNode = scalaAPILibNode.appendNode('CLASSES')
            project.configurations.getByName(SCALA_API_CONFIGURATION_NAME).files.each { scalaAPILibJar ->
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

    def configureIdeaModule(Project project) {
        project.idea.module {
            //Already included as part of the 'gradle-scala-library' project library
            //so can be omitted in the list of exported dependencies
            scopes.COMPILE.minus += project.configurations.getByName(SCALA_API_CONFIGURATION_NAME)

            //Additions to the *.iml file
            iml.withXml { provider ->
                //Add Scala Facet
                def facetComp = provider.node.appendNode('component')
                facetComp.@name = 'FacetManager'
                def facetNode = facetComp.appendNode('facet', [type: 'scala', name: 'Scala'])
                def facetConfNode = facetNode.appendNode('configuration')
                ['compilerLibraryLevel': 'Project',
                        'compilerLibraryName': project.ideaScala.scalaCompilerLibName,
                        'fsc': String.valueOf(project.ideaScala.compiler.fsc.enable),
                        'maximumHeapSize': String.valueOf(project.ideaScala.compiler.maxHeapSize),
                        'vmOptions': project.ideaScala.compiler.vmParameters].each {name, value ->
                    facetConfNode.appendNode('option', ['name': name, 'value': value])
                }

                //Add project dependency on 'gradle-scala-library'
                def moduleRootManagerComp = provider.node.component.find { it.@name == 'NewModuleRootManager' }
                moduleRootManagerComp.appendNode('orderEntry',
                        [type: 'library', name: project.ideaScala.scalaLibraryLibName, level: 'project'])
            }
        }
    }

    def checkScalaToolsIsNotEmpty(Project project) {
        def scalaTools = ScalaBasePlugin.SCALA_TOOLS_CONFIGURATION_NAME
        if (project.configurations.getByName(scalaTools).dependencies.isEmpty()) {
            logger.warn("WARN: '${scalaTools}' configuration is empty. Your module library '" +
                    project.ideaScala.scalaCompilerLibName + "' will appear empty. " +
                    "Please setup '${scalaTools}' as described here: " +
                    "http://gradle.org/docs/current/userguide/scala_plugin.html#N12952")
        }
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
        def scalaApiDep = project.configurations.getByName(IdeaScalaPlugin.SCALA_API_CONFIGURATION_NAME)
                .dependencies.find { it.name == "scala-library" }
        if (null == scalaApiDep) {
            throw new IdeaScalaPluginException("'scala-library' dependency was not found. " +
                    "Please add 'scala-library' dependency to the '${IdeaScalaPlugin.SCALA_API_CONFIGURATION_NAME}' " +
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

class IdeaScalaPluginException extends GradleException {
    IdeaScalaPluginException(String message) {
        super(message)
    }

    IdeaScalaPluginException(String message, Throwable cause) {
        super(message, cause)
    }
}