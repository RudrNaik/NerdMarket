package com.example.androidexample;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

import android.content.Intent;


@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class RudraSystemTest {
    private static final int ID = 2;
    private static final String USERNAME = "rudranai";
    private static final int SIMULATED_DELAY_MS = 3345;

    private static Intent sessionIntent(Class<?> enteredClass) {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), enteredClass);
        i.putExtra("id", ID);
        i.putExtra("username", USERNAME);
        i.putExtra("isAdmin", true);
        return i;
    }

    @Test
    public void testSearchCardAndAddToBinder() throws InterruptedException {
        try (ActivityScenario<CardSearchActivity> scenario = ActivityScenario.launch(sessionIntent(CardSearchActivity.class))) {

            onView(withId(R.id.card_search_field)).perform(typeText("A Case For K9"), closeSoftKeyboard());
            onView(withId(R.id.card_search_btn)).perform(click());
            Thread.sleep(SIMULATED_DELAY_MS);

            onView(withId(R.id.card_view)).check(matches(isDisplayed()));
            onView(withId(R.id.card_name)).check(matches(isDisplayed()));

            onView(withId(R.id.card_addTobinder_btn)).perform(click());
            Thread.sleep(SIMULATED_DELAY_MS);
        }


        try (ActivityScenario<CardBinderActivity> binderScenario = ActivityScenario.launch(sessionIntent(CardBinderActivity.class))) {
            Thread.sleep(SIMULATED_DELAY_MS);

            onView(withText(containsString("A Case for K9"))).perform(scrollTo()).check(matches(isDisplayed()));

        }
    }

    @Test
    public void testAdminActivateDeactivate() throws InterruptedException {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(sessionIntent(AdminActivity.class))) {


            onView(withId(R.id.admin_activate_deactivate_btn)).perform(scrollTo(), click());
            Thread.sleep(1000);
            onView(withId(R.id.admin_activate_deactivate_container)).check(matches(isDisplayed()));

            onView(withId(R.id.admin_activate_deactivate_search_field)).perform(scrollTo(), replaceText("12"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.admin_activate_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );

            onView(withId(R.id.admin_promote_demote_btn)).perform(scrollTo(), click());
            Thread.sleep(1000);
            onView(withId(R.id.admin_promote_demote_container)).check(matches(isDisplayed()));

            onView(withId(R.id.admin_promote_demote_field)).perform(scrollTo(), replaceText("12"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.admin_demote_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );

            onView(withId(R.id.admin_activate_deactivate_search_field)).perform(scrollTo(), replaceText("12"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.admin_deactivate_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );

            onView(withId(R.id.admin_showAllAccounts_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );
            onView(withId(R.id.admin_account_cardDetail_name)).check(matches(withText(containsString("ID: 12  //  FakeUser!!  //  Active: false  //  Admin: false"))));

            onView(withId(R.id.admin_activate_deactivate_search_field)).perform(scrollTo(), replaceText("12"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.admin_activate_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );

            onView(withId(R.id.admin_showAllAccounts_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );
            onView(withId(R.id.admin_account_cardDetail_name)).check(matches(withText(containsString("ID: 12  //  FakeUser!!  //  Active: true  //  Admin: false"))));
        }
    }

    @Test
    public void testAdminPromoteDemote() throws InterruptedException {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(sessionIntent(AdminActivity.class))) {

            onView(withId(R.id.admin_activate_deactivate_btn)).perform(scrollTo(), click());
            Thread.sleep(1000);
            onView(withId(R.id.admin_activate_deactivate_container)).check(matches(isDisplayed()));

            onView(withId(R.id.admin_activate_deactivate_search_field)).perform(scrollTo(), replaceText("12"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.admin_activate_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS);

            onView(withId(R.id.admin_promote_demote_btn)).perform(scrollTo(), click());
            Thread.sleep(1000);
            onView(withId(R.id.admin_promote_demote_container)).check(matches(isDisplayed()));

            onView(withId(R.id.admin_promote_demote_field)).perform(scrollTo(), replaceText("12"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.admin_demote_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );

            onView(withId(R.id.admin_promote_demote_field)).perform(scrollTo(), replaceText("12"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.admin_promote_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );

            onView(withId(R.id.admin_showAllAccounts_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );
            onView(withId(R.id.admin_account_cardDetail_name)).check(matches(withText(containsString("ID: 12  //  FakeUser!!  //  Active: true  //  Admin: true"))));

            onView(withId(R.id.admin_promote_demote_field)).perform(scrollTo(), replaceText("12"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.admin_demote_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS );

            onView(withId(R.id.admin_showAllAccounts_btn)).perform(scrollTo(), click());
            Thread.sleep(SIMULATED_DELAY_MS);
            onView(withId(R.id.admin_account_cardDetail_name)).check(matches(withText(containsString("ID: 12  //  FakeUser!!  //  Active: true  //  Admin: false"))));
        }
    }

    @Test
    public void testToggleVisibility() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(sessionIntent(AdminActivity.class))) {

            onView(withId(R.id.admin_activate_deactivate_btn)).perform(scrollTo(), click());

            onView(withId(R.id.admin_activate_deactivate_container)).check(matches(isDisplayed()));

            onView(withId(R.id.admin_activate_deactivate_btn)).perform(scrollTo(), click());

            onView(withId(R.id.admin_activate_deactivate_container)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void testEmptyNotif() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(sessionIntent(AdminActivity.class))) {

            onView(withId(R.id.admin_create_notif_btn)).perform(scrollTo(), click());

            onView(withId(R.id.admin_send_notif_btn)).perform(scrollTo(), click());
        }
    }

    @Test
    public void testSendNotif() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(sessionIntent(AdminActivity.class))) {

            onView(withId(R.id.admin_create_notif_btn)).perform(scrollTo(), click());

            onView(withId(R.id.admin_notif_title_field)).perform(replaceText("Test"), closeSoftKeyboard());

            onView(withId(R.id.admin_notif_message_field)).perform(replaceText("Hello"), closeSoftKeyboard());

            onView(withId(R.id.admin_send_notif_btn)).perform(scrollTo(), click());
        }
    }

    @Test
    public void testToMain() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(sessionIntent(AdminActivity.class))) {

            onView(withId(R.id.admin_to_main_btn)).perform(click());
        }
    }

    @Test
    public void testToPriceCrud() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(sessionIntent(AdminActivity.class))) {

            onView(withId(R.id.admin_to_priceCrud_btn)).perform(click());
        }
    }

    @Test
    public void testUnlockAccountFlow() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(sessionIntent(AdminActivity.class))) {

            onView(withId(R.id.admin_unlock_btn))
                    .perform(scrollTo(), click());

            onView(withId(R.id.admin_unlock_ID_searchField))
                    .perform(replaceText("12"), closeSoftKeyboard());

            onView(withId(R.id.admin_unlockAccount_btn))
                    .perform(scrollTo(), click());
        }
    }

    @Test
    public void testBadSession() {
        Intent badIntent = new Intent(ApplicationProvider.getApplicationContext(), AdminActivity.class
        );

        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(badIntent)) {

            onView(withId(R.id.admin_showAllAccounts_btn))
                    .perform(scrollTo(), click());
        }
    }

    @Test
    public void testBinderNav() {
        try (ActivityScenario<CardBinderActivity> scenario = ActivityScenario.launch(sessionIntent(CardBinderActivity.class))) {

            onView(withId(R.id.cardBinder_home_image)).perform(click());
            onView(withId(R.id.main_toSearch_image)).perform(click());
            onView(withId(R.id.cardlookup_to_main_button)).perform(click());
        }
    }

    @Test
    public void testCardDisplaysInBinder() throws InterruptedException {
        try (ActivityScenario<CardBinderActivity> scenario = ActivityScenario.launch(sessionIntent(CardBinderActivity.class))) {

            Thread.sleep(SIMULATED_DELAY_MS);

            onView(allOf(withId(R.id.card_view), isDisplayed())).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testChartVisibility() throws InterruptedException {
        try (ActivityScenario<CardBinderActivity> scenario = ActivityScenario.launch(sessionIntent(CardBinderActivity.class))) {

            Thread.sleep(SIMULATED_DELAY_MS);

            onView(allOf(withId(R.id.candleStick), isDisplayed())).check(matches(isDisplayed()));
        }
    }


    @Test
    public void testSearchNav() {
        try (ActivityScenario<CardBinderActivity> scenario = ActivityScenario.launch(sessionIntent(CardSearchActivity.class))) {

            onView(withId(R.id.cardlookup_to_main_button)).perform(click());
            onView(withId(R.id.main_toPortfolio_image)).perform(click());
            onView(withId(R.id.cardBinder_toSearch_image)).perform(click());
        }
    }

    @Test
    public void testCardDisplaysInSearch() throws InterruptedException {
        try (ActivityScenario<CardBinderActivity> scenario = ActivityScenario.launch(sessionIntent(CardSearchActivity.class))) {

            onView(withId(R.id.card_search_field)).perform(typeText("Raichu"), closeSoftKeyboard());
            onView(withId(R.id.card_search_btn)).perform(click());
            Thread.sleep(SIMULATED_DELAY_MS);

            onView(allOf(withId(R.id.card_view), isDisplayed())).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testCardSearchVisibility() throws InterruptedException {
        try (ActivityScenario<CardBinderActivity> scenario = ActivityScenario.launch(sessionIntent(CardSearchActivity.class))) {

            onView(withId(R.id.card_search_field)).perform(typeText("Raichu"), closeSoftKeyboard());
            onView(withId(R.id.card_search_btn)).perform(click());
            Thread.sleep(SIMULATED_DELAY_MS);

            onView(allOf(withId(R.id.candleStick), isDisplayed())).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testCardSearchEnableEditMode() {
        try (ActivityScenario<CardBinderActivity> scenario = ActivityScenario.launch(sessionIntent(CardSearchActivity.class))) {

            onView(withId(R.id.card_search_field)).perform(typeText("A Case for K9"), closeSoftKeyboard());
            onView(withId(R.id.card_search_btn)).perform(click());
            Thread.sleep(SIMULATED_DELAY_MS);

            onView(withId(R.id.card_view)).perform(scrollTo());
            onView(withId(R.id.card_edit_btn)).perform(click());

            onView(withId(R.id.card_name_edit)).check(matches(isDisplayed()));
            onView(withId(R.id.card_save_btn)).check(matches(isDisplayed()));

            Thread.sleep(SIMULATED_DELAY_MS);

            onView(withId(R.id.card_save_btn)).perform(scrollTo(),click());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCardSearchCameraSearchNav() {
        try (ActivityScenario<CardBinderActivity> scenario = ActivityScenario.launch(sessionIntent(CardSearchActivity.class))) {
            onView(withId(R.id.Search_camera_btn)).perform(click());
        }
    }










}
