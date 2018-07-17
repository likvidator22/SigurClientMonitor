package ru.alexfitness.sigurclientmonitor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ru.alexfitness.sigurclientmonitor.R;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurEventType;

public class MessageSettingsActivity extends Activity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_settings);
        listView = findViewById(R.id.eventtypesListView);

        ArrayAdapter<SigurEventType> arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, SigurEventType.values());
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SigurEventType type = (SigurEventType) listView.getAdapter().getItem(position);
                /*Intent intent = new Intent(MessageSettingsActivity.this, MessageEditActivity.class);
                intent.putExtra(MessageEditActivity.SIGUR_PREFERENCE_NAME_KEY, type.name());
                startActivity(intent);*/
                openMessageEditor(type.name());
            }
        });

        TextView waitingMessageTextView = findViewById(R.id.waitingMessageItemTextView);
        waitingMessageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(MessageSettingsActivity.this, MessageEditActivity.class);
                intent.putExtra(MessageEditActivity.SIGUR_PREFERENCE_NAME_KEY, getString(R.string.waiting_message_pref));
                startActivity(intent);*/
                openMessageEditor(getString(R.string.waiting_message_pref));
            }
        });
    }

    private void openMessageEditor(String messagePrefName){
        Intent intent = new Intent(this, MessageEditActivity.class);
        intent.putExtra(MessageEditActivity.SIGUR_PREFERENCE_NAME_KEY, messagePrefName);
        startActivity(intent);
    }

}
