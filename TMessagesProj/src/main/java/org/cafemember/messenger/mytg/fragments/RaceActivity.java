/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.cafemember.messenger.mytg.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.cafemember.messenger.AndroidUtilities;
import org.cafemember.messenger.LocaleController;
import org.cafemember.messenger.R;
import org.cafemember.messenger.mytg.Commands;
import org.cafemember.messenger.mytg.FontManager;
import org.cafemember.messenger.mytg.listeners.OnResponseReadyListener;
import org.cafemember.ui.ActionBar.ActionBar;
import org.cafemember.ui.ActionBar.ActionBarMenu;
import org.cafemember.ui.ActionBar.BaseFragment;
import org.cafemember.ui.Components.LayoutHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RaceActivity extends BaseFragment {

    private View doneButton;
    private TextView checkTextView;
    private Context context;
    private String Btn_Text = "ارسال درخواست";
    private String TextLoading = "در حال دریافت اطلاعات";
    private String compInfo = "";
    private boolean isCompEnabled = false;
    private boolean isWinnersEnabled= false;
    private final static int done_button = 1;
    private String Winner_Text = "نمایش برندگان";
    private TextView helpTextView;
    private Button done;
    private Button done2;
    private ProgressBar loader;

    @Override
    public View createView(final Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("MenuMilShow", R.string.MenuMilShow));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();




         loader = new ProgressBar(context);
        loader.setVisibility(View.VISIBLE);
        fragmentView = new LinearLayout(context);
        ((LinearLayout) fragmentView).setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        helpTextView = new TextView(context);
        helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        helpTextView.setTextColor(0xff212121);
        helpTextView.setGravity(Gravity.RIGHT);
//        helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("ShareText", R.string.ShareText)));

        helpTextView.setText(TextLoading);
        ((LinearLayout) fragmentView).addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 24, 10, 24, 0));


        done = new Button(context);
        done.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        done.setTextColor(0xff212121);
        done.setText(Btn_Text);
//        done.setEnabled(false);
        done.setVisibility(View.GONE);
        done2 = new Button(context);
        done2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        done2.setTextColor(0xff212121);
        done2.setText(Winner_Text);
        done2.setVisibility(View.GONE);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reg();
            }
        });
        done2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                winners();
            }
        });
        ((LinearLayout) fragmentView).addView(loader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 24, 24, 24, 0));

        ((LinearLayout) fragmentView).addView(done, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 24, 30, 24, 0));
        ((LinearLayout) fragmentView).addView(done2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 24, 30, 24, 0));

        FontManager.instance().setTypefaceImmediate(fragmentView);

        Commands.getCompInfo(new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){
                    try {
                        data = data.getJSONObject("data");
                        isCompEnabled = data.getBoolean("isCompEnabled");
                        isWinnersEnabled = data.getBoolean("isWinnersEnabled");
                        compInfo = data.getString("compInfo");
                        int count = data.getInt("count");
                        if(count > 0){
                            compInfo+="\n\nشما "+count+" درخواست ثبت کردید.";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    done.setEnabled(isCompEnabled);
                    if(!isCompEnabled && data.has("alert")){


                        try {
                            AlertDialog.Builder builder = null;
                            builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle("ممبر شاپ");
                            builder.setMessage(data.getString("alert"));
                            builder.setPositiveButton("باشه", null);
                            builder.create().show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    done.setVisibility(isCompEnabled?View.VISIBLE:View.GONE);
                    done2.setVisibility(isWinnersEnabled?View.VISIBLE:View.GONE);
                }
                else {
                    if(message!= null && message.length() > 0){
                        compInfo = message;
                    }
                    else {
                        compInfo = "خطا در دریافت اطلاعات";
                    }
                }
                helpTextView.setText(compInfo);
                loader.setVisibility(View.GONE);
            }
        });

        return fragmentView;
    }

    private void reg(){
        loader.setVisibility(View.VISIBLE);
        Commands.reg4Comp(new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){
                    message = "درخواست شما با موفقیت ثبت شد و تا 24 ساعت آینده شارژ شما ارسال خواهد شد.";
                }
                else {
                    if(message == null || message.length() == 0){
                        message = "خطا در ارسال درخواست";
                    }
                }
                Toast.makeText(getParentActivity(),message,Toast.LENGTH_SHORT).show();
                loader.setVisibility(View.GONE);
            }
        });
    }
    private void winners(){
        loader.setVisibility(View.VISIBLE);
        Commands.getWinners(new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {

                if(!error){
                    AlertDialog.Builder builder = null;
                    builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle("برندگان این دوره");
                    String [] winnerList = {"خطا در دریافت لیست"};
                    try {
                        JSONArray data2 = data.getJSONArray("data");
                        int size = data2.length();
                        winnerList= new String[size];
                        for( int i =0; i< size; i++){
                            JSONObject item = data2.getJSONObject(i);
                            String user = item.getString("user");
                            user = user.substring(user.length()-10);
                            Log.e("WIN",user);
                            user = user.substring(6)+"***"+user.substring(0,3);
                            String amount = item.getString("amount");
                            winnerList[i] = user + "   :   " + amount+" سکه ";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    builder.setItems(winnerList, null);
                    builder.setPositiveButton("باشه", null);
                    builder.create().show();

                }
                else {
                    if(message == null || message.length() == 0){
                        message = "خطا در ارسال درخواست";
                    }
                    Toast.makeText(getParentActivity(),message,Toast.LENGTH_SHORT).show();
                }
                loader.setVisibility(View.GONE);
            }
        });
    }



}
