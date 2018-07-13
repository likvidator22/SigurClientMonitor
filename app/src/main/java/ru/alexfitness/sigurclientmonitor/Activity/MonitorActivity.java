package ru.alexfitness.sigurclientmonitor.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import ru.alexfitness.sigurclientmonitor.Connection.SigurClientConnectionHandler;
import ru.alexfitness.sigurclientmonitor.Connection.SigurClientConnectionTask;
import ru.alexfitness.sigurclientmonitor.R;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurEvent;
import ru.alexfitness.sigurclientmonitor.util.MessageBuilder;
import ru.alexfitness.sigurclientmonitor.util.SyncTask;
import ru.alexfitness.sigurclientmonitor.util.SyncTaskListener;

public class MonitorActivity extends Activity implements SigurClientConnectionHandler, SyncTaskListener {

    private TextView messageTextView;
    private ProgressBar progressBar;
    private Timer timer;
    private SharedPreferences preferences;
    private MessageBuilder messageBuilder;
    private int messageDelayTime;

    SigurClientConnectionTask clientConnectionTask;
    SyncTask syncTask;

    @Override
    public void handleProgress(int percent) {
        progressBar.setProgress(percent);
    }

    @Override
    public void onSyncStart() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSyncFailed() {
        progressBar.setVisibility(View.INVISIBLE);
        messageTextView.setText(R.string.sync_failed_text);
        Toast.makeText(this, getString(R.string.sync_failed_text), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFinishAction() {
        progressBar.setVisibility(View.INVISIBLE);
        startMonitoring();
    }

    private class UpdateMessageTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageTextView.setText(preferences.getString(getString(R.string.waiting_message_pref), ""));
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        messageBuilder = new MessageBuilder(this);

        messageTextView = findViewById(R.id.messageTextView);
        progressBar = findViewById(R.id.monitorProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        preferences = PreferenceManager.getDefaultSharedPreferences(MonitorActivity.this);
        messageDelayTime = Integer.parseInt(preferences.getString("message_delay_pref","3000"));

        syncTask = new SyncTask(preferences, this);
        syncTask.setListener(this);

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            messageTextView.setText(R.string.sync_in_progress_text);
            syncTask.execute();
        } else {
            messageTextView.setText(getString(R.string.no_internet_text));
        }

    }

    public void startMonitoring() {
        clientConnectionTask = new SigurClientConnectionTask(preferences);
        clientConnectionTask.setHandler(this);
        clientConnectionTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(syncTask!=null) {
            syncTask.cancel(true);
        }
        if(clientConnectionTask!=null){
            clientConnectionTask.cancel(true);
        }
    }

    @Override
    public void handleNewEvent(SigurEvent sigurEvent) {
        setMessageTextForEvent(sigurEvent);
        updateTimer();
    }

    private void setMessageTextForEvent(SigurEvent event) {
         String messageText = messageBuilder.buildSigurEventMessage(event);
        if (messageText != null) {
            messageTextView.setText(messageText);
        }
    }

    private void updateTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new UpdateMessageTask(), messageDelayTime);
    }

    @Override
    public void handleClientShutDown() {
        messageTextView.setText(getString(R.string.server_down_text));
    }

    @Override
    public void handleClientStartUp() {
        updateTimer();
        messageTextView.setText(getString(R.string.connecting_text));
    }
}
