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
 * @author rodion
 */
class IdeaScalaFacetPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.plugins.apply(IdeaScalaBasePlugin.class)

    project.idea.module.extensions.create("scala", ModuleExtensions.Scala)
    project.idea.module.scala.extensions.create("compiler", ModuleExtensions.ScalaCompiler)

    //Change the default for scala-facet generation:
    // For project default is 'false'
    if (Utils.isRootProject(project)) {
      project.idea.module.scala.enable = false
    }

    configureIdeaModule(project)
  }

  def configureIdeaModule(Project project) {
    project.idea.module {
      //Additions to the *.iml file
      iml.withXml { provider ->
        if (project.idea.module.scala.enable) {
          //Modify Scala Facet node with user settings
          def facetComp = Utils.getOrCreateNode(provider.node, 'component', 'FacetManager')
          def facetNode = Utils.getOrCreateNode(facetComp, 'facet', 'Scala', [type: 'scala'])
          def facetConfNode = Utils.getOrCreateNode(facetNode, 'configuration')
          ['fsc': String.valueOf(project.idea.module.scala.compiler.useFSC),
              'maximumHeapSize': String.valueOf(project.idea.module.scala.compiler.maxHeapSize),
              'vmOptions': project.idea.module.scala.compiler.vmParameters].each { name, value ->
            facetConfNode?.appendNode('option', ['name': name, 'value': value])
          }
        }
      }
    }
  }
}