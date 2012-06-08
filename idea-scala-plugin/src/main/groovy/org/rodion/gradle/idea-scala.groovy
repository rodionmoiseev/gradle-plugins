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

/**
 * A convenience plugin for single module projects
 * @author rodion
 */
class IdeaScalaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        //Scala plugin needs to be initialised before idea-scala-project plugin
        //otherwise will fail when trying to register 'scalaTools' configuration
        project.plugins.apply(ScalaPlugin.class)
        project.plugins.apply(IdeaScalaFacetPlugin.class)
        project.plugins.apply(IdeaScalaProjectPlugin.class)
    }
}