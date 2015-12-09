package co.optonaut.optonaut.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class MainActivity extends AppCompatActivity {
    private final String FEED_FRAGMENT_TAG = "FEED_FRAGMENT";

    private TabsAdapter tabsAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        tabsAdapter = new TabsAdapter(getSupportFragmentManager(), this, viewPager, tabLayout);
    }

    public void openOptograph2DView(Optograph optograph) {
        Optograph2DFragment optograph2DFragment = new Optograph2DFragment();
        Bundle args = new Bundle();
        args.putParcelable("optograph", optograph);
        optograph2DFragment.setArguments(args);

        //getSupportFragmentManager().beginTransaction().
        //        replace(R.id.fragment_placeholder, optograph2DFragment).addToBackStack(null).commit();
    }

    public void openProfileFragment(Person person) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFragment.setArguments(args);

        //getSupportFragmentManager().beginTransaction().
        //        replace(R.id.fragment_placeholder, profileFragment).addToBackStack(null).commit();
    }

    // source: http://stackoverflow.com/a/17832632/1176596
    public class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, TabLayout.OnTabSelectedListener {
        private final String[] tabTitles = {"Feed", "Search", "Notification", "Profile"};

        private ViewPager viewPager;
        private TabLayout tabLayout;
        private Context context;
        private FragmentManager fm;

        private List<TabInfo> tabs = new LinkedList<TabInfo>();
        private int TOTAL_TABS;

        private Map<Integer, Stack<TabInfo>> history = new HashMap<>();

        public TabsAdapter(FragmentManager fm, Context context, ViewPager viewPager, TabLayout tabLayout) {
            super(fm);
            this.tabLayout = tabLayout;
            this.fm = fm;
            this.context = context;
            this.viewPager = viewPager;

            initializeTabs();
        }

        private void initializeTabs() {
            for (int i = 0; i < 4; i++) {
                addTab(null, FeedFragment.class, new Bundle(), tabTitles[i]);
            }

            viewPager.setAdapter(this);
            tabLayout.setupWithViewPager(viewPager);

            Drawable feedIcon = getResources().getDrawable(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            Drawable searchIcon = getResources().getDrawable(R.drawable.abc_ic_search_api_mtrl_alpha);
            Drawable notificationIcon = getResources().getDrawable(R.drawable.abc_btn_check_to_on_mtrl_015);
            Drawable profileIcon = getResources().getDrawable(R.drawable.abc_ic_clear_mtrl_alpha);

            Drawable[] images = {feedIcon, searchIcon, notificationIcon, profileIcon};

            // For some reason, we have to update images afterwards
            for (int i = 0; i < 4; i++) {
                tabLayout.getTabAt(i).setIcon(images[i]);
            }

        }

        public void addTab(final Drawable image, final Class fragmentClass, final Bundle args, final String title) {
            final TabInfo tabInfo = new TabInfo(fragmentClass, args, title);
            final TabLayout.Tab tab = tabLayout.newTab();
            tab.setTag(tabInfo);
            if (image != null) {
                tab.setIcon(image);
            }

            tabs.add(tabInfo);
            tabLayout.addTab(tab);

            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(final int position) {
            final TabInfo tabInfo = tabs.get(position);
            return Fragment.instantiate(context, tabInfo.fragmentClass.getName(), tabInfo.args);
        }

        @Override
        public int getItemPosition(final Object object) {
            int position = viewPager.getCurrentItem();

            int pos = POSITION_NONE;
            if (history.get(position).isEmpty()) {
                return POSITION_NONE;
            }

            for (Stack<TabInfo> stack : history.values()) {
                TabInfo c = stack.peek();
                if (c.fragmentClass.getName().equals(object.getClass().getName())) {
                    pos = POSITION_UNCHANGED;
                    break;
                }
            }
            return pos;
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            viewPager.setCurrentItem(position);
        }

        public void replace(final int position, final Class fragmentClass, final Bundle args, final String title) {
            fm.beginTransaction().addToBackStack(null).commit();
            updateTabs(new TabInfo(fragmentClass, args, title), position);
            history.get(position).push(new TabInfo(tabs.get(position).fragmentClass, tabs.get(position).args, tabs.get(position).title));
            notifyDataSetChanged();
        }

        private void updateTabs(final TabInfo tabInfo, final int position) {
            tabs.remove(position);
            tabs.add(position, tabInfo);
            tabLayout.getTabAt(position).setTag(tabInfo);
        }

        public void createHistory() {
            int position = 0;
            TOTAL_TABS = tabs.size();
            for (TabInfo tab : tabs) {
                if (history.get(position) == null) {
                    history.put(position, new Stack<TabInfo>());
                }
                history.get(position).push(new TabInfo(tab.fragmentClass, tab.args, tab.title));
                position++;
            }
        }

        public void back() {
            int position = viewPager.getCurrentItem();
            if (!historyIsEmpty(position)) {
                if (isLastItemInHistory(position)) {
                    finish();
                }
                final TabInfo currentTabInfo = getPrevious(position);
                tabs.clear();
                for (int i = 0; i < TOTAL_TABS; i++) {
                    if (i == position) {
                        tabs.add(new TabInfo(currentTabInfo.fragmentClass, currentTabInfo.args, currentTabInfo.title));
                    } else {
                        TabInfo otherTabInfo = history.get(i).peek();
                        tabs.add(new TabInfo(otherTabInfo.fragmentClass, otherTabInfo.args, otherTabInfo.title));
                    }
                }
            }
            viewPager.setCurrentItem(position);
            notifyDataSetChanged();
        }

        private boolean historyIsEmpty(final int position) {
            return history == null || history.isEmpty() || history.get(position).isEmpty();
        }

        private boolean isLastItemInHistory(final int position) {
            return history.get(position).size() == 1;
        }

        private TabInfo getPrevious(final int position) {
            TabInfo currentTabInfo = history.get(position).pop();
            if (!history.get(position).isEmpty()) {
                currentTabInfo = history.get(position).peek();
            }
            return currentTabInfo;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            TabInfo tabInfo = (TabInfo) tab.getTag();
            for (int i = 0; i < tabs.size(); i++) {
                if (tabs.get(i).equals(tabInfo)) {
                    viewPager.setCurrentItem(i);
                }
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    private static class TabInfo {
        public Class fragmentClass;
        public Bundle args;
        public String title;
        public TabInfo(Class fragmentClass, Bundle args, String title) {
            this.fragmentClass = fragmentClass;
            this.args = args;
            this.title = title;
        }

        @Override
        public boolean equals(final Object o) {
            return this.fragmentClass.getName().equals(o.getClass().getName());
        }

        @Override
        public int hashCode() {
            return fragmentClass.getName() != null ? fragmentClass.getName().hashCode() : 0;
        }

        @Override
        public String toString() {
            return "TabInfo{" +
                    "fragmentClass=" + fragmentClass +
                    '}';
        }
    }
}
