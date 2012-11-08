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

/**
 * <p>Created: 6/7/12 5:00 PM</p>
 * @author rodion
 */
class IdeaUtilsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(IdeaUtilsBasePlugin)
        addArtifactsToProjectIpr(project)
        addRunConfigurationsToProjectIpr(project)
        addVcsSettingsToProjectIpr(project)
        addCopyrightSettingsToProjectIpr(project)
        addSpellingSettingsToProjectIpr(project)
        addMiscellaneousSettingsToProjectIpr(project)
    }

    def addSpellingSettingsToProjectIpr(Project project) {
        project.idea.project.ipr.withXml { XmlProvider provider ->
            SpellingExtension ext = project.idea.project.extensions.findByName(IdeaUtilsBasePlugin.SPELLING_EXTENSION_NAME)

            /*
             * Populate 'accepted words' dictionary if present
             */
            List<String> words = ext.loadAcceptedWords()
            if (!words.empty) {
                def wordsNode = provider.node.appendNode("component", [name: 'ProjectDictionaryState'])
                        .appendNode("dictionary", [name: ext.userName])
                        .appendNode("words")
                words.each { word ->
                    wordsNode.appendNode("w", [:], word)
                }
            }

            /*
             * Setup 'Dictionaries' tab
             */
            if (!ext.dictionaries.empty) {
                def dictOpts = [name: 'SpellCheckerSettings',
                        BundledDictionaries: '0',
                        Folders: String.valueOf(ext.dictionaries.size()),
                        Dictionaries: '0']
                ext.dictionaries.eachWithIndex {dir, i ->
                    dictOpts.put("Folder${i}", String.valueOf(dir))
                }
                provider.node.appendNode("component", dictOpts)
            }
        }
    }

    def addArtifactsToProjectIpr(Project project) {
        project.idea.project.ipr.withXml { XmlProvider provider ->
            def artifacts = project.idea.project.extensions.findByName(IdeaUtilsBasePlugin.ARTIFACTS_EXTENSION_NAME)
            if (!artifacts.empty) {
                def artifactsContainer = provider.node.appendNode("component", [name: 'ArtifactManager'])
                artifacts.each { ArtifactsExtension artifact ->
                    if (artifact.outputPath == null) {
                        throw new IdeaUtilsPluginException("Required 'outputPath' field has not been specified. Please set " +
                                "idea.project.${IdeaUtilsBasePlugin.ARTIFACTS_EXTENSION_NAME}.outputPath to artifact output directory.")
                    }
                    def artifactNode = artifactsContainer.appendNode('artifact', [name: artifact.name, 'build-on-make': String.valueOf(artifact.buildOnMake)])
                    artifactNode.appendNode('output-path', [:], artifact.outputPath.absolutePath)
                    def rootNode = artifactNode.appendNode('root', [id: 'root'])
                    if (!artifact.fileCopy.containsKey('path')) {
                        throw new IdeaUtilsPluginException("Artifact file-copy directive is a required 'path' option. Please make sure " +
                                "idea.project.${IdeaUtilsBasePlugin.ARTIFACTS_EXTENSION_NAME}.fileCopy has 'path' option set.")
                    }
                    def copyTargetNode = rootNode;
                    if (artifact.fileCopy.containsKey('dir')) {
                        artifact.fileCopy['dir'].split("/").each { dirName ->
                            copyTargetNode = copyTargetNode.appendNode('element', [id: 'directory', name: dirName])
                        }
                    }
                    copyTargetNode.appendNode('element', [id: 'file-copy', path: artifact.fileCopy['path']])
                }
            }
        }
    }

    def addMiscellaneousSettingsToProjectIpr(Project project) {
        project.idea.project.ipr.withXml { XmlProvider provider ->
            MiscExtension ext = project.idea.project.extensions.findByName(IdeaUtilsBasePlugin.MISC_EXTENSION_NAME)
            if (ext.dynamicClasspath) {
                def dynamicCp = provider.node.appendNode('component')
                dynamicCp.@name = 'PropertiesComponent'
                dynamicCp.appendNode('property', [name: 'dynamic.classpath', value: 'true'])
            }
        }
    }

    def addCopyrightSettingsToProjectIpr(Project project) {
        project.idea.project.ipr.withXml { XmlProvider provider ->
            CopyrightExtension ext = project.idea.project.extensions.findByName(IdeaUtilsBasePlugin.COPYRIGHT_EXTENSION_NAME)
            if (ext.licenseSpecified) {
                if (ext.name == null) {
                    throw new IdeaUtilsPluginException("Required 'name' field nas not been specified. Please set "
                            + "idea.project.${IdeaUtilsBasePlugin.COPYRIGHT_EXTENSION_NAME}.name field to any String value.")
                }
                if (ext.license == null) {
                    throw new IdeaUtilsPluginException("Required 'license' field nas not been specified. Please set "
                            + "idea.project.${IdeaUtilsBasePlugin.COPYRIGHT_EXTENSION_NAME}.license field to path "
                            + "to a file contaning license content.")
                }

                def comp = provider.node.component.find { it.@name == 'CopyrightManager' }
                comp.@default = ext.name
                def copyright = comp.appendNode('copyright')
                copyright.appendNode('option', [name: 'notice', value: ext.license.text])
                ['keyword': 'Copyright',
                        'allowReplaceKeyword': '',
                        'myName': ext.name,
                        'myLocal': 'true'].each { name, value ->
                    copyright.appendNode('option', ['name': name, 'value': value])
                }

            }
        }
    }

    def addVcsSettingsToProjectIpr(Project project) {
        project.idea.project.ipr.withXml { XmlProvider provider ->
            VcsExtension vcsSettings = project.idea.project.extensions.findByName(IdeaUtilsBasePlugin.VCS_EXTENSION_NAME)
            if (vcsSettings.vcs != null) {
                def mapping = provider.node.component.find { it.@name == 'VcsDirectoryMappings' }.mapping
                mapping.@vcs = vcsSettings.vcs
                mapping.@directory = vcsSettings.directory
            }
        }
    }

    def addRunConfigurationsToProjectIpr(Project project) {
        def runConfigs = project.idea.project.extensions.findByName(IdeaUtilsBasePlugin.RUN_CONFIG_EXTENSION_NAME)
        project.idea.project.ipr.withXml { XmlProvider provider ->
            //All run configurations are stored inside a dedicated
            //ProjectRunConfigurationManager component
            def runConfigComp = provider.node.appendNode('component')
            runConfigComp.@name = 'ProjectRunConfigurationManager'
            runConfigs.each { RunConfiguration config ->
                //Check required fields!
                if (config.mainClass == null) {
                    throw new IdeaUtilsPluginException("Required 'mainClass' field has not been specified. Please set "
                            + "idea.project.${IdeaUtilsBasePlugin.RUN_CONFIG_EXTENSION_NAME}.${config.configName}.mainClass "
                            + "to an executable class name, e.g. mainClass = 'com.example.Main'")
                }
                def configurationNode = runConfigComp.appendNode('configuration',
                        ["default": config.isDefault,
                                name: config.name,
                                type: config.getRunConfigType().internalType,
                                factoryName: config.getRunConfigType().name()])
                configureOptions(config, configurationNode)
            }
        }
    }

    def configureOptions(RunConfiguration config, Node configurationNode) {
        configurationNode.appendNode("extension", [name: "coverage",
                enabled: "false",
                merge: "false",
                runner: "idea"])
        ["MAIN_CLASS_NAME": config.mainClass,
                "VM_PARAMETERS": config.vmOptions,
                "WORKING_DIRECTORY": config.workingDirectory.absolutePath].each { name, value ->
            configurationNode.appendNode("option", ["name": name, "value": value])
        }
        configurationNode.appendNode("module", [name: config.useModuleClasspath])
        configurationNode.appendNode("envs")
        def methodNode = configurationNode.appendNode("method")
        if (!config.buildArtifacts.empty) {
            def optionNode = methodNode.appendNode("option", [name: "BuildArtifacts", enabled: "true"])
            config.buildArtifacts.each { artifactName ->
                optionNode.appendNode("artifact", [name: artifactName])
            }
        }
        switch (config.runConfigType) {
            case RunConfigType.Application:
                configureApplicationOptions(config, configurationNode)
                break;
            case RunConfigType.JUnit:
                configureJUnitOptions(config, configurationNode)
                break;
            default:
                throw new IdeaUtilsPluginException("Unexpected run configuration type: " + config.runConfigType)
        }
    }

    def configureApplicationOptions(RunConfiguration config, Node configurationNode) {
        ["PROGRAM_PARAMETERS": config.programArguments].each { name, value ->
            configurationNode.appendNode("option", ["name": name, "value": value])
        }
    }

    def configureJUnitOptions(RunConfiguration config, Node configurationNode) {
        ["TEST_OBJECT": "class"].each { name, value ->
            configurationNode.appendNode("option", ["name": name, "value": value])
        }
        configurationNode.appendNode("option").appendNode("value", [defaultName: "moduleWithDependencies"])
        configurationNode.appendNode("patterns")
    }
}