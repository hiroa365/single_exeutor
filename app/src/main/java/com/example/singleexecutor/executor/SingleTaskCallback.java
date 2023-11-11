package com.example.singleexecutor.executor;

import com.example.singleexecutor.Result;

public interface SingleTaskCallback {
    void onFinish(Result result);
}
