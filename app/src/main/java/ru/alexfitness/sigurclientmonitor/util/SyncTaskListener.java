package ru.alexfitness.sigurclientmonitor.util;

public interface SyncTaskListener {

    public void handleProgress(int percent);
    public void onSyncStart();
    public void onSyncFailed();
    public void onFinishAction();

}
