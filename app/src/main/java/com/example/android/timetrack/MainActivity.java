package com.example.android.timetrack;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.example.android.timetrack.data.TaskDbHelper;
import com.example.android.timetrack.data.TimerContract;
import com.example.android.timetrack.data.TimerContract.TimerEntry;

//main activity for opening screen and displaying available classes
//includes functions for create class button and display data button

public class MainActivity extends AppCompatActivity {

    //Message that is passed when clicking a listView item. This is classname
    public final static String EXTRA_MESSAGE = "com.example.androidexample";

    //creates database helper member variable
    private TaskDbHelper mDbHelper;

    //cursor adapter, allows us to put database info into listview
    private SimpleCursorAdapter simpleCursorAdapter;

    //instantiates list view
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //identifies the listView from XML File using its id
        listView = (ListView) findViewById(R.id.list);

        //creates a new database helper
        mDbHelper = new TaskDbHelper(this);

        //onClickListener used to see if the user pressed a list item
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {

                        //Create intent to send to DisplayClass
                        Intent intent = new Intent(MainActivity.this, DisplayClass.class);

                        //Finds the text that holds the class name in the listView
                        TextView textView = (TextView) view.findViewById(R.id.class_name);

                        //saves the string inside this text view to a variable
                        String text = textView.getText().toString();

                        //adds string to the intent and sends to the next activity
                        intent.putExtra(EXTRA_MESSAGE, text);
                        startActivity(intent);
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();

        //display all classes upon starting the activity
        displayClasses();
    }

    public void createClass(View view) {

        //go to create class task by sending intent
        //called when clicking on create class button
        Intent intent = new Intent(this, CreateClass.class);
        startActivity(intent);

    }

    public void displayData(View view){

        // go to display data task by sending intent
        //called when clicking on display data button
        Intent intent = new Intent(this, DisplayData.class);
        startActivity(intent);
    }

    private void displayClasses() {
        //called to populate the list view with the classes from the database using an adapter

        //gets a SQLite Database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //determines which columns to return
        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME};

        //requirements to filter rows by
        //cursor only takes rows with certain task name
        String selection = TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ?";

        //arguments to filter rows by
        //cursor only takes rows with task name "CLASS"
        String[] selectionArgs = {"CLASS"};

        //querys the database using the parameters defined above
        Cursor cursor = db.query(
                TimerContract.TimerEntry.TABLE_NAME,    // The table to query
                projection,                             // The columns to return
                selection,                              // The columns for the WHERE clause////
                selectionArgs,                          // The values for the WHERE clause
                null,                                   // Don't group the rows
                null,                                   // Don't filter by row groups
                null);                                  // The sort order


        //Error checking the cursor
        if (cursor == null)
        {
            return;
        }
        if (cursor.getCount() == 0)
        {
            return;
        }

        //which column from the cursor will be used for the adapter
        String[] columns = new String[] {TimerEntry.COLUMN_CLASS_NAME,};

        //which view to bind the data to for the adapter
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
