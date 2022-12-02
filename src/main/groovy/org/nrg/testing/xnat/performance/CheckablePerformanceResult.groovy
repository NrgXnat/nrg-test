package org.nrg.testing.xnat.performance

class CheckablePerformanceResult {

    public static final SUCCESS = new CheckablePerformanceResult(passed: true)

    CheckablePerformanceResult() {}

    CheckablePerformanceResult(String problem) {
        setPassed(false)
        problems = [problem]
    }

    boolean passed = false
    List<String> problems

    static CheckablePerformanceResult merge(List<CheckablePerformanceResult> intermediateResults) {
        final List<String> problems = intermediateResults.findResults { result ->
            result?.problems
        }.flatten() as List<String>
        new CheckablePerformanceResult(
                passed: problems.size() == 0,
                problems: problems
        )
    }

}
