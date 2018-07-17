package ru.alexfitness.sigurclientmonitor.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import ru.alexfitness.sigurclientmonitor.R;

public class MainActivity extends Activity {

    public static final int REMOVE_SETTINGS_REQUEST_CODE = 1;
    public static final int REMOVE_SETTINGS_RESULT_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView navListView = findViewById(R.id.mainNavigationListView);
        navListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, MonitorActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, MessageSettingsActivity.class));
                        break;
                    case 3:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(getString(R.string.remove_settings_message));
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(MainActivity.this, RemoveSettingsActivity.class), REMOVE_SETTINGS_REQUEST_CODE);
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, null);
                        builder.create().show();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REMOVE_SETTINGS_REQUEST_CODE:
                if(resultCode==REMOVE_SETTINGS_RESULT_OK){
                    Toast.makeText(this, getString(R.string.remove_settings_result_ok),Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }
}
