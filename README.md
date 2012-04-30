Gradle Plugins
==============

IntelliJ IDEA Scala Facet Plugin
--------------------------------

This plugin works along with the [built-in Scala plugin][gradle-scala-plugin],
but in addition does a full set up of the Scala Facet, so that you are ready
to work with your Scala project right away!


## Installing

Below is the minimum configuration required to setup the `idea-scala` plugin.

To apply the plugin in your project add this to your `build.gradle`.

```groovy
buildscript {
  repositories{
    maven{
      url "https://raw.github.com/rodionmoiseev/maven-repo/master/repo/releases"
    }
  }
  dependencies {
    classpath group: 'org.rodion.gradle', name: 'idea-scala-plugin', version: '0.1'
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


## Usage

The plugin extends the existing `idea` gradle task of the [built-in IDEA plugin][gradle-idea-plugin].

To generate IntelliJ IDEA settings along with the Scala Facet, run:

```
$ gradle idea
```


## Configuration

Scala Facet settings can be optionally customised. 
The configuration is designed to resemble the structure of Scala compiler settings GUI in the IntelliJ IDEA.

Plugin is configured using the `ideaScala{ ... }` closure somewhere in your `build.gradle`.
Below is the list of all possible configurations:

```groovy
ideaScala{
  //IntelliJ library name for Scala compiler
  // default: 'gradle-scala-compiler-<scalaVersion>'
  scalaCompilerLibName = 'my-scala-compiler-lib'
 
  //IntelliJ library name for Scala API libraries
  // default: 'gradle-scala-library-<scalaVersion>'
  scalaLibraryLibName = 'my-scala-library-lib'

  //Enable/disable IntelliJ type-aware syntax highlighting option
  // default: true
  typeAwareHighlighting = false

  compiler {
    //Set Scala compiler maximum heap size (MB)
  	// default: 512
    maxHeapSize = 1024
    
    //Set Scala compiler VM options
    // default: '-Xss1m -server'
    vmParameters = '-Xss1m -server'

    //Set join compilation order.
    // false: javac, scalac
    // true: scalac, javac
    // default: true
    scalacBefore = false

   	fsc {
   		//Enable Fast Scala Compiler
        // default: false
        enable = true

        //Set FSC maximum heap size (MB)
        // default: 512
        maxHeapSize = 1024

        //Set FSC VM options
        // default: '-Xms128m -Xss1m -server'
        vmParameters = '-Xms128m -Xss1m -server'

        //Set FSC idle timeout (minutes). 0 for no timeout.
        // default: 0
        idleTimeout = 1

        //Set FSC server options
        // default: ''
        serverOptions = ''

        server {
            //Enable connecting to external FSC server
            // default: false
            enable = true

            //External server's host name
            // default: ''
            host = 'localhost'

            //External server's port number
            // default: -1 (unset)
            port = 32834

            //Path to shared directory
            // default: ''
            sharedDirectory = '/home/user/fsc/shared'
        }
    }
}
```


## Plugin Behaviour Details

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

 [gradle-scala-plugin]: http://gradle.org/docs/current/userguide/scala_plugin.html "Gradle Scala Plugin"
 [gradle-idea-plugin]: http://gradle.org/docs/current/userguide/idea_plugin.html "Gradle IDEA Plugin"