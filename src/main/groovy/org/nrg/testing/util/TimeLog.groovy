package org.nrg.testing.util

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import org.apache.commons.lang3.time.StopWatch
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.nrg.testing.FileIOUtils
import org.nrg.testing.TestNgUtils
import org.nrg.testing.TimeUtils
import org.nrg.testing.xnat.conf.Settings
import org.testng.ITestResult

import java.nio.file.Path
import java.nio.file.Paths

class TimeLog {

    private static final int MIN_NUM_FOR_STATS = 5
    private static final int NUM_POINTS_FOR_TREND = 5
    private static final double TREND_TOLERANCE = 0.05
    private static final String FAILED = 'Failed'
    private static final String PASSED = 'Passed'
    private static final String BLOCKED = 'Blocked'
    private static final Map<Integer, String> resultStringMap = [(ITestResult.FAILURE) : FAILED, (ITestResult.SUCCESS) : PASSED, (ITestResult.SKIP) : BLOCKED]
    private static Path BACKUP_LOCATION = Paths.get(Settings.TIMELOG_LOCATION, 'backups')
    private final List<Entry> timeLogList = []

    void addTimeLogEntry(StopWatch timer, ITestResult result) {
        final String test = TestNgUtils.getTestName(result)
        final String className = TestNgUtils.getTestClassName(result)
        timeLogList << new Entry(test, className, resultStringMap[result.status], timer.time / 1000)
    }

    void writeTimeLogs() {
        timeLogList.className.unique(false).each { className ->
            writeTimeLog(timeLogList.findAll { it.className == className })
        }
    }

    private void writeTimeLog(List<Entry> testsForOneClass) {
        FileIOUtils.mkdirs(BACKUP_LOCATION)
        final String className = testsForOneClass[0].className
        final File csvLog = getTimeLog(className)
        final List<List<String>> oldCsvContents = []
        if (csvLog.exists()) {
            final CSVReader reader = new CSVReader(new FileReader(csvLog))
            oldCsvContents.addAll(reader.readAll() as List<List<String>>)
            reader.close()
            writeCsvToFile(BACKUP_LOCATION.resolve("${className}${TimeUtils.getTimestamp()}.csv").toFile(), oldCsvContents)
            csvLog.delete()
        }

        final List<List<String>> fullContents = []
        final List<String> currentRequiredHeaders = headersFor(testsForOneClass)
        if (oldCsvContents.isEmpty()) {
            fullContents << currentRequiredHeaders
        } else {
            final List<String> headers = new ArrayList<>(oldCsvContents[0])
            currentRequiredHeaders.each { header ->
                if (!headers.contains(header)) headers << header
            }
            fullContents << headers
            removePreviousStats(oldCsvContents)
            fullContents.addAll(oldCsvContents.subList(1, oldCsvContents.size())) // keep non-header data
        }
        final int csvWidth = fullContents[0].size()

        final String[] dataRow = new String[csvWidth]
        final String[] medianRow = new String[csvWidth]
        final String[] meanRow = new String[csvWidth]
        final String[] trendRow = new String[csvWidth]
        dataRow[0] = TimeUtils.getTimestamp('yyyy-MM-dd')
        medianRow[0] = 'median execution time (seconds)'
        meanRow[0] = 'mean execution time (seconds)'
        trendRow[0] = 'trend of execution time'

        testsForOneClass.each { entry ->
            final int baseTestIndex = fullContents[0].indexOf(testTimeHeader(entry.testName))
            dataRow[baseTestIndex] = entry.runtime as String
            dataRow[baseTestIndex + 1] = entry.status
            final List<Double> allTimesForTest = oldCsvContents*.get(baseTestIndex).findResult { String time ->
                (time != null && time != '') ? Double.parseDouble(time) : null
            } as List<Double>
            allTimesForTest << entry.runtime
            if (allTimesForTest.size() >= MIN_NUM_FOR_STATS) {
                final DescriptiveStatistics stats = new DescriptiveStatistics()
                final SimpleRegression linearRegression = new SimpleRegression()
                allTimesForTest.each { stats.addValue(it) }
                allTimesForTest.takeRight(NUM_POINTS_FOR_TREND).eachWithIndex{ double time, int index ->
                    linearRegression.addData(index, time)
                }
                medianRow[baseTestIndex] = stats.getPercentile(50)
                meanRow[baseTestIndex] = stats.mean
                final double normalizedTrendSlope = linearRegression.slope / stats.mean
                if (normalizedTrendSlope > TREND_TOLERANCE) {
                    trendRow[baseTestIndex] = 'increasing'
                } else if (normalizedTrendSlope < -TREND_TOLERANCE) {
                    trendRow[baseTestIndex] = 'decreasing'
                } else {
                    trendRow[baseTestIndex] = 'stable'
                }
            }
        }
        fullContents << (dataRow as List<String>)
        fullContents << [] // visually distinguish stats
        fullContents << (medianRow as List<String>)
        fullContents << (meanRow as List<String>)
        fullContents << (trendRow as List<String>)

        writeCsvToFile(csvLog, fullContents)
    }

    private File getTimeLog(String className) {
        Paths.get(Settings.TIMELOG_LOCATION, "${className}_TimeData.csv").toFile()
    }

    private void writeCsvToFile(File outputFile, List<List<String>> lines) {
        final CSVWriter writer = new CSVWriter(new FileWriter(outputFile), ',' as char, CSVWriter.NO_QUOTE_CHARACTER)
        writer.writeAll(lines as List<String[]>)
        writer.close()
    }

    private List<String> headersFor(List<Entry> entries) {
        final List<String> headers = ['date']
        entries.each { entry ->
            headers << testTimeHeader(entry.testName)
            headers << "${entry.testName}_Result".toString()
        }
        headers
    }

    private String testTimeHeader(String testName) {
        "${testName}_Runtime_in_seconds"
    }

    private void removePreviousStats(List<List<String>> previousContents) {
        if (previousContents.size() > MIN_NUM_FOR_STATS) {
            final List<List<String>> finalThreeLines = previousContents.takeRight(3)
            if (!finalThreeLines.any { line -> // if we can recognize the last 3 lines all as stats...
                !['median', 'mean', 'trend'].any { stat ->
                    stat in line[0]
                }
            }) {
                previousContents.subList(previousContents.size() - 4, previousContents.size()).clear() // remove the three stats rows and the empty divider row
            }
        }
    }

    private class Entry {
        String testName
        String className
        String status
        double runtime

        Entry(String testName, String className, String status, double runtime) {
            this.testName = testName
            this.className = className
            this.status = status
            this.runtime = runtime
        }
    }

}
