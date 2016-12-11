package com.example.android.timetrack;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.timetrack.data.TaskDbHelper;
import com.example.android.timetrack.data.TimerContract;

/**
 * Created by Owner on 12/11/2016.
 */

//activity to make a new class, called when create task button clicked in displayClass activity
//includes function to make a new task and check if the task already exists
public class CreateTask extends AppCompatActivity {

    //defines the edit text views
    private EditText mTaskNameEditText;

    private EditText mPredictedTimeEditText;

    //database helper
    TaskDbHelper mDbHelper;

    //Elements to be stored in the new row of the database
    String classNameString;
    String taskNameString;
    long startTime;
    long elapsedTime;
    double predictedTime;
    String active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        //Receives intent from MainActivity and sets the classname equal to the received EXTRA_MESSAGE
        Intent intent = getIntent();

        classNameString = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //finds the taskname and predicted time editText views
        mTaskNameEditText = (EditText) findViewById(R.id.getTaskName);

        mPredictedTimeEditText = (EditText) findViewById(R.id.getPredictedTime);

        // Create database helper
        mDbHelper = new TaskDbHelper(this);
    }

    public void saveTask(View view) {

        //Initialize the info to be placed in the new class
        //classNameString already properly defined
        startTime = 0;
        elapsedTime = 0;
        active = "ACTIVE/STOPPED";

        //try task name string entry
        if (mTaskNameEditText.getText().toString().trim().equals("")) {
            mTaskNameEditText.setError("Please Enter a Class Name");
            return;
        }
        else {
            taskNameString = mTaskNameEditText.getText().toString().trim();
        }

        //try prediction entry
        Double tryPrediction;
        try {
            tryPrediction =  predictedTime = Double.parseDouble(mPredictedTimeEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            mPredictedTimeEditText.setError("Please Provide a Prediction");
            return;
        }


        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and attributes from the editor are the values.
        ContentValues values = new ContentValues();

        //checks if the task already exists
        boolean result = isDuplicateTask(taskNameString);

        //if a novel task, populates the values object
        if(!result) {
            //places values into the database
            values.put(TimerContract.TimerEntry.COLUMN_CLASS_NAME, classNameString);
            values.put(TimerContract.TimerEntry.COLUMN_TASK_NAME, taskNameString);
            values.put(TimerContract.TimerEntry.COLUMN_START_TIME, startTime);
            values.put(TimerContract.TimerEntry.COLUMN_ELAPSED_TIME, elapsedTime);
            values.put(TimerContract.TimerEntry.COLUMN_PREDICTED_TIME, predictedTime);
            values.put(TimerContract.TimerEntry.COLUMN_ACTIVE, active);

            // Insert a new row in the database using the values in "values" object, returning the ID of that new row.
            long newRowId = db.insert(TimerContract.TimerEntry.TABLE_NAME, null, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newRowId == -1) {
                // If the row ID is -1, then there was an error with insertion.
                Toast.makeText(this, "Error with saving", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful
                Toast.makeText(this, "Created Task: " + taskNameString + " in " + classNameString, Toast.LENGTH_SHORT).show();
            }
            //returns to displayClass activity
            finish();
        }
        //if task exists, returns error toast message
        else if (result){
            Toast.makeText(this,  taskNameString + " Already Exists!", Toast.LENGTH_SHORT).show();
        }
    }



    public boolean isDuplicateTask(String inputTaskName) {
        //checks if the task already exists

        //gets a SQLite Database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //determines which columns to return
        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME
        };

        //which rows to return
        //classname is the specified class the task is for
        String selection = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ?";

        //arguments to filter rows by
        String[] selectionArgs = {classNameString};

        //querys the database using the parameters defined above
        Cursor cursor = db.query(
                TimerContract.TimerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        //Error checking the cursor
        if (cursor == null)
        {
            return false;
        }
        if (cursor.getCount() == 0)
        {
            return false;
        }

        //moves cursor to firstrow
        cursor.moveToFirst();

        //gets index specified taskname column
        int taskColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_TASK_NAME);

        //for all the rows, if the inputTaskName equals a name of an existing task return false.
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            String taskName = cursor.getString(taskColumnIndex);

            if(inputTaskName.equals(taskName))
                return true;
        }

        return false;
    }

}

