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
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency

class Utils {
  public static final SCALA_LIBRARY_NAME = 'scala-library'
  public static final SCALA_COMPILER_NAME = 'scala-compiler'

  static boolean isRootProject(Project project) {
    return project.path == ':'
  }

  static Node getOrCreateNode(Node parent, String type) {
    for (Node child : parent.children()) {
      if (child.name().toString() == type) {
        return child
      }
    }
    return parent.appendNode(type)
  }

  static Node getOrCreateNode(Node parent, String type, String name) {
    return getOrCreateNode(parent, type, name, [:])
  }

  static Node getOrCreateNode(Node parent, String type, String name, Map<String, String> attrs) {
    parent.get(name)
    Node node = parent.children().find { it.@name == name }
    if (null == node) {
      node = parent.appendNode(type)
      node.@name = name
    }
    node.attributes().putAll(attrs)
    return node
  }

  static String getIdeaScalaCompilerLibraryName(Project project) {
    def scalaDep = findScalaLibDep(project)
    if (null == scalaDep) {
      throw new IdeaScalaPluginException("Could not determine scala-compiler library name: " +
          "No (sub)projects with '$SCALA_LIBRARY_NAME' dependency were found.")
    }
    return SCALA_COMPILER_NAME + "-" + scalaDep.getVersion()
  }

  static Dependency findScalaLibDep(Project project) {
    return findFirstDep(project, SCALA_LIBRARY_NAME)
  }

  private static Dependency findFirstDep(Project project, String depName) {
    for (Project prj : project.allprojects) {
      def conf = findConfContainingDep(prj, depName)
      if (null != conf) {
        return conf.dependencies.find { it.name == depName }
      }
    }
    return null
  }

  private static Configuration findConfContainingDep(Project project, String depName) {
    def testCompileConf = project.configurations.getByName('testCompile')
    if (confHasDep(testCompileConf, depName)) {
      return testCompileConf
    }
    def compileConf = project.configurations.getByName('compile')
    if (confHasDep(compileConf, depName)) {
      return compileConf
    }
    return null
  }

  private static boolean confHasDep(Configuration conf, String depName) {
    return conf?.dependencies.find { it.name == depName } != null
  }
}