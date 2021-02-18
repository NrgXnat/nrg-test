package org.nrg.testing.listeners.interceptors.sorters

import groovy.util.logging.Log4j
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.HardDependency
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.annotations.SoftDependency
import org.nrg.testing.annotations.SortLast
import org.nrg.xnat.util.GraphUtils
import org.testng.IMethodInstance
import org.testng.ITestNGMethod

import java.lang.annotation.Annotation

@Log4j
class DefaultMethodSortComparator implements Comparator<IMethodInstance> {

    private final List<Class> topologicallySortedClasses = []
    private final Map<Class, List<IMethodInstance>> topologicallySortedMethods = [:]
    private final List<Class> classesWithMethodDependencies = []
    private final Comparator<IMethodInstance> lineNumberComparator = new LineNumberComparator()

    DefaultMethodSortComparator(List<IMethodInstance> methods) {
        topologicallySortClasses(methods)
        topologicallySortMethods(methods)
    }

    @Override
    // Orders TestNG IMethodInstance methods by class, then by interdependency, and then by line number
    int compare(IMethodInstance m1, IMethodInstance m2) {
        final Class m1Class = TestNgUtils.getTestClass(m1)
        final Class m2Class = TestNgUtils.getTestClass(m2)
        final int indexDiff = topologicallySortedClasses.indexOf(m1Class) - topologicallySortedClasses.indexOf(m2Class)
        if (indexDiff != 0) {
            indexDiff
        } else if (m1Class in classesWithMethodDependencies) {
            // Now we know that m1 and m2 belong to the same test class
            final List<IMethodInstance> topologicallySortedList = topologicallySortedMethods[m1Class]
            topologicallySortedList.indexOf(m1) - topologicallySortedList.indexOf(m2)
        } else {
            lineNumberComparator.compare(m1, m2)
        }
    }

    private void topologicallySortClasses(List<IMethodInstance> methods) {
        final List<Class> allClasses = methods.collect { method ->
            TestNgUtils.getTestClass(method)
        }.unique()
        final Map<Class, Collection<Class>> unsortedClasses = allClasses.collectEntries { testClass ->
            final SoftClassDependency dependency = testClass.getAnnotation(SoftClassDependency) as SoftClassDependency
            final List<Class> classes = (dependency == null) ? [] : dependency.value() as List<Class>
            [(testClass) : classes]
        }
        try {
            topologicallySortedClasses.addAll(GraphUtils.topologicalSort(unsortedClasses))
        } catch (GraphUtils.CyclicGraphException cge) {
            throw new RuntimeException("Test classes had a cyclic dependency: ${cge.cycle}")
        }
    }

    private void topologicallySortMethods(List<IMethodInstance> methods) {
        topologicallySortedMethods.putAll(topologicallySortedClasses.collectEntries { testClass ->
            final Collection<IMethodInstance> instancesForClass = methods.findAll { method ->
                TestNgUtils.getTestClass(method) == testClass
            }
            if (instancesForClass.any { instance ->
                [SoftDependency, HardDependency, SortLast].any { annotation ->
                    TestNgUtils.getAnnotation(instance.method, annotation as Class<? extends Annotation>) != null
                }
            }) {
                classesWithMethodDependencies << testClass
            }
            [(testClass): topologicallySortMethodSubgraph(instancesForClass)]
        })
    }

    private List<IMethodInstance> topologicallySortMethodSubgraph(Collection<IMethodInstance> methods) {
        final List<IMethodInstance> methodsSortedLast, otherMethods
        (methodsSortedLast, otherMethods) = methods.split {methodInstance ->
            TestNgUtils.getAnnotation(methodInstance.method, SortLast) != null
        }
        final Map<IMethodInstance, Collection<IMethodInstance>> dependencyMap = methods.collectEntries { methodInstance ->
            final ITestNGMethod testMethod = methodInstance.method
            final List<String> dependencies = []
            final List<IMethodInstance> dependencyMethods = []
            final SoftDependency softDependencies = TestNgUtils.getAnnotation(testMethod, SoftDependency)
            final HardDependency hardDependencies = TestNgUtils.getAnnotation(testMethod, HardDependency)
            if (softDependencies != null) {
                dependencies.addAll(softDependencies.value())
            }
            if (hardDependencies != null) {
                dependencies.addAll(hardDependencies.value())
            }
            if (methodsSortedLast.contains(methodInstance)) {
                dependencyMethods.addAll(otherMethods)
            }
            if (!dependencies.isEmpty()) {
                dependencies.each { dependency ->
                    final IMethodInstance dependencyMethod = methods.find { method ->
                        TestNgUtils.getTestName(method) == dependency
                    }
                    if (dependencyMethod == null) {
                        log.warn("Test ${TestNgUtils.getTestName(methodInstance)} has a dependency on a test method called '${dependency}'. However, ${this.class.simpleName} can't find such a test, meaning it was likely already removed from unsatisfied version and/or plugin dependencies, or the test name was spelled incorrectly.")
                    } else {
                        dependencyMethods << dependencyMethod
                    }
                }
            }
            [(methodInstance): dependencyMethods]
        }

        try {
            GraphUtils.topologicalSort(dependencyMap)
        } catch (GraphUtils.CyclicGraphException cge) {
            throw new RuntimeException("Test methods had a cyclic dependency: ${cge.cycle}")
        }
    }

    /**
     * adapted from http://stackoverflow.com/a/28124123
     */
    private class LineNumberComparator implements Comparator<IMethodInstance> {

        @Override
        int compare(IMethodInstance m1, IMethodInstance m2) {
            getLineNumber(m1) - getLineNumber(m2)
        }

        private int getLineNumber(IMethodInstance mi) {
            final String methodName = TestNgUtils.getTestName(mi)
            final String className = mi.method.constructorOrMethod.declaringClass.canonicalName
            final ClassPool pool = ClassPool.getDefault()
            final CtClass cc = pool.get(className)
            final CtMethod ctMethod = cc.getDeclaredMethod(methodName)
            ctMethod.methodInfo.getLineNumber(0)
        }
    }

}