package com.dontstopthemusic.dontstopthemusic;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void mainActivityLaunchesThirdFragment() {
        onView(withId(R.id.button_debug_silence)).check(matches(isDisplayed()));
    }

    @Test
    public void mainActivityDisplaysGraphs() {
        onView(withId(R.id.chart_stereo)).check(matches(isDisplayed()));
        onView(withId(R.id.chart_monitor)).check(matches(isDisplayed()));
    }

    @Test
    public void silenceButtonTakesUserToSecondFragment() {
        onView(withId(R.id.button_debug_silence)).perform(click());
        onView(withId(R.id.textview_second)).check(matches(isDisplayed()));
    }

    @Test
    public void feedbackButtonTakesUserToFirstFragment() {
        onView(withId(R.id.button_debug_feedback)).perform(click());
        onView(withId(R.id.textview_first)).check(matches(isDisplayed()));
    }

    @Test
    public void hissButtonTakesUserToFourthFragment() {
        onView(withId(R.id.button_debug_hiss)).perform(click());
        onView(withId(R.id.textview_fourth)).check(matches(isDisplayed()));
    }


}