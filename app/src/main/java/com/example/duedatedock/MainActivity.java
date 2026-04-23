package com.example.duedatedock;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;
    EditText assignmentInput, classInput, notesInput;
    Spinner prioritySpinner;
    Button saveButton;
    ListView listView;

    DBHelper db;
    String selectedDate = "";

    ArrayList<String> assignmentList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        assignmentInput = findViewById(R.id.assignmentInput);
        classInput = findViewById(R.id.classInput);
        notesInput = findViewById(R.id.notesInput);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        saveButton = findViewById(R.id.saveButton);
        listView = findViewById(R.id.listView);

        db = new DBHelper(this);

        // Priority dropdown
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                priorities
        );
        prioritySpinner.setAdapter(spinnerAdapter);

        assignmentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                assignmentList);

        listView.setAdapter(adapter);

        // Default date (today)
        Calendar calendar = Calendar.getInstance();
        selectedDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);

        loadAssignments();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            loadAssignments();
        });

        // SAVE
        saveButton.setOnClickListener(v -> {

            String title = assignmentInput.getText().toString().trim();
            String className = classInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();

            if (!title.isEmpty()) {

                db.insertAssignment(title, selectedDate, priority, className, notes);

                assignmentInput.setText("");
                classInput.setText("");
                notesInput.setText("");

                loadAssignments();

            } else {
                Toast.makeText(this, "Enter assignment title", Toast.LENGTH_SHORT).show();
            }
        });

        // SAFE DELETE (FIXED)
        listView.setOnItemLongClickListener((parent, view, position, id) -> {

            Cursor cursor = db.getAssignments(selectedDate);

            if (cursor != null && cursor.moveToFirst()) {

                int index = 0;

                do {
                    if (index == position) {
                        int dbId = cursor.getInt(0);
                        db.deleteAssignment(dbId);
                        break;
                    }
                    index++;
                } while (cursor.moveToNext());

                cursor.close();
            }

            loadAssignments();
            return true;
        });
    }

    // LOAD DATA (SAFE VERSION)
    private void loadAssignments() {

        assignmentList.clear();

        Cursor cursor = db.getAssignments(selectedDate);

        if (cursor != null && cursor.moveToFirst()) {

            do {
                String title = cursor.getString(1);
                String dueDate = cursor.getString(2);
                String priority = cursor.getString(3);
                String className = cursor.getString(4);
                String notes = cursor.getString(5);

                // Priority icon
                String priorityIcon = "";
                if (priority != null) {
                    switch (priority) {
                        case "High":
                            priorityIcon = " 🔴 High";
                            break;
                        case "Medium":
                            priorityIcon = " 🟡 Medium";
                            break;
                        case "Low":
                            priorityIcon = " 🟢 Low";
                            break;
                    }
                }

                // Overdue check
                if (isOverdue(dueDate)) {
                    title += " 🔴 OVERDUE";
                }

                String display =
                        title +
                                "\n Class: " + className +
                                "\n Notes: " + notes +
                                "\n" + priorityIcon;

                assignmentList.add(display);

            } while (cursor.moveToNext());

            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    // OVERDUE CHECK (SAFE)
    private boolean isOverdue(String dueDate) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Date due = sdf.parse(dueDate);

            return due != null && due.before(new Date());

        } catch (Exception e) {
            return false;
        }
    }
}