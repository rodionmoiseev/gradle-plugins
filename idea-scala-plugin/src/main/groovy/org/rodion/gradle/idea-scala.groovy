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
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

/**
 * A convenience plugin for single module projects
 * @author rodion
 */
class IdeaScalaPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    //all projects depend on idea plugin
    project.plugins.apply(IdeaPlugin.class)

    if (Utils.isRootProject(project)) {
      //Root project does not depend on scala plugin
      //If root project also enables scala-facet, 
      //scala plugin has to be applied manually.
      project.plugins.apply(IdeaScalaProjectPlugin.class)
    } else {
      println("DEBUG: applying scala ($project.name)")
      project.plugins.apply(ScalaPlugin.class)
    }

    //Scala-facet plugin will internally determine if it
    //is applied, depending on the idea.module.scala.enabled flag 
    project.plugins.apply(IdeaScalaFacetPlugin.class)
  }
}