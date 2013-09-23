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

package com.github.rodionmoiseev.gradle.plugins.ideascala

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * @author rodion
 */
class IdeaScalaProjectPlugin implements Plugin<Project> {
  private static final Logger logger = Logging.getLogger(IdeaScalaProjectPlugin)

  @Override
  void apply(Project project) {
    if (!Utils.isRootProject(project)) {
      return
    }

    project.plugins.apply(IdeaScalaBasePlugin.class)

    project.idea.project.extensions.create("scala", ProjectExtensions.Scala)
    project.idea.project.scala.extensions.create("compiler", ProjectExtensions.ScalaCompiler)
    project.idea.project.scala.compiler.extensions.create("fsc", ProjectExtensions.ScalaFsc)
    project.idea.project.scala.compiler.fsc.extensions.create("server", ProjectExtensions.ScalaFscServer)

//    createScalaToolsConfIfDoesNotExist(project)
    configureIdeaProject(project)
  }


  def configureIdeaProject(Project project) {

    project.idea.project.ipr.withXml { XmlProvider provider ->
      //Scala Compiler Settings
      def fsc = project.idea.project.scala.compiler.fsc
      def compilerLibrary = fsc.compilerLibrary != null ?
        fsc.compilerLibrary : Utils.getIdeaScalaCompilerLibraryName(project)
      def scalacSettingsComp = Utils.getOrCreateNode(provider.node, 'component', 'ScalacSettings')
      ['COMPILER_LIBRARY_NAME': compilerLibrary,
          'COMPILER_LIBRARY_LEVEL': 'Project',
          'SCALAC_BEFORE': String.valueOf(project.idea.project.scala.compiler.scalacBefore),
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

      //Enable Scala Type-aware highlighting (remove this chunk of code to disable)
      def typeAwareHLVal = String.valueOf(project.idea.project.scala.typeAwareHighlighting)
      def highlightingAdvisorComp = Utils.getOrCreateNode(provider.node, 'component', 'HighlightingAdvisor')
      ['SUGGEST_TYPE_AWARE_HIGHLIGHTING': 'false',
          'TYPE_AWARE_HIGHLIGHTING_ENABLED': typeAwareHLVal].each { name, value ->
        highlightingAdvisorComp.appendNode('option', ['name': name, 'value': value])
      }
    }
  }
}