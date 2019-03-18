package com.example.todoapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TaskArrayAdapter extends ArrayAdapter<Task> {

    private Context context;
    private List<Task> taskProperties;
    private int taskIndex = -1;

    public TaskArrayAdapter(Context context, int resource, ArrayList<Task> objects) {
        super(context, resource, objects);

        this.context = context;
        this.taskProperties = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying
        Task task = taskProperties.get(position);
        taskIndex = task.getTaskIndex();

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.task_layout, null);

        TextView taskName = view.findViewById(R.id.taskName);
        taskName.setText(task.getTaskName());

        Button removeButton = view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.removeTask(taskIndex);
            }
        });

        return view;
    }
}
