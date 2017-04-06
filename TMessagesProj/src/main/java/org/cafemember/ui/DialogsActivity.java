/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.cafemember.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Keep;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.cafemember.messenger.ApplicationLoader;
import org.cafemember.messenger.MediaController;
import org.cafemember.messenger.mytg.FontManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.cafemember.messenger.AndroidUtilities;
import org.cafemember.messenger.BuildVars;
import org.cafemember.messenger.ChatObject;
import org.cafemember.messenger.ImageLoader;
import org.cafemember.messenger.LocaleController;
import org.cafemember.messenger.MessageObject;
import org.cafemember.messenger.UserObject;
import org.cafemember.messenger.mytg.Commands;
import org.cafemember.messenger.mytg.adapter.ReserveAdapter;
import org.cafemember.messenger.mytg.listeners.OnCoinsReady;
import org.cafemember.messenger.mytg.listeners.OnResponseReadyListener;
import org.cafemember.messenger.mytg.ui.Views;
import org.cafemember.messenger.mytg.util.Defaults;
import org.cafemember.messenger.support.widget.LinearLayoutManager;
import org.cafemember.messenger.support.widget.RecyclerView;
import org.cafemember.messenger.FileLog;
import org.cafemember.tgnet.TLRPC;
import org.cafemember.messenger.ContactsController;
import org.cafemember.messenger.MessagesController;
import org.cafemember.messenger.MessagesStorage;
import org.cafemember.messenger.NotificationCenter;
import org.cafemember.messenger.R;
import org.cafemember.messenger.UserConfig;
import org.cafemember.ui.ActionBar.BottomSheet;
import org.cafemember.ui.Adapters.DialogsAdapter;
import org.cafemember.ui.Adapters.DialogsSearchAdapter;
import org.cafemember.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.cafemember.messenger.AnimationCompat.ViewProxy;
import org.cafemember.ui.Cells.ProfileSearchCell;
import org.cafemember.ui.Cells.UserCell;
import org.cafemember.ui.Cells.DialogCell;
import org.cafemember.ui.ActionBar.ActionBar;
import org.cafemember.ui.ActionBar.ActionBarMenu;
import org.cafemember.ui.ActionBar.ActionBarMenuItem;
import org.cafemember.ui.ActionBar.BaseFragment;
import org.cafemember.ui.ActionBar.MenuDrawable;
import org.cafemember.ui.Components.PlayerView;
import org.cafemember.ui.Components.EmptyTextProgressView;
import org.cafemember.ui.Components.LayoutHelper;
import org.cafemember.ui.Components.RecyclerListView;
import org.cafemember.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Calendar;

import ir.adPlay.plugin.adPlay;
import ir.adPlay.plugin.adPlayListener;
import ir.magnet.sdk.MagnetAdLoadListener;
import ir.magnet.sdk.MagnetRewardAd;
import ir.magnet.sdk.MagnetRewardListener;
import ir.magnet.sdk.MagnetSDK;
import ir.tapsell.tapsellvideosdk.developer.CheckCtaAvailabilityResponseHandler;
import ir.tapsell.tapsellvideosdk.developer.DeveloperInterface;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private static final String MAGNET_TOKEN = "e7d94db1e57043098a421c7b0a2188cd";
    public static View gift;
    public static View joinCoins;
    public static View viewCoins;


    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private DialogsAdapter dialogsAdapter;
    private DialogsSearchAdapter dialogsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private ProgressBar progressView;
    private LinearLayout emptyView;
    private ActionBarMenuItem passcodeItem;
    private ImageView floatingButton;

    private AlertDialog permissionDialog;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private boolean checkPermission = true;

    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private int dialogsType;

    private static boolean dialogsLoaded;
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private long selectedDialog;
    private String searchString;
    private long openedDialogId;

    private DialogsActivityDelegate delegate;
    private boolean tapselReady;
    private boolean adPlayReady;
    private String ADPLAY_DEV = "4AGFjLKAIp";
    private String ADPLAY_ID = "VXBdPO3S2m";
    private FrameLayout frameLayout;
    private Context context;
    private MagnetRewardAd rewardAd;

    public interface DialogsActivityDelegate {
        void didSelectDialog(DialogsActivity fragment, long dialog_id, boolean param);
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }


    public View createMyChannel(){
        RecyclerView.Adapter adapter = listView.getAdapter();
        long dialog_id = -Defaults.getInstance().getMyChannelId();
        int message_id = 0;
        if (onlySelect) {
            didSelectResult(dialog_id, true, false);
        } else {
            Bundle args = new Bundle();
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    args.putInt("chat_id", lower_part);
                } else {
                    if (lower_part > 0) {
                        args.putInt("user_id", lower_part);
                    } else if (lower_part < 0) {
                        if (message_id != 0) {
                            TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                            if (chat != null && chat.migrated_to != null) {
                                args.putInt("migrated_to", lower_part);
                                lower_part = -chat.migrated_to.channel_id;
                            }
                        }
                        args.putInt("chat_id", -lower_part);
                    }
                }
            } else {
                args.putInt("enc_id", high_id);
            }
            if (message_id != 0) {
                args.putInt("message_id", message_id);
            } else {
                if (actionBar != null) {
                    actionBar.closeSearchField();
                }
            }
            if (AndroidUtilities.isTablet()) {
                /*if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                    return null;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                    updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                }*/
            }
            if (searchString != null) {
                if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                    ChatActivity chatActivity = new ChatActivity(args);
                    presentMyChannelFragment(chatActivity);
                    return chatActivity.getFragmentView();
                }
            } else {
                if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {

                    ChatActivity chatActivity = new ChatActivity(args);
                    presentMyChannelFragment(chatActivity);
                    return chatActivity.getFragmentView();
                }
            }
        }

        return null;
    }
    public void showChannel(long dialog_id){
        RecyclerView.Adapter adapter = listView.getAdapter();
         if(dialog_id > 0){
             dialog_id = -dialog_id;
         }
        int message_id = 0;
        if (onlySelect) {
            didSelectResult(dialog_id, true, false);
        } else {
            Bundle args = new Bundle();
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    args.putInt("chat_id", lower_part);
                } else {
                    if (lower_part > 0) {
                        args.putInt("user_id", lower_part);
                    } else if (lower_part < 0) {
                        if (message_id != 0) {
                            TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                            if (chat != null && chat.migrated_to != null) {
                                args.putInt("migrated_to", lower_part);
                                lower_part = -chat.migrated_to.channel_id;
                            }
                        }
                        args.putInt("chat_id", -lower_part);
                    }
                }
            } else {
                args.putInt("enc_id", high_id);
            }
            if (message_id != 0) {
                args.putInt("message_id", message_id);
            } else {
                if (actionBar != null) {
                    actionBar.closeSearchField();
                }
            }
            if (AndroidUtilities.isTablet()) {
                /*if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                    return null;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                    updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                }*/
            }
            if (searchString != null) {
                if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                    presentFragment(new ChatActivity(args));
                }
            } else {
                if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {

                    presentFragment(new ChatActivity(args));
                }
            }
        }


    }

    public void showTapsel(){
        DeveloperInterface.getInstance(getParentActivity())
                .showNewVideo(getParentActivity(),
                        DeveloperInterface.TAPSELL_DIRECT_ADD_REQUEST_CODE,
                        DeveloperInterface.DEFAULT_MIN_AWARD,
                        DeveloperInterface.VideoPlay_TYPE);
    }
    public void checkTapsel(final boolean showIfAvail){
        DeveloperInterface.getInstance(getParentActivity())
                .checkCtaAvailability(
                        getParentActivity(), DeveloperInterface.DEFAULT_MIN_AWARD,
                        DeveloperInterface.VideoPlay_TYPE, new CheckCtaAvailabilityResponseHandler() {
                            @Override
                            public void onResponse(Boolean isConnected, Boolean isAvailable) {
                                System.err.println(isConnected + " " + isAvailable);
                                if(isConnected && isAvailable ){
                                    Log.e("Tapsel","Ready");
                                    tapselReady = true;
                                    gift.setVisibility(View.VISIBLE);
                                    if(showIfAvail){
                                        showTapsel();
                                    }/*
                                    else {
                                        Animation myBlinkAnimation = AnimationUtils.loadAnimation(getParentActivity(), R.anim.blink);
                                        gift.startAnimation(myBlinkAnimation);
                                    }*/

                                }else {
                                    tapselReady = false;
                                    Log.e("Tapsel","No Ad");
                                    if(showIfAvail){
                                        Toast.makeText(getParentActivity(),"در حال حاضر تبلیغی وجود ندارد",Toast.LENGTH_SHORT).show();
                                    }/*
                                    else {
                                        gift.setVisibility(View.INVISIBLE);
                                    }*/
                                }
                            }
                        });
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        try {
            if (requestCode == DeveloperInterface.TAPSELL_DIRECT_ADD_REQUEST_CODE) {
                System.err
                        .println(data
                                .hasExtra(DeveloperInterface.TAPSELL_DIRECT_CONNECTED_RESPONSE));
                System.err
                        .println(data
                                .hasExtra(DeveloperInterface.TAPSELL_DIRECT_AVAILABLE_RESPONSE));
                System.err
                        .println(data
                                .hasExtra(DeveloperInterface.TAPSELL_DIRECT_AWARD_RESPONSE));
                System.err
                        .println(data
                                .getBooleanExtra(
                                        DeveloperInterface.TAPSELL_DIRECT_CONNECTED_RESPONSE, false));
                System.err
                        .println(data
                                .getBooleanExtra(
                                        DeveloperInterface.TAPSELL_DIRECT_AVAILABLE_RESPONSE, false));
                int shitil = data
                        .getIntExtra(
                                DeveloperInterface.TAPSELL_DIRECT_AWARD_RESPONSE, -1);
                System.err
                        .println();
                final AlertDialog.Builder alert = new AlertDialog.Builder(getParentActivity());
                alert.setTitle("سکه هدیه");
                alert.setCancelable(true);
                if (shitil > 0) {

                    Commands.doTapsell(shitil, new OnResponseReadyListener() {
                        @Override
                        public void OnResponseReady(boolean error, JSONObject data, String message) {

                            alert.setPositiveButton("باشه", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            if(!error){
                                int total = 0;

                                try {
                                    data = data.getJSONObject("data");
                                    if (data.has("joinCoinsPlus")) {
                                        total = data.getInt("joinCoinsPlus");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if(total > 0) {
                                    alert.setMessage("تبریک! شما " + total + " سکه برای مشاهده این تبلیغ دریافت کردید.");
                                }
                                else {
                                    if(message == null || message.length() == 0){
                                        message = "شرمنده, ظاهرا این تبلیغ هیچ جایزه ای نداشت!";
                                    }
                                    alert.setMessage(message);
                                }
                            }
                            else {
                                if(message == null || message.length() == 0){
                                    message = "شرمنده, خطایی موقع ارسال اطلاعات به سرور رخ داد!";
                                }
                                alert.setMessage(message);
                            }
                            alert.show();
                        }
                    });
                }
                else {
                    alert.setMessage("شرمنده, شما تبلیغ را کامل نکردید!");
                    alert.setPositiveButton("بیخیال", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        if (getArguments() != null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            dialogsType = arguments.getInt("dialogsType", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
        }
        if (searchString == null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
        }


        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 100, true);
            ContactsController.getInstance().checkInviteText();
            dialogsLoaded = true;
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
        }
        delegate = null;
    }

    private void doPlay(int shitil){
        final AlertDialog.Builder alert = new AlertDialog.Builder(getParentActivity());
        alert.setTitle("سکه هدیه");
        alert.setCancelable(true);
        if (shitil > 0) {

            Commands.doAdPlay(shitil, new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {

                    alert.setPositiveButton("باشه", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    if(!error){
                        int total = 0;

                        try {
                            data = data.getJSONObject("data");
                            if (data.has("joinCoinsPlus")) {
                                total = data.getInt("joinCoinsPlus");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(total > 0) {
                            alert.setMessage("تبریک! شما " + total + " سکه برای مشاهده این تبلیغ دریافت کردید.");
                        }
                        else {
                            if(message == null || message.length() == 0){
                                message = "شرمنده, ظاهرا این تبلیغ هیچ جایزه ای نداشت!";
                            }
                            alert.setMessage(message);
                        }
                    }
                    else {
                        if(message == null || message.length() == 0){
                            message = "شرمنده, خطایی موقع ارسال اطلاعات به سرور رخ داد!";
                        }
                        alert.setMessage(message);
                    }
                    alert.show();
                }
            });
        }
        else {
            alert.setMessage("شرمنده, شما تبلیغ را کامل نکردید!");
            alert.setPositiveButton("بیخیال", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    public void showMagnet(){
        if(rewardAd.isAdReady()){
            rewardAd.show(new MagnetRewardListener() {
                @Override
                public void onRewardSuccessful(String verificationToken, String trackingId) {
                    /**
                     *  Give reward to your user.
                     *  You can make sure the reward came from magnet server, within an API entering trackingId and verificationCode.
                     *  The API address is: http://magnet.ir/api/verify/conversion?TrackingId={trackingId}&VerificationToken={verificationToken}
                     */
                    Log.i("Magnet Log", "reward successful");
                    Log.i("Magnet Log", verificationToken);
                    Log.i("Magnet Log", trackingId);
                    Toast.makeText(getParentActivity(),"مشاهده تبلیغات انجام شد می\u200Cتوانید تبلیغ را ببندید",Toast.LENGTH_SHORT).show();
                    doPlay(1);
                }

                @Override
                public void onRewardFail(int i, String s) {
                    doPlay(0);
                    Log.i("Magnet Log", "reward failed");
                    Log.i("Magnet Log", s);
                }
            });
            checkMagnet();
        }
        else{

            Toast.makeText(getParentActivity(),"در حال حاضر تبلیغی وجود ندارد",Toast.LENGTH_SHORT).show();

            Log.i("Magnet Log", "No Video");
        }
    }
    public void checkMagnet(){
        adPlayReady = false;
        rewardAd = MagnetRewardAd.create(context);
        /**
         * When you enable manual loading, you can get the price of video at first
         * and then you can continue loading ad with retrieveData() method.
         */
//                rewardAd.enableManualLoading();

        Log.i("Magnet Log", "Checking;");
        rewardAd.setAdLoadListener(new MagnetAdLoadListener() {
            @Override
            public void onPreload(int price, String currency) {
                Log.i("Magnet Log", "price: " + price + "\ncurrency: " + currency);
                /**
                 * Call retrieveData in onPreload if you have enabled manual Loading.
                 */
//                        rewardAd.retrieveData();
            }

            @Override
            public void onReceive() {
                Log.i("Magnet Log", "New Video");
                adPlayReady = true;
//                showMagnet();
//                gift.setVisibility();
            }

            @Override
            public void onFail(int errorCode, String errorMessage) {
                Log.i("Magnet Log", errorMessage);
                /**
                 * User did not see the ad completely and can not get reward.
                 */
            }
        });

            rewardAd.load(MAGNET_TOKEN);
        

    }
    

    @Override
    public View createView(final Context context) {
        this.context = context;
        /*int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if( today != Defaults.getInstance().getLastDay()){
            Defaults.getInstance().setLastDay(today);
            AlertDialog.Builder builder = null;
            *//*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                builder = new AlertDialog.Builder(context, R.style.MyDialog);
            }
            else {*//*
                builder = new AlertDialog.Builder(context);
//            }

            builder.setTitle("هدیه روزانه");
            builder.setMessage(AndroidUtilities.replaceTags(LocaleController.getString("giftText",R.string.giftText)));

            builder.setPositiveButton("تایید",null);
            showDialog(builder.create());

        }*/



        searching = false;
        searchWas = false;
        MagnetSDK.initialize(context);
        MagnetSDK.getSettings().setSound(true); // enable/disable sound for video ads
        /*adPlay.init(getParentActivity(), ADPLAY_ID, ADPLAY_DEV, new adPlayListener() {

            @Override



            public void onAdComplete(boolean rewardBase) {

                adPlayReady = false;
                if(rewardBase){
                    doPlay(1);
                }
                else {
                    doPlay(0);
                }
                // When ad is show and completed

                // if (rewardBase == true) : Ad has reward, Give the reward

                // if (rewardBase == false) : Ad has no reward, Do not give the reward

            }



            @Override

            public void onAdAvailable(boolean rewardBase) {
                if(rewardBase){
                    adPlayReady = true;
                }
                else {
                    adPlayReady = false;
                }
                // Ad is ready to be shown

                // if (rewardBase == true) : Only for reward base ads

            }



            @Override

            public void onAdFail() {

                // When showing ad failed

            }



            @Override

            public void onInstallationComplete() {

                adPlayReady = false;

                doPlay(1);
                // this event fires when an installation ad is done

            }

        });*/
//        adPlay.setTestMode(true);
        Theme.loadRecources(context);

        ActionBarMenu menu = actionBar.createMenu();
        if (!onlySelect && searchString == null) {
            passcodeItem = menu.addItem(1, R.drawable.lock_close);
            updatePasscodeButton();
        }
        gift = menu.addItemResource(8, R.layout.gift_view);
        joinCoins = menu.addItemResource(7, R.layout.join_coins_view);
        joinCoins.setOnClickListener(new View.OnClickListener() {
             @Override
                 public void onClick(View v) {
                 ManageSpaceActivity.viewPager.setCurrentItem(0);
                 TabLayout.Tab tab = ManageSpaceActivity.tabLayout.getTabAt(0);
                 tab.select();
           }
              });
//        gift.setVisibility(View.INVISIBLE);
          gift.setVisibility(View.INVISIBLE);
        gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adPlayReady){
//                    adPlay.showAd(true);
                    showMagnet();
                }
                else {
                    checkTapsel(true);
                }
            }
        });
        FontManager.instance().setTypefaceImmediate(joinCoins);
//        viewCoins = menu.addItemResource(8, R.layout.view_coins_view);

//        final ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
//            @Override
//            public void onSearchExpand() {
//                searching = true;
//                if (listView != null) {
//                    if (searchString != null) {
//                        listView.setEmptyView(searchEmptyView);
//                        progressView.setVisibility(View.GONE);
//                        emptyView.setVisibility(View.GONE);
//                    }
//                    if (!onlySelect) {
//                        floatingButton.setVisibility(View.GONE);
//                    }
//                }
//                updatePasscodeButton();
//            }
//
//            @Override
//            public boolean canCollapseSearch() {
//                if (searchString != null) {
//                    finishFragment();
//                    return false;
//                }
//                return true;
//            }
//
//            @Override
//            public void onSearchCollapse() {
//                searching = false;
//                searchWas = false;
//                if (listView != null) {
//                    searchEmptyView.setVisibility(View.GONE);
//                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
//                        emptyView.setVisibility(View.GONE);
//                        listView.setEmptyView(progressView);
//                    } else {
//                        progressView.setVisibility(View.GONE);
//                        listView.setEmptyView(emptyView);
//                    }
//                    if (!onlySelect) {
//                        floatingButton.setVisibility(View.VISIBLE);
//                        floatingHidden = true;
//                        ViewProxy.setTranslationY(floatingButton, AndroidUtilities.dp(100));
//                        hideFloatingButton(false);
//                    }
//                    if (listView.getAdapter() != dialogsAdapter) {
//                        listView.setAdapter(dialogsAdapter);
//                        dialogsAdapter.notifyDataSetChanged();
//                    }
//                }
//                if (dialogsSearchAdapter != null) {
//                    dialogsSearchAdapter.searchDialogs(null);
//                }
//                updatePasscodeButton();
//            }
//
//            @Override
//            public void onTextChanged(EditText editText) {
//                String text = editText.getText().toString();
//                if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch()) {
//                    searchWas = true;
//                    if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter) {
//                        listView.setAdapter(dialogsSearchAdapter);
//                        dialogsSearchAdapter.notifyDataSetChanged();
//                    }
//                    if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView) {
//                        emptyView.setVisibility(View.GONE);
//                        progressView.setVisibility(View.GONE);
//                        searchEmptyView.showTextView();
//                        listView.setEmptyView(searchEmptyView);
//                    }
//                }
//                if (dialogsSearchAdapter != null) {
//                    dialogsSearchAdapter.searchDialogs(text);
//                }
//            }
//        });
//        item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        if (onlySelect) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
        } else {
            if (searchString != null) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            } else {
                actionBar.setBackButtonDrawable(new MenuDrawable());
            }
            if (BuildVars.DEBUG_VERSION) {
                actionBar.setTitle(LocaleController.getString("AppNameBeta", R.string.AppNameBeta));
            } else {
                actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
            }
        }
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == 1) {
                    UserConfig.appLocked = !UserConfig.appLocked;
                    UserConfig.saveConfig(false);
                    updatePasscodeButton();
                }
            }
        });


         frameLayout = new FrameLayout(context);
//        fragmentView = frameLayout;
        Commands.wait4Ans = true;
        fragmentView = Views.getTabLayout((FragmentActivity) context, this, frameLayout);
        
        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setItemAnimator(null);
        listView.setInstantClick(true);
        listView.setLayoutAnimation(null);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        if (Build.VERSION.SDK_INT >= 11) {
            listView.setVerticalScrollbarPosition(LocaleController.isRTL ? ListView.SCROLLBAR_POSITION_LEFT : ListView.SCROLLBAR_POSITION_RIGHT);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnAddChannelClickListener(
                new RecyclerListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (listView == null || listView.getAdapter() == null) {
                            return;
                        }
                        long dialog_id = 0;
                        int message_id = 0;
                        RecyclerView.Adapter adapter = listView.getAdapter();
                        if (adapter == dialogsAdapter) {
                            TLRPC.Dialog dialog = dialogsAdapter.getItem(position);
                            if (dialog == null) {
                                return;
                            }
                            int lower_id = (int) dialog.id;
                            final TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle(LocaleController.getString("MemberBegirTitle", R.string.MemberBegirTitle));

                            /*builder.setItems(Defaults.MEMBERS_COUNT , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Commands.addChannel(chat,Integer.parseInt(Defaults.MEMBERS_COUNT[which]));
                                }
                            });*/
                            /*builder.setAdapter(new ReserveAdapter(getParentActivity(),R.layout.adapter_buy_coin,1), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Commands.addChannel(chat,Integer.parseInt(Defaults.MEMBERS_COUNT[which]));
                                }
                            });*/
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }
                    }
                }
        );

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (listView == null || listView.getAdapter() == null) {
                    return;
                }
                long dialog_id = 0;
                int message_id = 0;
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsAdapter) {
                    TLRPC.Dialog dialog = dialogsAdapter.getItem(position);
                    if (dialog == null) {
                        return;
                    }
                    dialog_id = dialog.id;
                } else if (adapter == dialogsSearchAdapter) {
                    Object obj = dialogsSearchAdapter.getItem(position);
                    if (obj instanceof TLRPC.User) {
                        dialog_id = ((TLRPC.User) obj).id;
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add((TLRPC.User) obj);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.User) obj);
                        }
                    } else if (obj instanceof TLRPC.Chat) {
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                            chats.add((TLRPC.Chat) obj);
                            MessagesController.getInstance().putChats(chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                        }
                        if (((TLRPC.Chat) obj).id > 0) {
                            dialog_id = -((TLRPC.Chat) obj).id;
                        } else {
                            dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.Chat) obj);
                        }
                    } else if (obj instanceof TLRPC.EncryptedChat) {
                        dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.EncryptedChat) obj);
                        }
                    } else if (obj instanceof MessageObject) {
                        MessageObject messageObject = (MessageObject) obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.getId();
                        dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
                    } else if (obj instanceof String) {
                        actionBar.openSearchField((String) obj);
                    }
                }

                if (dialog_id == 0) {
                    return;
                }

                if (onlySelect) {
                    didSelectResult(dialog_id, true, false);
                } else {
                    Bundle args = new Bundle();
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                if (message_id != 0) {
                                    TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                                    if (chat != null && chat.migrated_to != null) {
                                        args.putInt("migrated_to", lower_part);
                                        lower_part = -chat.migrated_to.channel_id;
                                    }
                                }
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (message_id != 0) {
                        args.putInt("message_id", message_id);
                    } else {
                        if (actionBar != null) {
                            actionBar.closeSearchField();
                        }
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                            return;
                        }
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {
                if (onlySelect || searching && searchWas || getParentActivity() == null) {
                    if (searchWas && searching || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                        RecyclerView.Adapter adapter = listView.getAdapter();
                        if (adapter == dialogsSearchAdapter) {
                            Object item = dialogsSearchAdapter.getItem(position);
                            if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                                builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                            dialogsSearchAdapter.clearRecentSearch();
                                        } else {
                                            dialogsSearchAdapter.clearRecentHashtags();
                                        }
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                                return true;
                            }
                        }
                    }
                    return false;
                }
                TLRPC.Dialog dialog;
                ArrayList<TLRPC.Dialog> dialogs = getDialogsArray();
                if (position < 0 || position >= dialogs.size()) {
                    return false;
                }
                dialog = dialogs.get(position);
                selectedDialog = dialog.id;

                BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                int lower_id = (int) selectedDialog;
                int high_id = (int) (selectedDialog >> 32);

                if (dialog instanceof TLRPC.TL_dialogChannel) {
                    final TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                    CharSequence items[];
                    if (chat != null && chat.megagroup) {
                        items = new CharSequence[]{LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache), chat == null || !chat.creator ? LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu) : LocaleController.getString("DeleteMegaMenu", R.string.DeleteMegaMenu)};
                    } else {
                        items = new CharSequence[]{LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache), chat == null || !chat.creator ? LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu) : LocaleController.getString("ChannelDeleteMenu", R.string.ChannelDeleteMenu),LocaleController.getString("MemberBegir", R.string.MemberBegir)};
                    }
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            if (which == 0) {
                                if (chat != null && chat.megagroup) {
                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistorySuper", R.string.AreYouSureClearHistorySuper));
                                } else {
                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", R.string.AreYouSureClearHistoryChannel));
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MessagesController.getInstance().deleteDialog(selectedDialog, 2);
                                    }
                                });
                            } else if(which == 2){
                                builder.setTitle(LocaleController.getString("MemberBegirTitle", R.string.MemberBegirTitle));

                                /*builder.setAdapter(new ReserveAdapter(getParentActivity(),R.layout.adapter_buy_coin,1), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Commands.addChannel(chat,Integer.parseInt(Defaults.MEMBERS_COUNT[which]));
                                    }
                                });*/

                                /*builder.setItems(Defaults.MEMBERS_COUNT , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Commands.addChannel(chat,Integer.parseInt(Defaults.MEMBERS_COUNT[which]));
                                    }
                                });*/
                            }
                            else {
                                if (chat != null && chat.megagroup) {
                                    if (!chat.creator) {
                                        builder.setMessage(LocaleController.getString("MegaLeaveAlert", R.string.MegaLeaveAlert));
                                    } else {
                                        builder.setMessage(LocaleController.getString("MegaDeleteAlert", R.string.MegaDeleteAlert));
                                    }
                                } else {
                                    if (chat == null || !chat.creator) {
                                        builder.setMessage(LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert));
                                    } else {
                                        builder.setMessage(LocaleController.getString("ChannelDeleteAlert", R.string.ChannelDeleteAlert));
                                    }
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, UserConfig.getCurrentUser(), null);
                                        if (AndroidUtilities.isTablet()) {
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                        }
                                    }
                                });
                            }
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }
                    });
                    showDialog(builder.create());
                } else {
                    final boolean isChat = lower_id < 0 && high_id != 1;
                    TLRPC.User user = null;
                    if (!isChat && lower_id > 0 && high_id != 1) {
                        user = MessagesController.getInstance().getUser(lower_id);
                    }
                    final boolean isBot = user != null && user.bot;
                    builder.setItems(new CharSequence[]{LocaleController.getString("ClearHistory", R.string.ClearHistory),
                            isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) :
                                    isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete)}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            if (which == 0) {
                                builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                            } else {
                                if (isChat) {
                                    builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
                                } else {
                                    builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                                }
                            }
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (which != 0) {
                                        if (isChat) {
                                            TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                                            if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                                                MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                            } else {
                                                MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), null);
                                            }
                                        } else {
                                            MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                        }
                                        if (isBot) {
                                            MessagesController.getInstance().blockUser((int) selectedDialog);
                                        }
                                        if (AndroidUtilities.isTablet()) {
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                        }
                                    } else {
                                        MessagesController.getInstance().deleteDialog(selectedDialog, 1);
                                    }
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }
                    });
                    showDialog(builder.create());
                }
                return true;
            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.GONE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new LinearLayout(context);
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setVisibility(View.GONE);
        emptyView.setGravity(Gravity.CENTER);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        TextView textView = new TextView(context);
        textView.setText(LocaleController.getString("NoChats", R.string.NoChats));
        textView.setTextColor(0xff959595);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyView.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        String help = LocaleController.getString("NoChatsHelp", R.string.NoChatsHelp);
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace('\n', ' ');
        }
        textView.setText(help);
        textView.setTextColor(0xff959595);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(6), AndroidUtilities.dp(8), 0);
        textView.setLineSpacing(AndroidUtilities.dp(2), 1);
        emptyView.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        progressView = new ProgressBar(context);
        progressView.setVisibility(View.GONE);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        floatingButton = new ImageView(context);
        floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton.setBackgroundResource(R.drawable.floating_states);
        floatingButton.setImageResource(R.drawable.floating_pencil);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                presentFragment(new ContactsActivity(args));
            }
        });

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (searching && searchWas) {
                    if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                        dialogsSearchAdapter.loadMoreSearchMessages();
                    }
                    return;
                }
                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray().size() - 10) {
                        MessagesController.getInstance().loadDialogs(-1, 100, !MessagesController.getInstance().dialogsEndReached);
                    }
                }

                if (floatingButton.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated) {
                        hideFloatingButton(goingDown);
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }
            }
        });

        if (searchString == null) {
            dialogsAdapter = new DialogsAdapter(context, dialogsType);
            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
                dialogsAdapter.setOpenedDialogId(openedDialogId);
            }
            listView.setAdapter(dialogsAdapter);
        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
        dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.MessagesActivitySearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }
        });

        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setEmptyView(progressView);
        } else {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);
        }
        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

        if (!onlySelect && dialogsType == 0) {
            frameLayout.addView(new PlayerView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        }

//        fragmentView = Views.getTabLayout((FragmentActivity) context, this, frameLayout);
        if(Defaults.getInstance().getMyToken().equals("")){
            Commands.login(UserConfig.getCurrentUser().phone,DialogsActivity.this, new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                    Commands.loadCoins(DialogsActivity.this,new OnCoinsReady() {
                        @Override
                        public void onCoins(int viewCoinsAmount, int joinCoinsAmount) {
                            Log.e("COIN",joinCoinsAmount+"");
                            ApplicationLoader.setJoinCoins(joinCoinsAmount,true);
                        }
                    });
                }
            });
        }
        else {
        Commands.loadCoins(this,new OnCoinsReady() {
            @Override
            public void onCoins(int viewCoinsAmount, int joinCoinsAmount) {
                Log.e("COIN",joinCoinsAmount+"");
                ApplicationLoader.setJoinCoins(joinCoinsAmount,true);
            }
        });
        }
        return fragmentView;
    }

    public void afterWait(){
        ViewGroup parent = (ViewGroup) fragmentView.getParent();
        if (parent != null) {
            parent.removeView(fragmentView);
            fragmentView = Views.getTabLayout((FragmentActivity) context, this, frameLayout);
            parent.addView(fragmentView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(Commands.wait4Ans && Commands.enter2Ad){
            Commands.wait4Ans = false;
            Commands.enter2Ad = false;
            afterWait();
        }
        Commands.checkVersion(this);

//        adPlay.checkAdAvailability(true);
        checkMagnet();
        checkTapsel(false);

        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (/*activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||*/ activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    /*if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else*/ if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else {
                        askForPermissons();
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissons() {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissons = new ArrayList<>();
      /*  if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_CONTACTS);
            permissons.add(Manifest.permission.WRITE_CONTACTS);
            permissons.add(Manifest.permission.GET_ACCOUNTS);
        }*/
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] items = permissons.toArray(new String[permissons.size()]);
        activity.requestPermissions(items, 1);
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null) {
            askForPermissons();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!onlySelect && floatingButton != null) {
            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewProxy.setTranslationY(floatingButton, floatingHidden ? AndroidUtilities.dp(100) : 0);
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        if (Build.VERSION.SDK_INT < 16) {
                            floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a || grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                switch (permissions[a]) {
                    /*case Manifest.permission.READ_CONTACTS:
                        ContactsController.getInstance().readContacts();
                        break;*/
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        ImageLoader.getInstance().checkMediaPaths();
                        break;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (dialogsAdapter != null) {
                if (dialogsAdapter.isDataSetChanged()) {
                    dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                }
            }
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
            if (listView != null) {
                try {
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        searchEmptyView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        if (searching && searchWas) {
                            emptyView.setVisibility(View.GONE);
                            listView.setEmptyView(searchEmptyView);
                        } else {
                            searchEmptyView.setVisibility(View.GONE);
                            listView.setEmptyView(emptyView);
                        }
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e); //TODO fix it in other way?
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.updateInterfaces) {
            updateVisibleRows((Integer) args[0]);
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.openedChatChanged) {
            if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = (Boolean) args[1];
                long dialog_id = (Long) args[0];
                if (close) {
                    if (dialog_id == openedDialogId) {
                        openedDialogId = 0;
                    }
                } else {
                    openedDialogId = dialog_id;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId);
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.loadRecentSearch();
            }
        } else if (id == NotificationCenter.didLoadedReplyMessages) {
            updateVisibleRows(0);
        }
    }

    private ArrayList<TLRPC.Dialog> getDialogsArray() {
        if (dialogsType == 0) {
            return MessagesController.getInstance().dialogs;
        } else if (dialogsType == 1) {
            return MessagesController.getInstance().dialogsServerOnly;
        } else if (dialogsType == 2) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        }
        return null;
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (UserConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (UserConfig.appLocked) {
                passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(100) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
    }

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof DialogCell) {
                if (listView.getAdapter() != dialogsSearchAdapter) {
                    DialogCell cell = (DialogCell) child;
                    if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                        cell.checkCurrentDialogIndex();
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else {
                        cell.update(mask);
                    }
                }
            } else if (child instanceof UserCell) {
                ((UserCell) child).update(mask);
            } else if (child instanceof ProfileSearchCell) {
                ((ProfileSearchCell) child).update(mask);
            }
        }
    }

    public void setDelegate(DialogsActivityDelegate delegate) {
        this.delegate = delegate;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (addToGroupAlertString == null) {
            if ((int) dialog_id < 0 && ChatObject.isChannel(-(int) dialog_id) && !ChatObject.isCanWriteToChannel(-(int) dialog_id)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                showDialog(builder.create());
                return;
            }
        }
        if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance().getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance().getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        if (addToGroupAlertString != null) {
                            builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
                        } else {
                            builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                        }
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance().getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
            }

            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    didSelectResult(dialog_id, false, false);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                delegate.didSelectDialog(DialogsActivity.this, dialog_id, param);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }
}