package ru.alexfitness.sigurclientmonitor.util;

public interface SyncTaskListener {

    void handleProgress(int percent);
    void onSyncStart();
    void onSyncFailed();
    void onFinishAction();

}
