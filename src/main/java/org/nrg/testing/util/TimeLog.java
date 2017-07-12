package org.nrg.testing.util;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;
import org.nrg.testing.xnat.conf.Settings;
import org.testng.ITestResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class TimeLog {

    private static List<List<String>> timeLogList = new ArrayList<>();
    private static final String FAILED = "Failed";
    private static final String PASSED = "Passed";
    private static final String BLOCKED = "Blocked";
    private static final Logger LOGGER = Logger.getLogger(TimeLog.class);

    public static void addTimeLogEntry(StopWatch timer, ITestResult result) {
        String testResult;
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                testResult = PASSED;
                break;
            case ITestResult.FAILURE:
                testResult = FAILED;
                break;
            case ITestResult.SKIP:
                testResult = BLOCKED;
                break;
            default:
                throw new RuntimeException("Unknown test result: " + result);

        }
        String test = TestNgUtils.getTestName(result);
        String className = TestNgUtils.getTestClassName(result);
        timeLogList.add(Arrays.asList(test, String.valueOf(timer.getTime()/1000.0), testResult, className));
    }

    public static void writeTimeLogs() {
        while (!timeLogList.isEmpty()) {
            String nextClass = timeLogList.get(0).get(3); // timeLogList is nonempty, hence timeLogList.get(0) is fine
            List<List<String>> currentLogs = new ArrayList<>();
            for (List<String> test : timeLogList) {
                if (test.get(3).equals(nextClass)) {
                    currentLogs.add(test);
                }
            }
            writeTimeLog(currentLogs);
            timeLogList.removeAll(currentLogs);
        }
    }

    private static void writeTimeLog(List<List<String>> tests) {
        CSVReader reader;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
        CSVWriter writer;
        int medianIndex = -1;
        new File(Settings.TIMELOG_LOCATION).mkdirs();
        new File(Settings.TIMELOG_LOCATION + File.separator + "backups").mkdirs();

        String className = tests.get(0).get(3);

        try {
            final String CSV_LOG = Settings.TIMELOG_LOCATION + File.separator + className + "_TimeData.csv";

            List<String[]> oldCSVContents = new ArrayList<>();
            List<String[]> newContents = new ArrayList<>();

            if (new File(CSV_LOG).exists()) {
                reader = new CSVReader(new FileReader(CSV_LOG));
                oldCSVContents = reader.readAll();
                writer = new CSVWriter(new FileWriter(Settings.TIMELOG_LOCATION + File.separator + "backups" + File.separator + className + format.format(cal.getTime()) + ".csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
                writer.writeAll(oldCSVContents);
                // make backup in ./backups
                writer.close();
            }

            List<String> headers;

            if (oldCSVContents.isEmpty()) {
                // If there is no CSV present, set it up
                headers = new ArrayList<>();
                headers.add("date");
                // header array should have 2 columns per test for time and pass/fail, plus an extra column for date
                for (List<String> testList : tests) {
                    headers.add(testList.get(0) + "_Runtime_in_seconds");
                    headers.add(testList.get(0) + "_Result");
                    // Add test columns and corresponding pass/fail columns
                }
                newContents.add(headers.toArray(new String[headers.size()]));
            } else {
                // If there is a CSV already there, add whatever columns need to be there, maintaining the order of pre-existing columns
                reader = new CSVReader(new FileReader(CSV_LOG));
                headers = new ArrayList<>(Arrays.asList(reader.readNext()));
                // header array should have 2 columns per test for time and result, plus an extra column for date
                for (List<String> testList : tests) {
                    if (!headers.contains(testList.get(0) + "_Runtime_in_seconds")) {
                        headers.add(testList.get(0) + "_Runtime_in_seconds");
                        headers.add(testList.get(0) + "_Result");
                        // Add test columns and corresponding result columns
                    }
                }
                newContents.add(headers.toArray(new String[headers.size()]));
                if (oldCSVContents.size() > 5) {
                    for (int i = oldCSVContents.size() - 3; i < oldCSVContents.size(); i++) {
                        // only need to check last 3 entries
                        if (oldCSVContents.get(i)[0].contains("median")) {
                            medianIndex = i;
                            break;
                        }
                    }
                }
                // Assumption: if median data row exists, so does mean and trend. Order will be exactly median, mean, trend. Drop these three rows since they will be regenerated.
                if (medianIndex != -1) {
                    oldCSVContents.remove(medianIndex);
                    oldCSVContents.remove(medianIndex);
                    oldCSVContents.remove(medianIndex);
                    oldCSVContents.remove(medianIndex - 1); // and the blank row
                }
                oldCSVContents.remove(0);
                newContents.addAll(oldCSVContents);
                // drop old headers and stats, but keep all old data
            }
            new File(CSV_LOG).delete();

            String[] dataRow = new String[headers.size()];
            String[] medianRow = new String[headers.size()];
            String[] meanRow = new String[headers.size()];
            String[] trendRow = new String[headers.size()];
            // array should have 2 columns per test for time and pass/fail, plus an extra column for date
            dataRow[0] = format2.format(cal.getTime());
            // add date

            for (List<String> testList : tests) {
                if (headers.contains(testList.get(0) + "_Runtime_in_seconds")) {
                    // ... if this String is in the headers (e.g. it is a test name)
                    dataRow[headers.indexOf(testList.get(0) + "_Runtime_in_seconds")] = testList.get(1);
                    dataRow[headers.indexOf(testList.get(0) + "_Runtime_in_seconds") + 1] = testList.get(2);
                }
            }
            // add the row from the last execution
            newContents.add(dataRow);
            medianRow[0] = "median execution time (seconds)";
            meanRow[0] = "mean execution time (seconds)";
            trendRow[0] = "trend of execution time";
            for (int i = 1; i < headers.size(); i += 2) {
                // for every test time column...
                DescriptiveStatistics stats = new DescriptiveStatistics();
                SimpleRegression regression = new SimpleRegression();
                int pointsIndex = 0;
                for (int j = newContents.size() - 1; j > 0; j--) {
                    try {
                        if (newContents.get(j)[i] != null && !newContents.get(j)[i].equals("")) {
                            stats.addValue(Double.parseDouble(newContents.get(j)[i]));
                            if (pointsIndex < 5) {
                                regression.addData(5 - pointsIndex, Double.parseDouble(newContents.get(j)[i]));
                                pointsIndex++;
                                // only want 5 points for trend. Also added so that the latest points have maximal x value for simple regression
                            }
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException aioobe) { // csv might not be a perfect rectangle if tests were added or not every test was run at a given time
                    }

                    // Create list of every nonempty time in a given column
                }
                // Calculate stats on the list of times
                medianRow[i] = Double.toString(stats.getPercentile(50));
                meanRow[i] = Double.toString(stats.getMean());
                if (pointsIndex == 5) {
                    // only add an entry if there were at least 5 points to run regression on
                    double trendTolerance = 0.05;
                    double normalizedTrendSlope = regression.getSlope()/(stats.getMean()); // normalize by the mean
                    if (Math.abs(normalizedTrendSlope) < trendTolerance) {
                        trendRow[i] = "stable";
                    }
                    else if (normalizedTrendSlope > trendTolerance) {
                        trendRow[i] = "increasing";
                    }
                    else {
                        trendRow[i] = "decreasing";
                    }
                }
            }
            newContents.add(new String[headers.size()]);
            newContents.add(medianRow);
            newContents.add(meanRow);
            newContents.add(trendRow);
            writer = new CSVWriter(new FileWriter(CSV_LOG), ',', CSVWriter.NO_QUOTE_CHARACTER);
            writer.writeAll(newContents);

            writer.close();
        } catch (IOException ioe) {
            LOGGER.fatal("Failed to clear or setup time logs correctly.", ioe);
            throw new RuntimeException(ioe);
        } catch (NullPointerException npe) {
            LOGGER.error("Failed to clear or setup time logs correctly.", npe);
        }
    }

}
