package com.example.myapplication.utils;

import android.app.Activity;
import android.util.Log;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoogleFitUtil {
    public static final int REQUEST_OAUTH_REQUEST_CODE = 5001;

    private final Activity activity;
    private final FitnessOptions fitnessOptions;
    private DataCallback<Integer> pendingStepCallback;
    private DataCallback<Float> pendingSleepCallback;

    public interface DataCallback<T> {
        void onResult(T data);
    }

    public GoogleFitUtil(Activity activity) {
        this.activity = activity;
        fitnessOptions = FitnessOptions.builder()
                .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                .build();
    }

    private boolean hasPermissions() {
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions);
    }

    private void requestPermissions() {
        GoogleSignIn.requestPermissions(
                activity,
                REQUEST_OAUTH_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(activity),
                fitnessOptions);
    }

    public void getTodayStepCount(DataCallback<Integer> callback) {
        if (!hasPermissions()) {
            pendingStepCallback = callback;
            requestPermissions();
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        long end = cal.getTimeInMillis();

        DataReadRequest request = new DataReadRequest.Builder()
                .aggregate(com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA,
                        com.google.android.gms.fitness.data.DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .readData(request)
                .addOnSuccessListener(new OnSuccessListener<com.google.android.gms.fitness.result.DataReadResponse>() {
                    @Override
                    public void onSuccess(com.google.android.gms.fitness.result.DataReadResponse response) {
                        int total = 0;
                        for (com.google.android.gms.fitness.data.Bucket bucket : response.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet ds : dataSets) {
                                for (DataPoint dp : ds.getDataPoints()) {
                                    for (Field field : dp.getDataType().getFields()) {
                                        total += dp.getValue(field).asInt();
                                    }
                                }
                            }
                        }
                        callback.onResult(total);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("GoogleFitUtil", "Failed to read step count", e);
                        callback.onResult(0);
                    }
                });
    }

    public void getTodaySleepHours(DataCallback<Float> callback) {
        if (!hasPermissions()) {
            pendingSleepCallback = callback;
            requestPermissions();
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        long end = cal.getTimeInMillis();

        DataReadRequest request = new DataReadRequest.Builder()
                .read(com.google.android.gms.fitness.data.DataType.TYPE_SLEEP_SEGMENT)
                .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .readData(request)
                .addOnSuccessListener(new OnSuccessListener<com.google.android.gms.fitness.result.DataReadResponse>() {
                    @Override
                    public void onSuccess(com.google.android.gms.fitness.result.DataReadResponse response) {
                        long sleepMillis = 0;
                        for (DataSet ds : response.getDataSets()) {
                            for (DataPoint dp : ds.getDataPoints()) {
                                sleepMillis += dp.getEndTime(TimeUnit.MILLISECONDS) - dp.getStartTime(TimeUnit.MILLISECONDS);
                            }
                        }
                        callback.onResult(sleepMillis / (1000f * 60f * 60f));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("GoogleFitUtil", "Failed to read sleep data", e);
                        callback.onResult(0f);
                    }
                });
    }

    public void onPermissionResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (pendingStepCallback != null) {
                getTodayStepCount(pendingStepCallback);
                pendingStepCallback = null;
            }
            if (pendingSleepCallback != null) {
                getTodaySleepHours(pendingSleepCallback);
                pendingSleepCallback = null;
            }
        }
    }
}
