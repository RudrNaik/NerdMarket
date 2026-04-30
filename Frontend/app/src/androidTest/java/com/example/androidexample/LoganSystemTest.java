package com.example.androidexample;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;

import android.content.Intent;
import android.widget.EditText;

import java.util.List;


@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class LoganSystemTest {
    private static final int SIMULATED_DELAY_MS = 2345;
    static int deleteAccountId = -1;

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private static Intent sessionIntent(Class enteredClass) {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), enteredClass);
        i.putExtra("id", 4);
        i.putExtra("username", "logheil");
        i.putExtra("isAdmin", true);
        return i;
    }

    @Test
    public void test1_LaunchInitialActivityAndSignupAndDeleteAccount() throws InterruptedException {
        ActivityScenario.launch(InitialActivity.class);
        Thread.sleep(SIMULATED_DELAY_MS * 3);

        onView(withId(R.id.login_signup_btn)).perform(click());
        Thread.sleep(SIMULATED_DELAY_MS);

        onView(withId(R.id.signup_firstname)).perform(typeText("DELETEMEPLZ!"), closeSoftKeyboard());
        onView(withId(R.id.signup_lastname)).perform(typeText("DELETEMEPLZ!"), closeSoftKeyboard());
        onView(withId(R.id.signup_email)).perform(typeText("email@email.email"), closeSoftKeyboard());
        onView(withId(R.id.signup_username)).perform(typeText("DELETEMEPLZ!"), closeSoftKeyboard());
        onView(withId(R.id.signup_password)).perform(typeText("DELETEMEPLZ!"), closeSoftKeyboard());
        onView(withId(R.id.create_account_btn)).perform(click());
        Thread.sleep(SIMULATED_DELAY_MS);

        onView(withId(R.id.login_username)).perform(typeText("DELETEMEPLZ!"), closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("DELETEMEPLZ!"), closeSoftKeyboard());
        Intents.init();
        onView(withId(R.id.login_go_btn)).perform(click());
        Thread.sleep(SIMULATED_DELAY_MS);

        List<Intent> intents = Intents.getIntents();
        deleteAccountId = intents.get(intents.size() - 1).getIntExtra("id", -1);
        Intents.release();

        ActivityScenario<UserActivity> regularScenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), UserActivity.class)
                        .putExtra("id", deleteAccountId)
                        .putExtra("username", "DELETEMEPLZ!")
                        .putExtra("isAdmin", false)
        );

        onView(withId(R.id.userPage_delete_account_btn)).perform(click());
        onView(withClassName(Matchers.equalTo(EditText.class.getName())))
                .perform(typeText("DELETEMEPLZ!"), closeSoftKeyboard());
        onView(withText("Delete")).perform(click());
        Thread.sleep(SIMULATED_DELAY_MS);
    }

    @Test
    public void testLoginNavigationAndIntent() {
        Intents.init();
        onView(withId(R.id.login_username)).perform(typeText("logheil"), closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("AnwarTheGOAT#1"), closeSoftKeyboard());
        onView(withId(R.id.login_go_btn)).perform(click());
        try { Thread.sleep(SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        intended(allOf(
                hasComponent(MainActivity.class.getName()),
                hasExtra("username", "logheil"),
                hasExtra("isAdmin", true),
                hasExtra("id", 4)
        ));
        Intents.release();
    }

    @Test
    public void testNavigateToNotifications() {
        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(sessionIntent(UserActivity.class))) {
            onView(withId(R.id.userPage_to_notifs_btn)).perform(click());
            onView(withId(R.id.notification_unread_count)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testNavigateToCardBinder() {
        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(sessionIntent(UserActivity.class))) {
            onView(withId(R.id.userPage_toPortfolio_image)).perform(click());
            onView(withId(R.id.cardBinder_toSearch_image)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testNavigateToCardSearch() {
        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(sessionIntent(UserActivity.class))) {
            onView(withId(R.id.userPage_toSearch_image)).perform(click());
            onView(withId(R.id.cardLookup_toPortfolio_image)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testNavigateToHome() {
        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(sessionIntent(UserActivity.class))) {
            onView(withId(R.id.userPage_home_image)).perform(click());
            onView(withId(R.id.main_toSearch_image)).check(matches(isDisplayed()));
        }
    }

}
