package com.github.rodionmoiseev.gradle.plugins

import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * @author rodion
 */
class CustomElementMergeTest {
    @Test
    public void byDefaultNoCustomElementsGetMerged() {
        def runConf = new RunConfiguration()

        //Original Node
        Node confNode = new NodeBuilder().component(a: "a-val") {
            envs()
            hello(v: "my-hello")
        }

        new IdeaUtilsPlugin().addCustomElements(confNode, runConf)
        assertEquals("a-val", confNode.@a)
        assertEquals(["my-hello"], confNode.hello.@v)
    }

    @Test
    public void specifiedCustomElementsAreAddedExistingOnesPreserved() {
        def runConf = new RunConfiguration()
        runConf.customElements = {
            hello(attr1: true, attr2: "hello") {
                world('#text': "Hello, World!")
            }
            newstuff(n: "new-stuff")
        }

        //Original Node
        Node confNode = new NodeBuilder().component(a: "a-val") {
            envs()
            hello(v: "my-hello", attr2: "hello2")
        }

        new IdeaUtilsPlugin().addCustomElements(confNode, runConf)
        assertEquals("a-val", confNode.@a)
        //Element with the same name are appended
        assertEquals(2, confNode.findAll { it.name() == "hello" }.size())
        assertEquals(1, confNode.findAll {
            it.name() == "hello" && it.@v == "my-hello"
        }.size())
        assertEquals(1, confNode.findAll {
            it.name() == "hello" && it.@attr1
        }.size())
        assertEquals(["hello2", "hello"], confNode.hello.@attr2)
        assertEquals(["new-stuff"], confNode.newstuff.@n)
    }
}
