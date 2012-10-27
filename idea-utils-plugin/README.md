IntelliJ IDEA Utilities Plugin
==============================

**Note:** Current latest version is `0.1-SNAPSHOT`. `0.1` will be released shortly.

This plugin is a collection of miscellaneous IntelliJ IDEA project configurations, such as version control settings, copyright settings and so on. 

If you don't see feature here that you would love to have, feel free to post an enhancement request issue.

Installing
----------

Below is the minimum configuration required to setup the `idea-utils` plugin.

To apply the plugin in your project add this to your `build.gradle`.

```groovy
buildscript {
  repositories{
    maven{
      url "https://raw.github.com/rodionmoiseev/maven-repo/master/repo/snapshots"
    }
  }
  dependencies {
    classpath group: 'org.rodion.gradle', name: 'idea-utils-plugin', version: '0.1-SNAPSHOT'
  }
}

apply plugin: 'idea-utils'

```

Usage
-----

The plugin extends the existing `idea` gradle task of the [built-in IDEA plugin][gradle-idea-plugin].

After you have finished configuring your IntelliJ IDEA setup (see below for Configuration options), simply run command below to generate IntelliJ settings files.

```
$ gradle idea
```

Configuration
-------------

The configuration is designed to resemble the structure of respective feature settings GUI in the IntelliJ IDEA itself.

Here is a full list of all possible IntelliJ configurations
that maybe generated using this plugin.

```groovy
idea {
  project {
    
    /*
     * Version Control Configuration
     * Currently only one setting per project is supported.
     */
    vcs {
      //Specifies version control type
      // default: none (required)
      // possible values: Git, svn, ClearCase, hg4idea
      // defined in: project
      vcs = 'Git'
      
      //Specifies version control root directory. Not necessary for some VCS types.
      // default: ''
      // Typically set to '$PROJECT_DIR$'. However if your parent build.gradle is
      // in the special 'master' folder set directory to '$PROJECT_DIR$/..'
      directory = '$PROJECT_DIR$'
    }
    
    /*
     * Copyright Plugin (built-in) Confgiuration
     */
    copyright {
      //License configuration name
      // default: none (required)
      // defined in: project
      name = 'Apache 2.0'
      
      //File containing license content. 
      // default: none (required)
      // defined in: project
      license = file('apache_license.txt')
    }
    
    /*
     * Shared Run-configuration Configuration
     */
    runConfigurations {
       //Run-configuration configure block.
       //Block name will be used as the default configuration name.
       //Multiple blocks with unique names may be defined.
       runMyApp {
         //Type of run-configuration to create
         // default: 'Application'
         // possible values: Application, Junit, SpecsRunConfiguration
         // defined in: project
         type = 'Application'
         
         //Name of run-configuration as it will appear in the IDE
         // default: 'runMyApp' (defaults to configuration block name)
         // defined in: project
         name = 'Run my application'
         
         //FQDN of the main class to execute
         // default: none (required)
         mainClass = 'com.examle.main.Application'
         
         //Options to pass to Java VM
         // default: ''
         // defined in: project
         vmOptions = ''
         
         //Arguments to be passed to the user application
         // default: ''
         // defined in: project
         programArguments = ''
         
         //Module the classpath of which will be used to run this application
         // default: Empty string for single project setup. Required for multi-project setup.
         // defined in: project
         module = project(':sub-project')
         
         //Current working directory of script to be executed
         // default: module.projectDir (Directory of the module for multi-project setup. 
         //  Parent project directory for single-project setup)
         // defined in: project
         workingDirectory = project(':sub-project').projectDir
         
         //When set to 'true', this configuration will appear under 'Defaults' tree in the
         // 'Run/Debug Configurations' screen, and will act as default settings for other configurations
         // default: false
         // defined in: project
         isDefault = false
         
         //List of project artifacts to build before launch. Artifact names must 
         //be the same as those defined under:
         // 'Project Structure' -> 'Project Settings' -> Artifacts
         //Tip: See 'artifacts' configuration closure below to auto-configure project artifacts
         // default: []
         // defined in: project
         buildArtifacts = ['myArtifact']
       }
    }
    
    /*
     * Other miscellaneous options
     */
    misc {
      //Set to 'true' to enable dynamic classpath feature on project level
      //See: http://stackoverflow.com/questions/4853540/what-does-the-dynamic-classpath-flag-do-intellij-project-settings
      // default: false
      // defined in: project
      dynamicClasspath = false
    }
    
    /*
     * Project Artifact Configuration (API is still Beta)
     */
    artifacts {
      //Artifact configuration block.
      //Block name will be used as the artifact name.
      //Multiple blocks with unique names may be defined.
      myArtifact {
        //Directory where artifact content will be copied to
        // default: none (required)
        // defined in: project
        outputPath = file("artifacts")
        
        //If set to 'true', this artifact will be automatically built
        //every time you run 'Make project'
        // default: false
        // defined in: project
        buildOnMake = false
        
        //Specify a file to copy and sub-directory relative to outputPath
        //to copy the file to
        // default: 
        //  'path' element: none (required)
        //  'dir' element: '' (will copy file directly under outputPath directory)
        // defined in: project
        fileCopy = [path: 'config.xml', dir: 'data/configs']
      }
    }
  }
}
```

 [gradle-idea-plugin]: http://gradle.org/docs/current/userguide/idea_plugin.html "Gradle IDEA Plugin"
