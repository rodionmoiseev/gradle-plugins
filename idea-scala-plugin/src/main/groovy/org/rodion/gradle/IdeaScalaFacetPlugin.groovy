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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

/**
 *
 * @author rodion
 */
class IdeaScalaFacetPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(IdeaPlugin.class)
        project.plugins.apply(ScalaPlugin.class)
        project.plugins.apply(IdeaScalaBasePlugin.class)

        configureConfigurations(project)
        configureIdeaModule(project)
    }

    def configureConfigurations(Project project) {
        def scalaApiConf = project.configurations.getByName(IdeaScalaBasePlugin.SCALA_API_CONFIGURATION_NAME)
        project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).extendsFrom(scalaApiConf)
    }

    def configureIdeaModule(Project project) {
        project.idea.module {
            //Already included as part of the 'gradle-scala-library' project library
            //so can be omitted in the list of exported dependencies
            scopes.COMPILE.minus += project.configurations.getByName(IdeaScalaBasePlugin.SCALA_API_CONFIGURATION_NAME)

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
}