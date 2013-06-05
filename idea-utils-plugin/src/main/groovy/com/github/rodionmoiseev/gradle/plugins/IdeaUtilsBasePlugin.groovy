package com.github.rodionmoiseev.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin

class IdeaUtilsBasePlugin implements Plugin<Project> {
    static final String ARTIFACTS_EXTENSION_NAME = "artifacts"
    static final String RUN_CONFIG_EXTENSION_NAME = "runConfigurations"
    static final String VCS_EXTENSION_NAME = "vcs"
    static final String COPYRIGHT_EXTENSION_NAME = "copyright"
    static final String SPELLING_EXTENSION_NAME = "spelling"
    static final String MISC_EXTENSION_NAME = "misc"

    @Override
    void apply(Project project) {
        project.plugins.apply(IdeaPlugin.class)
        def artifacts = project.container(ArtifactsExtension)
        project.idea.project.extensions.add(ARTIFACTS_EXTENSION_NAME, artifacts)
        project.idea.project.extensions.create(VCS_EXTENSION_NAME, VcsExtension)
        project.idea.project.extensions.create(COPYRIGHT_EXTENSION_NAME, CopyrightExtension)
        project.idea.project.extensions.create(SPELLING_EXTENSION_NAME, SpellingExtension)
        project.idea.project.extensions.create(MISC_EXTENSION_NAME, MiscExtension)
        def runConfigs = project.container(RunConfiguration)
        runConfigs.all { rootProject = project }
        project.idea.project.extensions.add(RUN_CONFIG_EXTENSION_NAME, runConfigs)
    }
}

enum RunConfigType {
    Application("Application"),
    JUnit("Junit"),
    Specs("SpecsRunConfiguration");

    private final String internalType;

    def RunConfigType(String internalType) {
        this.internalType = internalType;
    }

    public String getInternalType() {
        return internalType
    }
}