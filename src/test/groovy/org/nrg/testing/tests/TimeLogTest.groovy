package org.nrg.testing.tests

import org.nrg.testing.FileIOUtils
import org.nrg.testing.TimeUtils
import org.nrg.testing.util.TimeLog
import org.testng.annotations.Test

import java.nio.file.Files
import java.nio.file.Path

import static org.testng.AssertJUnit.*

class TimeLogTest {

    private static final Path TEMP = Files.createTempDirectory('timelog_test')
    private static final String FAILED = 'Failed'
    private static final String PASSED = 'Passed'

    @Test
    void testSimplestTimelog() {
        final File outputLog = TEMP.resolve('timelog_no_history.csv').toFile()
        final TimeLog timeLog = new TimeLog()
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetB', 'TestFeatureA', FAILED, 10.03)
        timeLog.writeEntriesToFile([], timeLog.timeLogList, outputLog)
        compareToExpected('simplest_timelog_example.csv', outputLog)
    }

    @Test
    void testStatsWithoutPreexistingStats() {
        final File outputLog = TEMP.resolve('timelog_no_stats.csv').toFile()
        final TimeLog timeLog = new TimeLog()
        final List<List<String>> previousEntries = timeLog.readPreviousTimelog(injectDateToTimelog('timelog_no_stats_example_original.csv'))
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetB', 'TestFeatureA', FAILED, 12)
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetC', 'TestFeatureA', PASSED, 11.6)
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetD', 'TestFeatureA', PASSED, 10)
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetE', 'TestFeatureA', PASSED, 100)
        timeLog.writeEntriesToFile(previousEntries, timeLog.timeLogList, outputLog)
        compareToExpected('timelog_no_stats_example_modified.csv', outputLog)
    }

    @Test
    void testStatsWithPreexistingStats() {
        final File outputLog = TEMP.resolve('timelog_all_stats.csv').toFile()
        final TimeLog timeLog = new TimeLog()
        final List<List<String>> previousEntries = timeLog.readPreviousTimelog(injectDateToTimelog('timelog_existing_stats_example_original.csv'))
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetB', 'TestFeatureA', FAILED, 10)
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetC', 'TestFeatureA', PASSED, 12.2)
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetD', 'TestFeatureA', PASSED, 10)
        timeLog.timeLogList << new TimeLog.Entry(timeLog, 'testFeatureAFacetE', 'TestFeatureA', PASSED, 10)
        timeLog.writeEntriesToFile(previousEntries, timeLog.timeLogList, outputLog)
        compareToExpected('timelog_existing_stats_example_modified.csv', outputLog)
    }

    private void compareToExpected(String expectedFile, File actualFile) {
        assertEquals(injectDateToTimelog(expectedFile).text, actualFile.text)
    }

    private File injectDateToTimelog(String timelogFile) {
        final File properFile = TEMP.resolve(timelogFile).toFile()
        properFile.text = FileIOUtils.loadResource(timelogFile).text.replace('%DATE%', TimeUtils.getTimestamp('uuuu-MM-dd'))
        properFile
    }


}
