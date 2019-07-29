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
    final List<Entry> timeLogList = []

    void addTimeLogEntry(StopWatch timer, ITestResult result) {
        final String test = TestNgUtils.getTestName(result)
        final String className = TestNgUtils.getTestClassName(result)
        timeLogList << new Entry(test, className, resultStringMap[result.status], timer.time / 1000)
    }

    void writeTimeLogs() {
        final Path backupLocation = Paths.get(Settings.TIMELOG_LOCATION, 'backups')
        FileIOUtils.mkdirs(backupLocation)
        timeLogList.className.unique(false).each { className ->
            final List<Entry> testsForOneClass = timeLogList.findAll { it.className == className }
            final File csvLog = getTimeLog(className)
            final List<List<String>> oldCsvContents = readPreviousTimelog(csvLog)
            if (csvLog.exists()) {
                writeCsvToFile(backupLocation.resolve("${className}${TimeUtils.getTimestamp()}.csv").toFile(), oldCsvContents)
                csvLog.delete()
            }
            writeEntriesToFile(oldCsvContents, testsForOneClass, csvLog)
        }
    }

    List<List<String>> readPreviousTimelog(File csvLog) {
        final List<List<String>> oldCsvContents = []
        if (csvLog.exists()) {
            final CSVReader reader = new CSVReader(new FileReader(csvLog))
            reader.readAll().each { row ->
                oldCsvContents << (row as List<String>)
            }
            reader.close()
        }
        oldCsvContents
    }

    List<List<String>> readEntriesToLists(List<List<String>> previousTimelog, List<Entry> entries) {
        final List<List<String>> fullContents = []
        final List<String> currentRequiredHeaders = headersFor(entries)
        if (previousTimelog.isEmpty()) {
            fullContents << currentRequiredHeaders
        } else {
            final List<String> headers = new ArrayList<>(previousTimelog[0])
            currentRequiredHeaders.each { header ->
                if (!headers.contains(header)) headers << header
            }
            fullContents << headers
            removePreviousStats(previousTimelog)
            fullContents.addAll(previousTimelog.drop(1)) // keep non-header data
        }
        final int csvWidth = fullContents[0].size()

        final String[] dataRow = new String[csvWidth]
        final String[] medianRow = new String[csvWidth]
        final String[] meanRow = new String[csvWidth]
        final String[] trendRow = new String[csvWidth]
        dataRow[0] = TimeUtils.getTimestamp('uuuu-MM-dd')
        medianRow[0] = 'median execution time (seconds)'
        meanRow[0] = 'mean execution time (seconds)'
        trendRow[0] = 'trend of execution time'

        entries.each { entry ->
            final int baseTestIndex = fullContents[0].indexOf(testTimeHeader(entry.testName))
            dataRow[baseTestIndex] = entry.runtime as String
            dataRow[baseTestIndex + 1] = entry.status
            final List<Double> allTimesForTest = previousTimelog.drop(1).findResults { row ->
                if (baseTestIndex < row.size()) {
                    final String time = row[baseTestIndex]
                    (time != null && time != '') ? Double.parseDouble(time) : null
                } else {
                    null
                }
            } ?: []
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
        if (medianRow.drop(1).any { it != null }) {
            fullContents << (1 .. medianRow.size()).collect { '' } // visually distinguish stats with an empty row
            fullContents << (medianRow as List<String>)
            fullContents << (meanRow as List<String>)
            fullContents << (trendRow as List<String>)
        }
        fullContents
    }

    void writeCsvToFile(File outputFile, List<List<String>> lines) {
        final CSVWriter writer = new CSVWriter(new FileWriter(outputFile), ',' as char, CSVWriter.NO_QUOTE_CHARACTER)
        writer.writeAll(lines.collect { it as String[] })
        writer.close()
    }

    void writeEntriesToFile(List<List<String>> previousTimelog, List<Entry> entries, File outputFile) {
        writeCsvToFile(outputFile, readEntriesToLists(previousTimelog, entries))
    }

    private File getTimeLog(String className) {
        Paths.get(Settings.TIMELOG_LOCATION, "${className}_TimeData.csv").toFile()
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
                    line[0].contains(stat)
                }
            }) {
                previousContents.subList(previousContents.size() - 4, previousContents.size()).clear() // remove the three stats rows and the empty divider row
            }
        }
    }

    class Entry {
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
