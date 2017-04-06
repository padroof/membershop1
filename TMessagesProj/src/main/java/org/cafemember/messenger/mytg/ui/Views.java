package org.cafemember.messenger.mytg.ui;

import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.cafemember.messenger.R;
import org.cafemember.messenger.mytg.Commands;
import org.cafemember.messenger.mytg.FontManager;
import org.cafemember.messenger.mytg.adapter.ViewPagerAdapter;
import org.cafemember.messenger.mytg.fragments.AddChannelActivity;
import org.cafemember.messenger.mytg.listeners.OnJoinSuccess;
import org.cafemember.messenger.mytg.listeners.Refrashable;
import org.cafemember.ui.DialogsActivity;
import org.cafemember.ui.ManageSpaceActivity;

/**
 * Created by Masoud on 6/2/2016.
 */
public class Views {

    private static View alertView;
    private static View defaultView;

    /*private final static String alert = "با توجه به محدودیت\u200Cهای تلگرام بهتر است ابتدا کانال خود را ثبت نموده سپس نسبت به عضویت در کانال\u200C" +
            "ها اقدام نمایید تا در زمان محدود شدن مشکلی بابت سفارش عضو نداشته باشید" +
            "";*/

    public static ViewPagerAdapter viewPagerAdapter;
    public static View getTabLayout(final FragmentActivity context, DialogsActivity dialogsActivity, View dialogsLayout){

        if(Commands.wait4Ans){
            return getDefaultLayout(context, dialogsActivity, dialogsLayout);
        }
        return getDefaultLayout(context, dialogsActivity, dialogsLayout);

    }

    private static View getAlertLayout(final FragmentActivity context,final DialogsActivity dialogsActivity){
        if(alertView != null){
            return alertView;
        }
        View lay = context.getLayoutInflater().inflate(R.layout.alert_layout, null);
        //lay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));*/
        lay.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Commands.enter2Ad = true;
                AddChannelActivity addChannelActivity = new AddChannelActivity();
                /*addChannelActivity.setChannelAddListener(new OnJoinSuccess() {
                    @Override
                    public void OnResponse(boolean ok) {
                    }
                });*/
                dialogsActivity.presentFragment(addChannelActivity);
            }
        });
        lay.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commands.wait4Ans = false;
                dialogsActivity.afterWait();
            }
        });
//        ((TextView)lay.findViewById(R.id.alert)).setText(alert);
        alertView = lay;
        FontManager.instance().setTypefaceImmediate(alertView);
        return lay;

    }

    private static View getDefaultLayout(final FragmentActivity context, final DialogsActivity dialogsActivity, View dialogsLayout){

        /*if(defaultView != null){
            return defaultView;
        }*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Commands.checkChannelsTrigger(null, dialogsActivity);

            }
        },3000);
        View lay = context.getLayoutInflater().inflate(R.layout.main_layout, null);
        //lay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));*/

        ManageSpaceActivity.tabLayout = (TabLayout)lay.findViewById(R.id.tabs);
        ManageSpaceActivity.viewPager = (MyViewPager)lay.findViewById(R.id.viewpager);

        viewPagerAdapter = new ViewPagerAdapter(context.getSupportFragmentManager(), dialogsActivity, dialogsLayout);
         ManageSpaceActivity.viewPager.setAdapter(viewPagerAdapter);


         ManageSpaceActivity.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                /*Fragment page = context.getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
                if(page != null && page instanceof Refrashable){
                    ((Refrashable)page).refresh();
                }*/
                Refrashable refrashable = (Refrashable) viewPagerAdapter.getItem(position);
                refrashable.refresh();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

//        viewPager.setCurrentItem(1);
//        final TabLayout.Tab telegram = tabLayout.newTab();
        final TabLayout.Tab myChannels = ManageSpaceActivity.tabLayout.newTab();
        final TabLayout.Tab channels = ManageSpaceActivity.tabLayout.newTab();
        final TabLayout.Tab shop = ManageSpaceActivity.tabLayout.newTab();
//        final TabLayout.Tab viewCoins = tabLayout.newTab();


//        telegram.setIcon(R.drawable.ic_message);
//        channels.setText("خرید سکه");
        View tabOne =  LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        TextView text = (TextView)tabOne.findViewById(R.id.text);
        ImageView icon = (ImageView)tabOne.findViewById(R.id.icon);
        text.setText("خرید سکه");
        icon.setImageResource(R.drawable.buy_coin);
        shop.setCustomView(tabOne);

//        posts.setText("سکه رایگان");
//        joinCoins.setText("سفارش سکه");

        View tabTwo =  LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        text = (TextView)tabTwo.findViewById(R.id.text);
        icon = (ImageView)tabTwo.findViewById(R.id.icon);
        text.setText("سکه رایگان");
        icon.setImageResource(R.drawable.free_coin);
        channels.setCustomView(tabTwo);

        View tabThree =  LayoutInflater.from(context).inflate(R.layout.custom_tab, null);

        text = (TextView)tabThree.findViewById(R.id.text);
        icon = (ImageView)tabThree.findViewById(R.id.icon);
        text.setText("درخواست ممبر");
        icon.setImageResource(R.drawable.member);

        myChannels.setCustomView(tabThree);

        /*channels.setIcon(R.drawable.free_coin);
        posts.setIcon(R.drawable.member);

        joinCoins.setIcon(R.drawable.buy_coin);*/
//        viewCoins.setIcon(R.drawable.ic_coin_eye);



//        tabLayout.addTab(telegram, 0);
        ManageSpaceActivity.tabLayout.addTab(shop, 0);
        ManageSpaceActivity.tabLayout.addTab(channels, 1);
        ManageSpaceActivity.tabLayout.addTab(myChannels, 2);
        ManageSpaceActivity.viewPager.setCurrentItem(1);
        TabLayout.Tab tab = ManageSpaceActivity.tabLayout.getTabAt(1);
        tab.select();
//        tabLayout.addTab(viewCoins, 4);
//        tabLayout.setTabMode(ViewGroup.);
        ManageSpaceActivity.tabLayout.setTabTextColors(ContextCompat.getColorStateList(context, R.color.abc_primary_text_material_dark));

        FontManager.instance().setTypefaceImmediate(ManageSpaceActivity.tabLayout);

         ManageSpaceActivity.viewPager.setOffscreenPageLimit(3);
         ManageSpaceActivity.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(ManageSpaceActivity.tabLayout));
        ManageSpaceActivity.tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                 ManageSpaceActivity.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        defaultView = lay;
        return defaultView;

    }

    public static void refreshView(){
        viewPagerAdapter.refreshView();
    }

}
