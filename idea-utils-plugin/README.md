IntelliJ IDEA Utilities Plugin
==============================

This plugin is a collection of miscellaneous IntelliJ IDEA project configurations, such as version control settings, copyright settings and so on. 

If you don't see feature here that you would love to have, feel free to post an enhancement request issue.

Installing
----------

Below is the minimum configuration required to setup the `idea-utils` plugin.

To apply the plugin in your project add this to your `build.gradle`.

```groovy
buildscript {
  repositories{
    mavenCentral()
  }
  dependencies {
    classpath group: 'com.github.rodionmoiseev.gradle.plugins', name: 'idea-utils', version: '0.2'
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
     * Project Settings / Spelling configuration options
     */
    spelling {
      //Name used for the "Accepted Words" (see "Accepted Words" tab under "Spelling" options)
      //embedded dictionary. Must match 'user.name' property in IntelliJ, otherwise dictionary
      //will not be recognised. Usually will work with the default value.
      // default: `System.getProperty("user.name")`
      // defined in: project
      userName = 'rodion'

      //Sources of accepted words (words to be recognised as correctly spelled).
      //Accepts a list of strings, a File, or a list of Files.
      //When files are specified, their contents are loaded and
      // exploded into a flat list, with duplicates removed.
      // default: []
      // defined in: project
      acceptedWords = []

      //A list of directories to be added to the "Dictionaries" tab. These directories will be
      //scanned for dictionary files.
      //Tip: you can use $PROJECT_DIR$ placeholder that evaluates to you project root directory.
      // default: []
      // defined in: project
      dictionaries = []

      //Charset to use when loading accepted words from files
      // default: 'UTF-8'
      charset = 'UTF-8'
    }
    
    /*
     * Copyright Plugin (built-in) Configuration
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

         //Name of the folder to place this run configuration under.
         //Folders will be created automatically as needed.
         // default: null (root folder)
         // defined in: project
         folderName = 'myFolder'
         
         //FQDN of the main class to execute
         // default: none (required, unless 'isDefault' is 'true')
         mainClass = 'com.example.main.Application'
         
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

         //Environment variables to pass to the executing process (Groovy map)
         // default: [:]
         // defined in: project
         env = [:]
         
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

         /*
          * Configures output logs for the Run Configuration (See "Logs" tab)
          *  defined in: project
          */
         logs {
           //Log configuration block.
           //Multiple blocks with unique names may be defined.
           log("myApp.log") {
             //Absolute path to the log file to watch.
             //Tip: you can use $PROJECT_DIR$ placeholder that evaluates to you
             //project root directory.
             // default: none (required)
             path = '$PROJECT_DIR$/myApp/logs/myApp.log'

             //Alias to use in the GUI
             // default: same as block name ("myApp.log" in this example)
             alias = 'myApp log'

             //If 'false' log watching will be disabled, and no tab for this log
             //will appear. Corresponds to the 'Is Active' column in the GUI.
             // default: true
             isActive = true

             //When 'true', IDEA will attempt to hide old log content from previous
             //application run.
             // default: true
             skipContent = true

             //When 'true', the 'path' parameter will be interpreted as a wildcard, where
             //all matching log files will be added to the watch.
             // default: false
             usePattern = false
           }
         }
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
