package ru.alexfitness.sigurclientmonitor.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ru.alexfitness.sigurclientmonitor.Connection.SigurClientConnectionHandler;
import ru.alexfitness.sigurclientmonitor.Connection.SigurClientConnectionTask;
import ru.alexfitness.sigurclientmonitor.R;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurEvent;
import ru.alexfitness.sigurclientmonitor.util.MessageBuilder;
import ru.alexfitness.sigurclientmonitor.util.SyncTask;
import ru.alexfitness.sigurclientmonitor.util.SyncTaskListener;

public class MonitorActivity extends Activity implements SigurClientConnectionHandler, SyncTaskListener, TextToSpeech.OnInitListener {

    private TextView messageTextView;
    private ProgressBar progressBar;
    private Timer timer;
    private SharedPreferences preferences;
    private MessageBuilder messageBuilder;
    private int messageDelayTime;
    private TextToSpeech textToSpeech;

    SigurClientConnectionTask clientConnectionTask;
    SyncTask syncTask;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(MonitorActivity.this);

        if(preferences.getBoolean("enable_voice_pref", false)){
            textToSpeech = new TextToSpeech(this, this);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        messageBuilder = new MessageBuilder(this);
        messageTextView = findViewById(R.id.messageTextView);
        progressBar = findViewById(R.id.monitorProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        messageDelayTime = Integer.parseInt(preferences.getString("message_delay_pref","3000"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(clientConnectionTask==null) {
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
        } else {
            startMonitoring();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!syncDone()) {
            syncTask.cancel(true);
        }
        if(clientConnectionTask!=null){
            clientConnectionTask.cancel(true);
            clientConnectionTask.setHandler(null);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    //
    //SyncTaskListener

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

    //
    //SigurClientConnectionHandler

    @Override
    public void handleNewEvent(SigurEvent sigurEvent) {
        setMessageTextForEvent(sigurEvent);
        updateTimer();
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

    //
    //TextToSpeech.OnInitListener

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int res = textToSpeech.setLanguage(new Locale("ru"));
            if(res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED){
                textToSpeech = null;
                Toast.makeText(this, "Cant create textspeach", Toast.LENGTH_LONG).show();
            }
        }
    }

    //
    //service

    public void startMonitoring() {
        clientConnectionTask = new SigurClientConnectionTask(preferences);
        clientConnectionTask.setHandler(this);
        clientConnectionTask.execute();
    }

    private void setMessageTextForEvent(SigurEvent event) {
        String messageText = messageBuilder.buildSigurEventMessage(event);
        if (messageText != null) {
            messageTextView.setText(messageText);
            if(textToSpeech!=null){
                Bundle speakParam = new Bundle();
                speakParam.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1);
                textToSpeech.speak(messageText, TextToSpeech.QUEUE_FLUSH, speakParam, null);
            }
        }
    }

    private boolean syncDone(){
        return syncTask!=null && syncTask.getStatus()!= AsyncTask.Status.FINISHED;
    }

    private void updateTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new UpdateMessageTask(), messageDelayTime);
    }
}
