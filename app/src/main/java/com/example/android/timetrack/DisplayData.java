package com.example.android.timetrack;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.timetrack.data.TaskDbHelper;
import com.example.android.timetrack.data.TimerContract;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

/**
 * Created by Owner on 12/11/2016.
 */

public class DisplayData extends AppCompatActivity {

    //Message passed to ClassData in order to know what class to look for in DB
    public final static String EXTRA_MESSAGE = "com.example.androidexample";
    //holds the yData for the pie chart. elapsedTime
    private String[] yData;
    //holds xData for pie chart. className
    private String[] xData;
    //pie chart object
    PieChart pieChart;
    //DB helper
    TaskDbHelper mDbHelper;
    //Cursor adapter to turn arraylist of classNames into view on the spinner
    SimpleCursorAdapter simpleCursorAdapter;
    //drop down spinner
    Spinner spin;
    //view button to switch views
    Button button;
    //cursor that searches through DB. instantiated here because search criteria changes
    Cursor cursor;
    //used to know which class from the spinner was selected. helps pass classname to ClassData page
    int selection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        final Intent intent = getIntent();

        //instantiate DBHelper
        mDbHelper = new TaskDbHelper(this);

        //find views
        spin = (Spinner) findViewById(R.id.spinner);
        pieChart = (PieChart) findViewById(R.id.piechart);

        //initial pie setup
        pieChart.setRotationEnabled(false);
        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleAlpha(0);

        //sets the spinner to hold values of the classnames
        setupSpinner();
        //goes through DB and finds class names of all classes
        getClassNamesData();
        //goes through DB adds all elapsed times of all tasks for each class.
        getElapsedTimeData();
        //adds all data to the pie chart
        addDataSet(pieChart);

        //Retrieves information for the toast display upon click of pie chart
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                //h is passed by pie chart. holds values of x and y values of selection
                String classSubstring = h.toString().substring(14,15);
                //parses string to find x value of clicked class
                int pos1 = Integer.parseInt(classSubstring);

                //uses pos1 to find the class name and elapsed time of the selected task
                String className = xData[pos1];
                String hours = yData[pos1];
                //displays toast to user
                Toast.makeText(DisplayData.this, "Class: " + className + "\n" + "Hours: " + hours, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //change variable that says which data set to go to on ClassData
                selection = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                //nothing here
            }
        });

        button = (Button) findViewById(R.id.view_button);
        button.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //if there are no classes, do nothing. if there are classes, send data to ClassData and start activity
                try {
                    Intent intent = new Intent(getApplicationContext(), ClassData.class);
                    String className = xData[selection];
                    intent.putExtra(EXTRA_MESSAGE, className);
                    startActivity(intent);
                }
                catch (Exception ex){
                    return;
                }
            }
        });

    }

    private void addDataSet(PieChart pieChart) {

        //instantiates array of PieEntry. pie entry holds a string (class name) and float (elapsed time)
        ArrayList<PieEntry> Entrys = new ArrayList<>();

        //Error check to see if there is any data
        if (xData == null || yData == null)
        {
            Entrys.add(new PieEntry(0));
        }
        else {

            for (int i = 0; i < yData.length; i++) {
                //checks to see if value is zero. if it is, dont add it to the pie chart
                if(Float.parseFloat(yData[i]) > 0)
                    Entrys.add(new PieEntry(Float.parseFloat(yData[i]), xData[i]));
                else {
                    //do nothing
                }
            }

        }

        //Create data set and add formatting to chart
        PieDataSet pieDataSet = new PieDataSet(Entrys, "Class Hours");
        pieDataSet.setSliceSpace(0);
        pieDataSet.setValueTextSize(10);
        pieDataSet.setValueFormatter(new DefaultValueFormatter(3));
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

    }


    //grabs info from DB and puts it into spinner
    public void setupSpinner() {

        //class getAllClasses from TaskDBHelper
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //which columns to return
        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME,
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME,
                TimerContract.TimerEntry.COLUMN_PREDICTED_TIME};

        //which column to filter
        String selection = TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ?";
        //argument to filter by
        String[] selectionArgs = {"CLASS"};

        Cursor cursor = db.query(
                TimerContract.TimerEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                selection,                  // The columns for the WHERE clause////
                selectionArgs,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order


        String[] columns = new String[]{TimerContract.TimerEntry.COLUMN_CLASS_NAME,};

        //binds the data to the text view that holds the class name
        int[] boundTo = new int[] {
                android.R.id.text1,
        };


        simpleCursorAdapter = new android.widget.SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                cursor,
                columns,
                boundTo, 0);

        simpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(simpleCursorAdapter);

    }

    //gets class names from DB
    public void getClassNamesData(){

        //class getAllClasses from TaskDBHelper
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //columns to return
        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME,
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME,
                TimerContract.TimerEntry.COLUMN_PREDICTED_TIME};

        //column to filter
        String selection = TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ?";
        //columns to filter by
        String[] selectionArgs = {"CLASS"};

        Cursor cursor = db.query(
                TimerContract.TimerEntry.TABLE_NAME,    // The table to query
                projection,                             // Returns all columns
                selection,                              // filter the class and task name if the class and task name are not equal to (taskname and class)
                selectionArgs,                          //
                null,                                   // Don't group the rows
                null,                                   // Don't filter by row groups
                null);                                  // The sort order


        if (cursor == null) {
            return;
        }
        if (cursor.getCount() == 0) {
            return;
        }

        //moves to first row. we are about to pull data from the cursor.
        cursor.moveToFirst();

        //column to index through
        int classNamesColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_CLASS_NAME);

        //initialize array list to hold class names after iteration through cursor
        ArrayList<String> xDataList = new ArrayList<>();

        //iterate through cursor and add class names to arraylist
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String classNames = cursor.getString(classNamesColumnIndex);
            xDataList.add(classNames);
        }

        //convert the arraylist into a string array for piechart usage
        xData = xDataList.toArray(new String[xDataList.size()]);
    }


    public void getElapsedTimeData(){

        //class getAllClasses from TaskDBHelper
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //columns to return in cursor
        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME,
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME,
                TimerContract.TimerEntry.COLUMN_PREDICTED_TIME,
                TimerContract.TimerEntry.COLUMN_ACTIVE};

        //column to filter
        String selection = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ? and "
                + TimerContract.TimerEntry.COLUMN_ELAPSED_TIME + " != ?";

        //Arraylist to hold results from query to get data. eventually converted to String[] for pie chart use
        ArrayList<String> yDataList = new ArrayList<>();

        //iterates through DB and pulls elapsed time data for all tasks of a class (xData[i]).
        if (xData != null) {
            for (int i = 0; i < xData.length; i++) {

                //check above rows to see if they have the following criteria
                String[] selectionArgs = {xData[i], "0"};

                cursor = db.query(
                        TimerContract.TimerEntry.TABLE_NAME,    // The table to query
                        projection,                             // Returns all columns
                        selection,                              // filter the class and task name if the class and task name are not equal to (taskname and class)
                        selectionArgs,                          //
                        null,                                   // Don't group the rows
                        null,                                   // Don't filter by row groups
                        null);                                  // The sort order


                //moves to first row
                cursor.moveToFirst();

                //looking through
                int elapsedTimeColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_ELAPSED_TIME);

                Float elapsedTime = new Float(0);

                //iterates through cursor and adds all the tasks elapsed time for a certain class
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Float currentElapsedTime = cursor.getFloat(elapsedTimeColumnIndex);
                    elapsedTime += currentElapsedTime;
                }

                //turns into hours, cuts to 3 decimal places and adds to yData list
                elapsedTime /= 3600;
                String elapsedTimeTruncated = String.format("%.3f", elapsedTime);
                yDataList.add(elapsedTimeTruncated);

            }

            //converts Arraylist into string for piechart use.
            yData = yDataList.toArray(new String[yDataList.size()]);
        }
    }

}
