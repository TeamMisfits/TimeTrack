package com.example.android.timetrack;

/**
 * Created by Owner on 12/11/2016.
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.timetrack.data.TaskDbHelper;
import com.example.android.timetrack.data.TimerContract;

//activity to display the specific data for a certain class
//called after clicking on a class in the list view in main activity
//includes function to delete the class, error check deleting a task,
// move to the create a task screen, and display all of the tasks for the class
public class DisplayClass extends AppCompatActivity {

    //Sends classname to createTask so that it knows what to set as classname
    public final static String EXTRA_MESSAGE_TWO = "com.example.androidexample";

    //sends taskname to displayTask so that it can associate information with that row
    public final static String EXTRA_MESSAGE_THREE = "com.example.androidexample";

    //cursor adapter, allows us to put database info into listview
    private SimpleCursorAdapter simpleCursorAdapter;

    //Instantiates text view to display class name
    TextView textView;

    //instantiates button for deleting class
    Button button;

    //Instantiates string to hold classname
    String classname;

    //Database helper
    TaskDbHelper mDbHelper;

    //instantiates list view
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_class);

        //Receives intent from MainActivity and sets the classname equal to the received EXTRA_MESSAGE
        Intent intent = getIntent();

        //finds the list where tasks will be displayed
        listView = (ListView) findViewById(R.id.list);

        //finds the text view where the class name will be displayed
        textView = (TextView) findViewById(R.id.class_title);

        //finds the delete button
        button = (Button) findViewById(R.id.delete_button);

        //Create a new TaskDbHelper
        mDbHelper = new TaskDbHelper(this);


        //recieves string from MainActivity and sets to variable classname
        classname = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //sets text of the found textview to classname which is the class title
        textView.setText(classname);


        //onClickListener used to see if the user pressed a list item. Takes to associated task if they do
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3) {

                        //creates new intent for DisplayTask
                        Intent intent = new Intent(DisplayClass.this, DisplayTask.class);

                        //Finds the text that holds the task name in the listView
                        TextView textView = (TextView) view.findViewById(R.id.class_name);
                        //creates a string array with the task name and the class name to pass to the display task activity
                        String[] text = new String[] {textView.getText().toString(), classname};
                        intent.putExtra(EXTRA_MESSAGE_THREE, text);
                        startActivity(intent);
                    }
                }
        );

        //Set a clickListener on that view
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmClick();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //display all tasks upon starting the activity
        displayTasks();
    }


    public void confirmClick(){
        //error checking for the delete class button
        //creates pop up menu to confirm that class should be deleted
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Delete this Class?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteClass();
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


    public void deleteClass(){
        //function to delete a class and all of its entries from the database

        //gets SQLite database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //Looks in COLUMN_CLASS_NAME
        String whereClause = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ?";

        //looks in COLUMN_CLASS_NAME for a row with name stored in classname (the name of the current class being deleted
        String[] whereArgs = new String[] { classname };

        //deletes all the entries that meet above criteria
        //deletes all tasks for the given class from the database
        db.delete(TimerContract.TimerEntry.TABLE_NAME, whereClause, whereArgs);

        //Outputs a toast message to confirm deleting
        Toast.makeText(this, classname + " Deleted", Toast.LENGTH_SHORT).show();

        //Returns to main activity
        finish();
    }


    public void createTask(View view){
        //function called by create task button to move to create task activity

        Intent intent = new Intent(DisplayClass.this, CreateTask.class);

        //pass the classname so that a task can also have the class name in its row
        intent.putExtra(EXTRA_MESSAGE_TWO, classname);
        startActivity(intent);
    }


    private void displayTasks() {
        //function to display all of the tasks for the specified class

        //gets SQLite Database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //determines which columns to return
        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME,
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME,
                TimerContract.TimerEntry.COLUMN_PREDICTED_TIME,
                TimerContract.TimerEntry.COLUMN_ACTIVE};

        //look in the following rows (for next action)
        //looks for rows with classname equal to the specified class
        //looks for rows with task that doesnt equal "CLASS", the keyword for the class row (not a task)
        //looks for rows with task that is active
        String selection = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ? and "
                + TimerContract.TimerEntry.COLUMN_TASK_NAME + " != ? and "
                + TimerContract.TimerEntry.COLUMN_ACTIVE + " != ?";


        //check above rows to see if they have the following criteria
        String[] selectionArgs = {classname, "CLASS", "INACTIVE"};

        //querys database using above arguments
        Cursor cursor = db.query(
                TimerContract.TimerEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                selection,                  // The columns for the WHERE clause////
                selectionArgs,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order

        //Error checking
        if (cursor == null)
        {
            return;
        }
        if (cursor.getCount() == 0)
        {
            return;
        }

        //Which columns to look in
        String[] columns = new String[] {TimerContract.TimerEntry.COLUMN_TASK_NAME,};

        //binds the data to the text view that holds the class name
        int[] boundTo = new int[] {R.id.class_name,};

        //displays in listView using simpleCursorAdapter
        simpleCursorAdapter = new android.widget.SimpleCursorAdapter(this,
                R.layout.layout,
                cursor,
                columns,
                boundTo,
                0);

        listView.setAdapter(simpleCursorAdapter);
    }


}