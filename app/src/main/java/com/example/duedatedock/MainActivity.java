package com.example.duedatedock;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;
    EditText assignmentInput;
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
        saveButton = findViewById(R.id.saveButton);
        listView = findViewById(R.id.listView);

        db = new DBHelper(this);

        assignmentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, assignmentList);
        listView.setAdapter(adapter);

        // Initialize selectedDate with today's date
        Calendar calendar = Calendar.getInstance();
        selectedDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
        loadAssignments();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            loadAssignments();
        });

        saveButton.setOnClickListener(v -> {
            String title = assignmentInput.getText().toString();

            if (!title.isEmpty()) {
                db.insertAssignment(title, selectedDate);
                assignmentInput.setText("");
                loadAssignments();
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            // Note: This only removes from the list, not the database. 
            // In a real app, you'd want to delete from DB too.
            assignmentList.remove(position);
            adapter.notifyDataSetChanged();
            return true;
        });
    }

    private void loadAssignments() {
        assignmentList.clear();

        Cursor cursor = db.getAssignments(selectedDate);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(1);
                String dueDate = cursor.getString(2);

                if (isOverdue(dueDate)) {
                    title += " 🔴 OVERDUE";
                }

                assignmentList.add(title);
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private boolean isOverdue(String dueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
            Date due = sdf.parse(dueDate);
            Date today = new Date();
            // Compare only dates (ignoring time)
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(due);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(today);
            
            if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true;
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR)) return true;
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
