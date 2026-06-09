package org.nrg.testing.tests

import org.nrg.testing.annotations.MutatesServerState
import org.nrg.testing.annotations.TestRequires
import org.nrg.testing.xnat.parallel.IsolationManager
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertFalse
import static org.testng.AssertJUnit.assertTrue

class IsolationClassificationTest {

    @Test
    void annotatedClassIsMutator() {
        assertTrue(IsolationManager.mutatesServerState(AnnotatedFixture))
    }

    @Test
    void annotationIsInherited() {
        assertTrue(IsolationManager.mutatesServerState(SubclassOfAnnotatedFixture))
    }

    @Test
    void classLevelClosedXnatIsMutator() {
        assertTrue(IsolationManager.mutatesServerState(ClosedXnatClassFixture))
    }

    @Test
    void methodLevelOpenXnatIsMutator() {
        assertTrue(IsolationManager.mutatesServerState(OpenXnatMethodFixture))
    }

    @Test
    void plainClassIsNotMutator() {
        assertFalse(IsolationManager.mutatesServerState(PlainFixture))
    }

    @Test
    void otherTestRequiresFlagsAreNotMutators() {
        assertFalse(IsolationManager.mutatesServerState(DicomScpFixture))
    }

    @MutatesServerState
    static class AnnotatedFixture {
    }

    static class SubclassOfAnnotatedFixture extends AnnotatedFixture {
    }

    @TestRequires(closedXnat = true)
    static class ClosedXnatClassFixture {
    }

    static class OpenXnatMethodFixture {
        @TestRequires(openXnat = true)
        void someTest() {
        }
    }

    static class PlainFixture {
    }

    @TestRequires(dicomScp = true)
    static class DicomScpFixture {
    }

}
