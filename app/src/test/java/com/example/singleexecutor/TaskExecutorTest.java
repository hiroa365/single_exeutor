package com.example.singleexecutor;

import com.example.singleexecutor.executor.SingleTaskCallback;
import com.example.singleexecutor.executor.TaskExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

public class TaskExecutorTest {

    TaskExecutor singleTaskExecutor = new TaskExecutor();

    private class WaitCallable implements Callable {

        @Override
        public Object call() throws Exception {
            Thread.sleep(1 * 1000);
            boolean sample = new Random().nextBoolean();
            if (sample) {
                return "OK";
            } else {
                throw new TestException();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void submit_single() {

        ArrayList<Result> results = new ArrayList<>();

        class Callback implements SingleTaskCallback {
            @Override
            public void onFinish(Result result) {
                results.add(result);
            }
        }

        var result_1 = singleTaskExecutor.submit(new WaitCallable(), new Callback());
        var result_2 = singleTaskExecutor.submit(new WaitCallable(), new Callback());

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var result_3 = singleTaskExecutor.submit(new WaitCallable(), new Callback());
    }

    @Test
    public void cancelTask() {
    }

    public static class TestException extends Exception {
    }
}