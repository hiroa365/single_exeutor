package com.example.singleexecutor.executor;

import androidx.annotation.NonNull;

import com.example.singleexecutor.Result;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 上位アプリの要求を非同期実行するためのExecutorクラス
 * <p>
 * 実行できるタスクは常に1つのため、SingleThreadExecutorで十区できるタスクを1つに絞り
 * 他のタスクを実行中の場合はエラーで返却します。
 * 非同期処理の実行結果は、コールバックで返却します。
 */
public class TaskExecutor {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future runningTask = null;

    /**
     * 非同期タスク実行
     * <p>
     * 非同期でタスクを処理してonFinishで結果を返却します。
     *
     * @param task     非同期実行するCallableクラス
     * @param callback 完了コールバック
     * @return true：非同期処置開始成功、 false：非同期処理開始失敗
     */
    public boolean submit(@NonNull Callable task, @NonNull SingleTaskCallback callback) {
        //処理中の場合はエラー返却
        if (isBusy()) {
            return false;
        }

        runningTask = executorService.submit(task);

        //Executorの実行完了を別スレッドで待機し、コールバックで結果を通知
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    var result = runningTask.get();
                    callback.onFinish(new Result.Success(result));
                } catch (ExecutionException e) {
                    //実行処理でexception発生した場合はここで拾う
                    callback.onFinish(new Result.Error(e));
                } catch (InterruptedException e) {
                    //executorが中断された場合はここで拾う
                    callback.onFinish(new Result.Error(e));
                } catch (Exception e) {
                    //想定外エラーはここで拾う
                    callback.onFinish(new Result.Error(e));
                } finally {
                    //非同期処理完了後は、必ず実行中タスクをnullにする
                    runningTask = null;
                }
            }
        }).start();

        return true;
    }

    /**
     * タスクキャンセル
     * <p>
     * 実行中のタスクがある場合は、タスクのキャンセルを行います。
     */
    public void cancelTask() {
        if (runningTask != null) {
            runningTask.cancel(true);
        }
    }

    /**
     * 処理中判定
     *
     * @return true 処理中
     */
    private boolean isBusy() {
        if (runningTask == null) {
            return false;
        } else if (runningTask.isDone()) {
            return false;
        } else if (runningTask.isCancelled()) {
            return false;
        } else {
            return true;
        }
    }
}
