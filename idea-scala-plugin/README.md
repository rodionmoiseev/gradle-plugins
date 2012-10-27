IntelliJ IDEA Scala Facet Plugin
================================

This plugin works along with the [built-in Scala plugin][gradle-scala-plugin],
but in addition does a full set up of the Scala Facet, so that you are ready
to work with your Scala project right away!


Installing
----------

Below is the minimum configuration required to setup the `idea-scala` plugin
for a single-module setup.
If you have multi-project setup, please refer to [this section](#multi-project-setup)

To apply the plugin in your project add this to your `build.gradle`.

```groovy
buildscript {
  repositories{
    maven{
      url "https://raw.github.com/rodionmoiseev/maven-repo/master/repo/releases"
    }
  }
  dependencies {
    classpath group: 'org.rodion.gradle', name: 'idea-scala-plugin', version: '0.2'
  }
}

apply plugin: 'idea-scala'

dependencies{
  //Dependencies for the 'scala-library-2.9.1' module library
  scalaApi 'org.scala-lang:scala-library:2.9.1'

  //Dependencies for the 'scala-compiler-2.9.1' project library
  //as well as for gradle's scala compilation tasks
  scalaTools 'org.scala-lang:scala-library:2.9.1'
  scalaTools 'org.scala-lang:scala-compiler:2.9.1'
}
```

*Note:* You can add other Scala libraries, such as `scala-swing`, `scala-dbc`, etc. to the
`scalaApi` configuration. This will gurantee they are visible at compile time by both IntelliJ
compiler and Gradle scala compilation tasks.


Usage
-----

The plugin extends the existing `idea` gradle task of the [built-in IDEA plugin][gradle-idea-plugin].

To generate IntelliJ IDEA settings along with the Scala Facet, run:

```
$ gradle idea
```


Configuration
-------------

Scala Facet settings can be optionally customised. 
The configuration is designed to resemble the structure of Scala compiler settings GUI in the IntelliJ IDEA.

Plugin is configured using the `ideaScala{ ... }` closure somewhere in your `build.gradle`.
Below is the list of all possible configurations:

```groovy
ideaScala{
  //IntelliJ library name for Scala compiler
  // default: 'gradle-scala-compiler-<scalaVersion>'
  // defined in: project and module
  scalaCompilerLibName = 'my-scala-compiler-lib'
 
  //IntelliJ library name for Scala API libraries
  // default: 'gradle-scala-library-<scalaVersion>'
  // defined in: project and module
  scalaLibraryLibName = 'my-scala-library-lib'

  //Enable/disable IntelliJ type-aware syntax highlighting option
  // default: true
  // defined in: project
  typeAwareHighlighting = false

  compiler {
    //Set Scala compiler maximum heap size (MB)
    // default: 512
    // defined in: module
    maxHeapSize = 1024
    
    //Set Scala compiler VM options
    // default: '-Xss1m -server'
    // defined in: module
    vmParameters = '-Xss1m -server'

    //Set join compilation order.
    // false: javac, scalac
    // true: scalac, javac
    // default: true
    // defined in: project
    scalacBefore = false

    fsc {
      //Enable Fast Scala Compiler
      // default: false
      // defined in: module
      enable = true

      //Set FSC maximum heap size (MB)
      // default: 512
      // defined in: project
      maxHeapSize = 1024

      //Set FSC VM options
      // default: '-Xms128m -Xss1m -server'
      // defined in: project
      vmParameters = '-Xms128m -Xss1m -server'

      //Set FSC idle timeout (minutes). 0 for no timeout.
      // default: 0
      // defined in: project
      idleTimeout = 1

      //Set FSC server options
      // default: ''
      // defined in: project
      serverOptions = ''

      server {
        //Enable connecting to external FSC server
        // default: false
        // defined in: project
        enable = true

        //External server's host name
        // default: ''
        // defined in: project
        host = 'localhost'

        //External server's port number
        // default: -1 (unset)
        // defined in: project
        port = 32834

        //Path to shared directory
        // default: ''
        // defined in: project
        sharedDirectory = '/home/user/fsc/shared'
      }
    }
  }
}
```


Plugin Behaviour Details
------------------------

Internally, IntelliJ Scala Facet requires two project libraries:

* `scala-library-<scalaVersion>`: Scala API available at compile time of your project
* `scala-compiler-<scalaVersion>`: Scala compiler for IntelliJ internal use.

The plugin will consider all dependencies defined in the `scalaApi` configuration
to be part of the `scala-library-<scalaVersion>` module library, where `<scalaVersion>`
is worked out from the required `'org.scala-lang:scala-library:<version>'` dependency.

The `scala-compiler-<scalaVersion>` project module will include all dependencies of
the `scalaTools` configuration.

The `scalaApi` configuration is automatically declared by the plugin. The default
`compile` configuration is also set up to extends from `scalaApi` configuration.


Multi Project Setup
-------------------

*Since ver 0.2*

In IntelliJ you can choose which modules will have the Scala-facet applied.
This means that there are some module specific configurations as well as global project
configuration.

Global projet configuration has to be defined in `build.gradle` of the parent (or master) project,
and Scala-facet configuration has to be in `build.gradle` for each of the modules.

To make this possible, the plugin consists of two separate plugins (well, actually three):

* `idea-scala-project` - Should be applied in the root project to create global project configuration
* `idea-scala-facet`   - Should be applied in each Scala-enabled module to configure the Scala-facet
* `idea-scala`         - A convenience plugin which simply combines the two above. Should only be used for single module projects.

Please use the following `build.gradle` templates as a reference on how to set up your multi-project environment:

* Root project (master) configuration

```groovy
buildscript {
  repositories{
    maven{
      url "https://raw.github.com/rodionmoiseev/maven-repo/master/repo/releases"
    }
  }
  dependencies {
    classpath group: 'org.rodion.gradle', name: 'idea-scala-plugin', version: '0.2'
  }
}

// 'idea-scala-project' plugin includes the 'idea' plugin
apply plugin: 'idea-scala-project'

repositories{
  mavenCentral()
}


dependencies{
  // 'scalaApi' configuration is automatically added by the plugin
  // All libraries in this configuration will be part of the 'scala-library'
  // library in IntelliJ, a project-level library used by the Scala-facet.
  // The library will be referenced by all Scala-facet modules.
  scalaApi 'org.scala-lang:scala-library:2.9.1'

  // 'scalaTools' configuration is automatically added by the plugin
  // All libraries in this configuration will be part of the 'scala-compiler'
  // library in IntelliJ, a project-level library used by the Scala compiler.
  scalaTools 'org.scala-lang:scala-library:2.9.1'
  scalaTools 'org.scala-lang:scala-compiler:2.9.1'
}
```

* Scala-enabled module configuration

```groovy
// 'idea-scala-facet' plugin includes both, 'idea' and 'scala' plugins
apply plugin: 'idea-scala-facet'

repositories{
  mavenCentral()
}

dependencies{
  // 'scalaApi' configuration is automatically added by the plugin.
  // 'compile' configuration is made to extend the 'scalaApi' configuration.
  // All libraries in this configuration will be included during compilation.
  // These libraries will not appear as direct dependencies of the module, but
  // will be indirectly included in the 'scala-library' library of the Scala-facet.
  scalaApi 'org.scala-lang:scala-library:2.9.1'
}
```

The plugin configuration options described in the [Configuration](#configuration) section,
have the same format as in the single project setup. Note that the `defined in:` explanation
field for each of the options specifies where this option may be used:

* `project` - Means this option may only be specified in the root probject's `build.gradle` file
* `module`  - Means this option may only be specified in a Scala-facet module's `build.gradle` file

Note that if either `scalaCompilerLibName` or `scalaLibraryLibName` is changed, the same change has to
be applied in all modules's `build.gradle` and root project `build.gradle` files.

 [gradle-scala-plugin]: http://gradle.org/docs/current/userguide/scala_plugin.html "Gradle Scala Plugin"
 [gradle-idea-plugin]: http://gradle.org/docs/current/userguide/idea_plugin.html "Gradle IDEA Plugin"
