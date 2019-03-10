package io.github.jgame.testListeners;

import org.testng.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.jgame.Constants.settings;
import static io.github.jgame.util.StringManager.fmt;

public class TestListener implements ITestListener {
    private Logger logger;

    public TestListener() {
        super();
        logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        // logger.fine(fmt("Started test '%s'", result.getName()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        /*log(fmt("Finished test '%s' in %sms (SUCCESS)", result.getName(),
                result.getEndMillis() - result.getStartMillis()), Level.WARNING);*/
    }

    @Override
    public void onTestFailure(ITestResult result) {
        /*log(fmt("TEST FAILED: '%s' in %sms", result.getName(),
                result.getEndMillis() - result.getStartMillis()), Level.SEVERE);*/
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        /*log(fmt("TEST SKIPPED: '%s' in %sms", result.getName(),
                result.getEndMillis() - result.getStartMillis()), Level.SEVERE);*/
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        /*log(fmt("TEST FAILED (in % range): '%s' in %sms", result.getName(),
                result.getEndMillis() - result.getStartMillis()), Level.SEVERE);*/
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        /*log(fmt("TEST TIMED OUT: %s' in %sms", result.getName(),
                result.getEndMillis() - result.getStartMillis()), Level.SEVERE);*/
    }

    @Override
    public void onStart(ITestContext context) {
        log(fmt("Running %s", context.getName()));
        TestRunner runner = (TestRunner) context;
        runner.setOutputDirectory(settings.getString("tests.testOut"));
    }

    private void log(String msg) {
        logger.log(Level.INFO, msg);
        Reporter.log(msg + "\n");
    }

    @Override
    public void onFinish(ITestContext context) {
        log(fmt("Finished %s", context.getName()));
    }
}
