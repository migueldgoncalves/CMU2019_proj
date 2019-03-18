package com.example.todoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.proj.R;

public class MainActivity extends AppCompatActivity {

    private static ArrayList<Task> taskProperties = new ArrayList<>();
    private static ArrayAdapter<Task> taskAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskAdapter = new TaskArrayAdapter(this, 0, taskProperties);
        TableLayout albumTable = findViewById(R.id.album_layout);
        //albumTable.setAdapter(taskAdapter);

        findViewById(R.id.add_button).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView taskName = findViewById(R.id.taskText);
                int taskIndex = taskProperties.size();
                taskProperties.add(new Task (taskName.getText().toString(), taskIndex));
                taskAdapter.notifyDataSetChanged();
            }
        });
    }

    public static void removeTask(int taskIndex) {
        taskProperties.remove(taskIndex);
        taskAdapter.notifyDataSetChanged();
    }

}
