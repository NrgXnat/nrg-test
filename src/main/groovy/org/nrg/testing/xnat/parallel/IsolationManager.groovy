package org.nrg.testing.xnat.parallel

import org.nrg.testing.annotations.MutatesServerState
import org.nrg.testing.annotations.TestRequires
import org.nrg.testing.xnat.conf.Settings

import java.lang.reflect.Method
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Coordinates concurrent test classes sharing one XNAT when parallel class execution is enabled
 * (xnat.parallel.classes > 1). Classes that mutate server-global state (see {@link MutatesServerState})
 * take the write side of a suite-wide read/write lock so they never overlap with any other class;
 * all other classes take the read side and may overlap with each other.
 *
 * Also tracks which test class owns the current thread, so {@link ServerStateGuard} can attribute
 * REST calls to a class.
 */
class IsolationManager {

    // Fair, so a queued server-state mutator is not starved by a steady stream of read-locking classes.
    private static final ReentrantReadWriteLock SUITE_LOCK = new ReentrantReadWriteLock(true)
    // Read/write locks must be released on the thread that acquired them. TestNG's parallel="classes"
    // mode runs all configuration and test methods of a class on a single worker thread, which is what
    // makes the ThreadLocal bookkeeping here valid.
    private static final ThreadLocal<Lock> HELD_LOCK = new ThreadLocal<>()
    private static final InheritableThreadLocal<Class> CURRENT_TEST_CLASS = new InheritableThreadLocal<>()

    /**
     * A class mutates server state if it (or a superclass) is annotated with {@link MutatesServerState},
     * or if the framework toggles site config on its behalf via @TestRequires(openXnat/closedXnat).
     */
    static boolean mutatesServerState(Class testClass) {
        if (testClass.getAnnotation(MutatesServerState) != null) {
            return true
        }
        if (requiresSiteToggle(testClass.getAnnotation(TestRequires))) {
            return true
        }
        testClass.methods.any { Method method ->
            requiresSiteToggle(method.getAnnotation(TestRequires))
        }
    }

    static void enterClass(Class testClass) {
        noteCurrentClass(testClass)
        if (!Settings.PARALLEL_ENABLED || HELD_LOCK.get() != null) {
            return
        }
        final Lock lock = mutatesServerState(testClass) ? SUITE_LOCK.writeLock() : SUITE_LOCK.readLock()
        lock.lock()
        HELD_LOCK.set(lock)
    }

    static void exitClass() {
        final Lock lock = HELD_LOCK.get()
        if (lock != null) {
            HELD_LOCK.remove()
            lock.unlock()
        }
    }

    static void noteCurrentClass(Class testClass) {
        CURRENT_TEST_CLASS.set(testClass)
    }

    static Class currentTestClass() {
        CURRENT_TEST_CLASS.get()
    }

    private static boolean requiresSiteToggle(TestRequires testRequires) {
        testRequires != null && (testRequires.openXnat() || testRequires.closedXnat())
    }

}
