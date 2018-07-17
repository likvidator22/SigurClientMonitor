package ru.alexfitness.sigurclientmonitor.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import ru.alexfitness.sigurclientmonitor.R;

public class MessageEditActivity extends Activity {

    public static final String SIGUR_PREFERENCE_NAME_KEY = "sigur_preference_name_key";

    private EditText editText;
    private String preference_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_edit);
        editText = findViewById(R.id.messageEditText);

        if(savedInstanceState!=null){
            preference_name = savedInstanceState.getString(SIGUR_PREFERENCE_NAME_KEY);
        } else {
            Intent intent = getIntent();
            preference_name = intent.getStringExtra(SIGUR_PREFERENCE_NAME_KEY);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String messageText = sharedPreferences.getString(preference_name,"");
        editText.setText(messageText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SIGUR_PREFERENCE_NAME_KEY, preference_name);
    }

    public void saveMessage(View view) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(preference_name, editText.getText().toString());
        editor.commit();
        finish();
    }


}
