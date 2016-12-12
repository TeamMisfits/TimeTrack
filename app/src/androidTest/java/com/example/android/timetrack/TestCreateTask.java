package com.example.android.timetrack;

import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.hamcrest.Matchers;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.example.android.timetrack.CreateClass;
import com.example.android.timetrack.R;
import com.example.android.timetrack.data.TimerContract;

import java.util.Map;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestCreateTask {
    private String mStringToBetyped;

    @Rule
    public ActivityTestRule<CreateTask> mActivityRule = new ActivityTestRule<>(CreateTask.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        mStringToBetyped = "PA4";
    }

    @Test
    public void changeText_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.getTaskName))
                .perform(typeText(mStringToBetyped), closeSoftKeyboard());
        onView(withId(R.id.getTaskButton)).perform(click());

        // Check that the text was changed.
        onData(allOf(is(instanceOf(Map.class)),
                hasEntry(equalTo(TimerContract.TimerEntry.COLUMN_TASK_NAME), is(mStringToBetyped))));
    }
}
