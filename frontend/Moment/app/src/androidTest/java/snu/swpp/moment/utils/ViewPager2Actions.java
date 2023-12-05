package snu.swpp.moment.utils;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.view.View;
import androidx.annotation.Nullable;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.viewpager2.widget.ViewPager2;
import org.hamcrest.Matcher;

public class ViewPager2Actions {
    private static final boolean DEFAULT_SMOOTH_SCROLL = false;

    private ViewPager2Actions() {
        // forbid instantiation
    }

    /**
     * Moves {@link ViewPager2} to the right by one page.
     */
    public static ViewAction scrollRight() {
        return scrollRight(DEFAULT_SMOOTH_SCROLL);
    }

    /*
     * Moves {@link ViewPager2} to the right by one page.
     */
    public static ViewAction scrollRight(final boolean smoothScroll) {
        return new ViewPagerScrollAction() {
            @Override
            public String getDescription() {
                return "ViewPager2 move one page to the right";
            }

            @Override
            protected void performScroll(ViewPager2 viewPager) {
                int current = viewPager.getCurrentItem();
                viewPager.setCurrentItem(current + 1, smoothScroll);
            }
        };
    }

    public static ViewAction scrollLeft() {
        return scrollLeft(DEFAULT_SMOOTH_SCROLL);
    }

    /**
     * Moves {@link ViewPager2} to the left by one page.
     */
    public static ViewAction scrollLeft(final boolean smoothScroll) {
        return new ViewPagerScrollAction() {
            @Override
            public String getDescription() {
                return "ViewPager2 move one page to the left";
            }

            @Override
            protected void performScroll(ViewPager2 viewPager) {
                int current = viewPager.getCurrentItem();
                viewPager.setCurrentItem(current - 1, smoothScroll);
            }
        };
    }

    private static final class CustomViewPager2Listener extends ViewPager2.OnPageChangeCallback
        implements IdlingResource {
        private int mCurrState = ViewPager2.SCROLL_STATE_IDLE;

        @Nullable
        private IdlingResource.ResourceCallback mCallback;

        private boolean mNeedsIdle = false;

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            mCallback = resourceCallback;
        }

        @Override
        public String getName() {
            return "ViewPager2 listener";
        }

        @Override
        public boolean isIdleNow() {
            if (!mNeedsIdle) {
                return true;
            } else {
                return mCurrState == ViewPager2.SCROLL_STATE_IDLE;
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mCurrState == ViewPager2.SCROLL_STATE_IDLE) {
                if (mCallback != null) {
                    mCallback.onTransitionToIdle();
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mCurrState = state;
            if (mCurrState == ViewPager2.SCROLL_STATE_IDLE) {
                if (mCallback != null) {
                    mCallback.onTransitionToIdle();
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    }

    private abstract static class ViewPagerScrollAction implements ViewAction {

        @Override
        public final Matcher<View> getConstraints() {
            return isDisplayed();
        }

        @Override
        public final void perform(UiController uiController, View view) {
            final ViewPager2 viewPager = (ViewPager2) view;

            // Add a custom tracker listener
            final CustomViewPager2Listener customListener = new CustomViewPager2Listener();
            viewPager.registerOnPageChangeCallback(customListener);

            // Note that we're running the following block in a try-finally construct.
            // This is needed since some of the actions are going to throw (expected) exceptions.
            // If that happens, we still need to clean up after ourselves
            // to leave the system (Espresso) in a good state.
            try {
                // Register our listener as idling resource so that Espresso waits until the
                // wrapped action results in the ViewPager2 getting to the SCROLL_STATE_IDLE state
                IdlingRegistry.getInstance().register(customListener);

                uiController.loopMainThreadUntilIdle();

                performScroll((ViewPager2) view);

                uiController.loopMainThreadUntilIdle();

                customListener.mNeedsIdle = true;
                uiController.loopMainThreadUntilIdle();
                customListener.mNeedsIdle = false;
            } finally {
                // Unregister our idling resource
                IdlingRegistry.getInstance().unregister(customListener);
                // And remove our tracker listener from ViewPager2
                viewPager.unregisterOnPageChangeCallback(customListener);
            }
        }

        protected abstract void performScroll(ViewPager2 viewPager);
    }
}
