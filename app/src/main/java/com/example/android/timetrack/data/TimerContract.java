package com.example.android.timetrack.data;

/**
 * Created by Owner on 12/11/2016.
 */

import android.provider.BaseColumns;


public final class TimerContract {

    //private constructor for timer contract to prevent user instantiation
    private TimerContract(){}

    //Inner class within TimerContract. Defines constants for data base table
    public static final class TimerEntry implements BaseColumns{

        //name of the data base
        public static final String TABLE_NAME = "tasks";

        //unique ID number for each task

        public static final String _ID = BaseColumns._ID;

        //name of the class

        public static final String COLUMN_CLASS_NAME = "className";

        //name of the task

        public static final String COLUMN_TASK_NAME = "taskName";

        //start time for the task

        public static final String COLUMN_START_TIME = "startTime";

        //end time for the task

        public static final String COLUMN_ELAPSED_TIME = "totalTime";

        public static final String COLUMN_PREDICTED_TIME = "predictedTime";

        public static final String COLUMN_ACTIVE = "active";
    }
}
