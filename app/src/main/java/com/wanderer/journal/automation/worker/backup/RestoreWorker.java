package com.wanderer.journal.automation.worker.backup;

import android.content.Context;

import androidx.work.WorkerParameters;
import androidx.work.rxjava3.RxWorker;

import org.jspecify.annotations.NonNull;

import io.reactivex.rxjava3.core.Single;

public class RestoreWorker extends RxWorker {
    public RestoreWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public @NonNull Single<Result> createWork() {
        return null;
    }
}
