package org.nrg.testing.listeners.interceptors.sorters;

import com.google.common.collect.Sets;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.apache.log4j.Logger;
import org.nrg.testing.annotations.HardDependency;
import org.nrg.testing.annotations.SoftClassDependency;
import org.nrg.testing.annotations.SoftDependency;
import org.nrg.testing.util.GraphUtils;
import org.nrg.testing.util.TestNgUtils;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.*;

public class DefaultMethodSorter implements IMethodInterceptor {

    public static boolean disabled = false; // Communication between IMethodInterceptors is not really allowed by TestNG, so have to use a static here to disabled default sorting if a more targeted sorter wants to take the job (pipeline tests)
    private static final Logger LOGGER = Logger.getLogger(DefaultMethodSorter.class);

    public static void disable() {
        disabled = true;
    }

    /**
     * Orders TestNG IMethodInstance methods by class, then by interdependency, and then by line number
     * @param methods all methods being run
     * @return ordered list of method instances
     */
    public List<IMethodInstance> orderMethods(List<IMethodInstance> methods) {
        // Line number method adapted from http://stackoverflow.com/a/28124123

        final List<IMethodInstance> allSortedTests = new ArrayList<>();
        final Map<Class, List<IMethodInstance>> classTestMap = sortTestInstancesByClass(methods);
        final List<Class> sortedClasses = calculateClassGraph(classTestMap.keySet());

        for (Class testClass : sortedClasses) { // Now sort each bundle of methods under a class
            final List<IMethodInstance> instances = classTestMap.get(testClass);
            final Map<IMethodInstance, Collection<IMethodInstance>> testMethodGraph = calculateMethodGraph(instances);
            boolean classHasDependencies = false;
            for (Collection<IMethodInstance> deps : testMethodGraph.values()) {
                if (!deps.isEmpty()) {
                    classHasDependencies = true;
                    break;
                }
            }

            if (classHasDependencies) {
                try {
                    allSortedTests.addAll(GraphUtils.topologicalSort(testMethodGraph));
                } catch (GraphUtils.CyclicGraphException cge) {
                    @SuppressWarnings("unchecked") final List<IMethodInstance> cyclicalDependencies = cge.getCycle();
                    final List<String> cyclicalMethods = new ArrayList<>();
                    for (IMethodInstance instance : cyclicalDependencies) {
                        cyclicalMethods.add(TestNgUtils.getTestName(instance.getMethod()));
                    }
                    throw new RuntimeException("Test suite could not execute because the following methods have a circular dependency cycle: " + cyclicalMethods);
                }
            } else {
                allSortedTests.addAll(orderByLineNumber(instances));
            }
        }
        return allSortedTests;
    }

    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (disabled) {
            LOGGER.debug("More targeted test sorter detected. Skipping ordering from " + this.getClass().getSimpleName());
            return methods;
        }
        LOGGER.debug("Method instance ordering intercepted in " + this.getClass().getSimpleName());
        return orderMethods(methods);
    }

    protected Map<IMethodInstance, Collection<IMethodInstance>> calculateMethodGraph(List<IMethodInstance> testMethods) {
        final Map<IMethodInstance, Collection<IMethodInstance>> methodGraph = new HashMap<>();
        final Map<String, IMethodInstance> methodNameMap = new HashMap<>();

        for (IMethodInstance method : testMethods) {
            methodNameMap.put(TestNgUtils.getTestName(method.getMethod()), method);
        }

        for (IMethodInstance methodInstance : testMethods) {
            final Collection<String> requirements = new ArrayList<>();
            final ITestNGMethod testMethod = methodInstance.getMethod();
            final SoftDependency softDependency = TestNgUtils.getAnnotation(testMethod, SoftDependency.class);
            final HardDependency hardDependency = TestNgUtils.getAnnotation(testMethod, HardDependency.class);
            if (softDependency != null) requirements.addAll(Arrays.asList(softDependency.value()));
            if (hardDependency != null) requirements.addAll(Arrays.asList(hardDependency.value()));

            if (!requirements.isEmpty()) {
                final List<IMethodInstance> requiredTests = new ArrayList<>();
                for (String requirement : requirements) {
                    final IMethodInstance requiredMethod = methodNameMap.get(requirement); // If required test is not being run, this would return null. Don't worry about that case here for sorting (will fail later if HardDependency required)
                    if (requiredMethod != null) requiredTests.add(requiredMethod);
                }
                methodGraph.put(methodInstance, requiredTests);
            } else {
                methodGraph.put(methodInstance, new ArrayList<IMethodInstance>());
            }
        }
        return methodGraph;
    }

    protected List<Class> calculateClassGraph(Collection<Class> classes) {
        final Map<Class, Collection<Class>> unsortedClasses = new HashMap<>();
        for (Class<?> testClass : classes) {
            final SoftClassDependency dependency = testClass.getAnnotation(SoftClassDependency.class);
            if (dependency == null) {
                unsortedClasses.put(testClass, new HashSet<>());
            } else {
                unsortedClasses.put(testClass, Sets.newHashSet(dependency.value()));
            }
        }
        try {
            return GraphUtils.topologicalSort(unsortedClasses);
        } catch (GraphUtils.CyclicGraphException cge) {
            throw new RuntimeException("Test classes had a cyclic dependency: " + cge.getCycle());
        }
    }

    protected List<IMethodInstance> orderByLineNumber(List<IMethodInstance> instances) {
        Comparator<IMethodInstance> lineNumberComparator = new Comparator<IMethodInstance>() {
            private int getLineNo(IMethodInstance mi) {
                int result = 0;

                String methodName = mi.getMethod().getConstructorOrMethod().getMethod().getName();
                String className  = mi.getMethod().getConstructorOrMethod().getDeclaringClass().getCanonicalName();
                ClassPool pool    = ClassPool.getDefault();

                try {
                    CtClass cc        = pool.get(className);
                    CtMethod ctMethod = cc.getDeclaredMethod(methodName);
                    result            = ctMethod.getMethodInfo().getLineNumber(0);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }

                return result;
            }

            public int compare(IMethodInstance m1, IMethodInstance m2) {
                return getLineNo(m1) - getLineNo(m2);
            }
        };

        IMethodInstance[] array = instances.toArray(new IMethodInstance[instances.size()]);
        Arrays.sort(array, lineNumberComparator);
        return Arrays.asList(array);
    }

    protected Map<Class, List<IMethodInstance>> sortTestInstancesByClass(List<IMethodInstance> methods) {
        final Map<Class, List<IMethodInstance>> classTestMap = new HashMap<>();

        for (IMethodInstance instance : methods) { // group all methods first by class
            final Class testClass = instance.getMethod().getRealClass();
            if (classTestMap.containsKey(testClass)) {
                classTestMap.get(testClass).add(instance);
            } else {
                classTestMap.put(testClass, new ArrayList<>(Collections.singletonList(instance)));
            }
        }

        return classTestMap;
    }
}