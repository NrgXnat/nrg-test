package org.nrg.testing.xnat.components

import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.xnat.pogo.Project

class ComponentizedTest {

    private final List<TestComponent> testSteps

    ComponentizedTest(TestComponent... steps) {
        testSteps = Arrays.asList(steps)
    }

    Project createTestProject(BaseXnatRestTest xnatRestTest) {
        final Project project = xnatRestTest.registerTempProject()
        xnatRestTest.mainInterface().createProject(project)
        project
    }

    void run(BaseXnatRestTest xnatRestTest) {
        final Project project = createTestProject(xnatRestTest)
        testSteps.each { component ->
            component.perform(xnatRestTest, project)
        }
    }

}
