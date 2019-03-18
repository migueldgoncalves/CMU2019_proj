package com.example.todoapp;

public class Task {

    private String taskName;
    private int taskIndex;

    public Task(String taskName, int taskIndex) {
        this.taskName = taskName;
        this.taskIndex = taskIndex;
    }

    public String getTaskName() { return this.taskName; }

    public int getTaskIndex() { return this.taskIndex; }
}
