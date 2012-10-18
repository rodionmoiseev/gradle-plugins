package org.rodion.gradle

class ArtifactsExtension {
    final String name;
    File outputPath = null
    boolean buildOnMake = false
    Map<String, String> fileCopy = [:]

    ArtifactsExtension(String name) {
        this.name = name
    }
}
