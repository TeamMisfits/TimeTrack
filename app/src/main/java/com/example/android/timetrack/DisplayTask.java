package com.example.android.timetrack;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.example.android.timetrack.data.TaskDbHelper;
import com.example.android.timetrack.data.TimerContract;
import java.util.concurrent.TimeUnit;
import static com.example.android.timetrack.DisplayClass.EXTRA_MESSAGE_THREE;


/**
 * Created by Owner on 12/11/2016.
 */

//activity to display the information for a specific task
//called when clicking on a specific task in DisplayClass activity
//includes functions to start the timer, stop the timer, finish a task, delete a task,
//and display the current data for the elapsed/predicted time

public class DisplayTask extends AppCompatActivity {

    //initializes the strings for task name and class name
    String taskname;

    String classname;

    //initializes the views for the textviews to display instructions, elapsed time, and predicted time
    TextView textView;

    TextView elapsedTimeView;

    TextView predictedTimeView;

    //initializes buttons for start and stop
    Button startButton;

    Button stopButton;

    Button finishTask;

    Button deleteTask;

    //initializes database helper
    TaskDbHelper mDbHelper;

    //initializes cursor adapter
    SimpleCursorAdapter simpleCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_task);

        //Receives intent from MainActivity and sets the classname equal to the received EXTRA_MESSAGE
        Intent intent = getIntent();

        //Finds the text view that displays classname
        textView = (TextView) findViewById(R.id.info);

        //receives the string array with task name and class name from display class activity
        String[] taskClassArray = intent.getStringArrayExtra(EXTRA_MESSAGE_THREE);

        //uses strings from intent to populate class name and task name
        taskname = taskClassArray[0];

        classname = taskClassArray[1];

        //sets text to class name and taskname
        textView.setText(classname + ": " + taskname);

        //creates new database helper
        mDbHelper = new TaskDbHelper(this);

        //finds views for elapsed time, predicted time, and start/stop buttons
        elapsedTimeView = (TextView) findViewById(R.id.time_spent);

        predictedTimeView = (TextView) findViewById(R.id.predicted);

        startButton = (Button) findViewById(R.id.startButton);

        stopButton = (Button) findViewById(R.id.stopButton);

        finishTask = (Button) findViewById(R.id.finish_button);

        deleteTask = (Button) findViewById(R.id.delete_button);

        //displays the data for elapsed/predicted times
        displayData();

        //determines which start/stop button to be invisible
        if (checkActive())
        {
            //if task is currently being timed, start button is made invisible
            startButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            //if task is not being timed, stop button is invisible
            stopButton.setVisibility(View.INVISIBLE);
        }

        //Set a clickListener on that view
        finishTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmFinish(view);
            }
        });

        //Set a clickListener on that view
        deleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete(view);
            }
        });

    }

    public void displayData() {
        //function to display the elapsed and predicted time for the task

        //makes a readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //which columns are going to be returned in the cursor
        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME,
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME,
                TimerContract.TimerEntry.COLUMN_PREDICTED_TIME};

        //which rows will be returned in the cursor
        //task name must be equal to taskname and class name must be equal to classname
        String selection = TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ? and " +
                TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ?";

        //arguments for the selection string
        String[] selectionArgs = {taskname, classname};

        //queries the database using above arguments to make new cursor
        Cursor cursor = db.query(
                TimerContract.TimerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        //error checking the cursor
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() == 0) {
            return;
        }

        //moves to first row of cursor
        cursor.moveToFirst();

        //gets indices for colums with elapsed time and predicted time
        int elapsedTimeColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_ELAPSED_TIME);

        int predictedTimeColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_PREDICTED_TIME);

        //gets the value from elapsed time as a long data type
        long elapsedTime = cursor.getLong(elapsedTimeColumnIndex);

        //gets the value for the predicted time as a double
        double predictedTime = cursor.getDouble(predictedTimeColumnIndex);

        //converts elapsedTime to double
        double seconds = (double)elapsedTime;

        //converts seconds into hours
        seconds = seconds / 3600;

        //creates strings with the elapsed time and predicted time to be displayed
        //sets these strings as the text for the elapsed time and predicted time textViews

        String elapsedTimeTruncated = String.format("%.3f", seconds);
        elapsedTimeView.setText((elapsedTimeTruncated) + " hours");

        String predictedTimeTruncated = String.format("%.3f", predictedTime);
        predictedTimeView.setText((predictedTimeTruncated) + " hours");
    }

    public void changeStartTime(View view)
    {
        //changes the start time in the database and sets activity to "ACTIVE/STARTED"

        //obtains the current time in seconds since January 1, 1970 using System.currentTimeMillis function
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        //gets a readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // object which takes values to be placed in database
        ContentValues values = new ContentValues();

        //populates values with values to be put into the database
        //enters values for the start time and changes the active state to "ACTIVE/STARTED"
        //to indicate timing is currently happening
        values.put(TimerContract.TimerEntry.COLUMN_START_TIME, timeSeconds);
        values.put(TimerContract.TimerEntry.COLUMN_ACTIVE, "ACTIVE/STARTED");

        // Which row to update, based on the classname and taskname
        String selection = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ? and " +
                TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ?";

        //arguments for the above sql statement string
        String[] selectionArgs = {classname, taskname};

        //updates the database row using the given arguments above
        int count = db.update(
                TimerContract.TimerEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        //changes the start button to invisible and stop button to visible
        startButton.setVisibility(View.INVISIBLE);

        stopButton.setVisibility(View.VISIBLE);
    }

    public void changeStopTime(View view)
    {
        //changes the stop time in the database and changes activity to "ACTIVE/STOPPED"

        //gets the current time as done above
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        //gets a readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //which columns to be returned in the cursor
        String[] projection = {
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME };

        //which rows to be returned in the cursor
        //class name should be classname and task name should be taskname
        String selection = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ? and " +
                TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ?";

        //arguments for above sql statement
        String[] selectionArgs = {classname, taskname};

        //queries the database using the above arguments
        Cursor cursor = db.query(
                TimerContract.TimerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        //moves to the first row of the cursor
        cursor.moveToFirst();

        //gets indices for columns with start time and elapsed time
        int startTimeColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_START_TIME);
        int elapsedTimeColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_ELAPSED_TIME);

        // Uses those indices to get the start time and elapsed time
        // at the current row the cursor is on.
        long startTime = cursor.getLong(startTimeColumnIndex);

        long elapsedTime = cursor.getLong(elapsedTimeColumnIndex);

        //increments the elapsed time by how much time was spent in the most recent session
        //time = current time - start time
        elapsedTime += (timeSeconds - startTime);

        // object to place updates values in to later be inserted into database
        ContentValues values = new ContentValues();

        //puts new elapsed time in and updates activity to "ACTIVE/STOPPED"
        values.put(TimerContract.TimerEntry.COLUMN_ELAPSED_TIME, elapsedTime);
        values.put(TimerContract.TimerEntry.COLUMN_ACTIVE, "ACTIVE/STOPPED");

        //uses same selection and selection args as above
        //since accessing the same row

        //updates the specific row in the database
        int count = db.update(
                TimerContract.TimerEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        //changes the start button to visible and stop button to invisible
        startButton.setVisibility(View.VISIBLE);

        stopButton.setVisibility(View.INVISIBLE);

        //redisplays the data with the updated values
        displayData();
    }

    public void finishTask(View view)
    {
        //function to make a task "finished"

        //checks if the task is currently being time
        //if it is, stops the timing for the task
        if (checkActive())
        {
            changeStopTime(view);
        }

        //makes readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //which columns will be returned in the cursor
        String[] projection = {
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME };

        //which rows will be changed in the cursor
        //class name is classname and task name is taskname
        String selection = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ? and " +
                TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ?";

        //arguments for above sql statement
        String[] selectionArgs = {classname, taskname};

        //makes object to place updated values in
        ContentValues values = new ContentValues();

        //changes the activity column to "INACTIVE"
        values.put(TimerContract.TimerEntry.COLUMN_ACTIVE, "INACTIVE");

        //updates the database given the above arguments
        int count = db.update(
                TimerContract.TimerEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // returns to the display class activity
        finish();
    }

    public void deleteTask(View view)
    {
        //function to delete a task

        //gets a writeable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //which rows will be deleted
        //class name is classname and task name is taskname
        String whereClause = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ? and " +
                TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ?";

        //arguments for above sql statement
        String[] whereArgs = new String[] { classname, taskname };

        //deletes the row in the database satisfying the above arguments
        db.delete(TimerContract.TimerEntry.TABLE_NAME, whereClause, whereArgs);

        //returns to displayClass activity
        finish();
    }

    public boolean checkActive() {
        //function to check if a task is currently being timed

        //gets readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //which columsn will be returned
        String[] projection = {
                TimerContract.TimerEntry.COLUMN_ACTIVE};

        //which rows will be returned
        //class name is classname and task name is taskname
        String selection = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ? and " +
                TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ?";

        //arguments for above sql statement
        String[] selectionArgs = {classname, taskname};

        //queries the database using above arguments
        Cursor cursor = db.query(
                TimerContract.TimerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        //moves to the first row of cursor
        cursor.moveToFirst();

        ///gets index for activity column
        int activeColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_ACTIVE);

        //uses above index to access the value in the activity column
        String active = cursor.getString(activeColumnIndex);

        //returns true if the task is being timed, returns false is task is not being timed
        if (active.equals("ACTIVE/STARTED"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public void confirmFinish(final View view){
        //error checking for the delete class button
        //creates pop up menu to confirm that class should be deleted
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Finish this Task?" + "\n" + "It will be hidden from Task List.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishTask(view);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void confirmDelete(final View view){
        //error checking for the delete class button
        //creates pop up menu to confirm that class should be deleted
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Delete this Task?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTask(view);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}

