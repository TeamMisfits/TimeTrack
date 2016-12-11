package com.example.android.timetrack;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.timetrack.data.TaskDbHelper;
import com.example.android.timetrack.data.TimerContract;
import java.util.ArrayList;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

/**
 * Created by Owner on 12/11/2016.
 */

public class ClassData extends AppCompatActivity {
    //string[] that holds yData. elapsed time.
    private String[] yData;
    //string[] that holds xData. classname
    private String[] xData;
    //DB helper
    TaskDbHelper mDbHelper;
    //piechart object
    PieChart pieChart;
    //cursor to look through DB
    Cursor cursor;
    //name of class. passed from DisplayData activity
    String className;
    //textview that displays classname
    TextView class_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_data);

        final Intent intent = getIntent();

        //receive classname, find text view and classname as text view
        className = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        class_name = (TextView) findViewById(R.id.class_name);
        class_name.setText(className);

        //instantiate DB helper
        mDbHelper = new TaskDbHelper(this);
        //finds piechart
        pieChart = (PieChart) findViewById(R.id.piechart);
        //initial pie chart setup
        pieChart.setRotationEnabled(false);
        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleAlpha(0);

        //goes through DB and finds all task names for the specific class
        getTaskNamesData();
        //goes through DB and gets elapsed time for each task
        getElapsedTimeData();
        //adds the data to the pie chart
        addDataSet(pieChart);

        //Retrieves information for the toast display upon click of pie chart
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                //h is passed by pie chart. holds values of x and y values of selection
                String taskSubstring = h.toString().substring(14,15);
                //parses string to find x value of clicked class
                int pos1 = Integer.parseInt(taskSubstring);

                //uses pos1 to find the class name and elapsed time of the selected task
                String task = xData[pos1];
                String hours = yData[pos1];
                //display toast to user
                Toast.makeText(ClassData.this, "Task: " + task + "\n" + "Hours: " + hours, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void addDataSet(PieChart pieChart) {

        //instantiates array of PieEntry. pie entry holds a string (task name) and float (elapsed time)
        ArrayList<PieEntry> Entrys = new ArrayList<>();

        //Error check to see if there is any data
        if (xData == null || yData == null)
        {
            Entrys.add(new PieEntry(0));
        }
        else {

            for (int i = 0; i < yData.length; i++) {
                //dont include tasks that have no data
                if ((Float.parseFloat(yData[i]) > 0))
                    Entrys.add(new PieEntry(Float.parseFloat(yData[i]), xData[i]));
                else{
                    //do nothing
                }
            }
        }


        //Create data set
        PieDataSet pieDataSet = new PieDataSet(Entrys, "Class Hours");

        //pie chart formatting
        pieDataSet.setSliceSpace(0);
        pieDataSet.setValueTextSize(10);
        pieDataSet.setValueFormatter(new DefaultValueFormatter(3));
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);

        //create pie chart
        pieChart.setData(pieData);
        pieChart.setDescription("");
        pieChart.invalidate();

    }


    public void getTaskNamesData(){

        //class getAllClasses from TaskDBHelper
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME,
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME,
                TimerContract.TimerEntry.COLUMN_PREDICTED_TIME};

        //look in the following rows (for next action)
        String selection = TimerContract.TimerEntry.COLUMN_CLASS_NAME + " = ? and "
                + TimerContract.TimerEntry.COLUMN_TASK_NAME + " != ?";

        String[] selectionArgs = {className, "CLASS"};

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


        //moves to first row
        cursor.moveToFirst();

        //column to index through
        int taskNamesColumnIndex = cursor.getColumnIndex(TimerContract.TimerEntry.COLUMN_TASK_NAME);

        //initialize array list to hold class names after iteration through cursor
        ArrayList<String> xDataList = new ArrayList<>();

        //iterate through cursor and add class names to arraylist
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String taskNames = cursor.getString(taskNamesColumnIndex);
            xDataList.add(taskNames);
        }

        //convert the arraylist into a string array for piechart usage
        xData = xDataList.toArray(new String[xDataList.size()]);
    }


    public void getElapsedTimeData(){

        //class getAllClasses from TaskDBHelper
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                TimerContract.TimerEntry._ID,
                TimerContract.TimerEntry.COLUMN_CLASS_NAME,
                TimerContract.TimerEntry.COLUMN_TASK_NAME,
                TimerContract.TimerEntry.COLUMN_START_TIME,
                TimerContract.TimerEntry.COLUMN_ELAPSED_TIME,
                TimerContract.TimerEntry.COLUMN_PREDICTED_TIME,
                TimerContract.TimerEntry.COLUMN_ACTIVE};

        //look in the following rows (for next action)
        String selection = TimerContract.TimerEntry.COLUMN_TASK_NAME + " = ? ";

        //Arraylist to hold results from query to get data
        ArrayList<String> yDataList = new ArrayList<>();


        if (xData != null) {
            for (int i = 0; i < xData.length; i++) {

                //check above rows to see if they have the following criteria
                String[] selectionArgs = {xData[i]};

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

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Float currentElapsedTime = cursor.getFloat(elapsedTimeColumnIndex);
                    elapsedTime += currentElapsedTime;
                }

                //convert into hours and
                elapsedTime /= 3600;
                String elapsedTimeTruncated = String.format("%.3f", elapsedTime);
                yDataList.add(elapsedTimeTruncated);

            }
            //convert arraylist into string[] for pie chart use
            yData = yDataList.toArray(new String[yDataList.size()]);
        }
    }
}

