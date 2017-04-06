package org.cafemember.messenger.mytg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import org.cafemember.messenger.BuildVars;
import org.cafemember.messenger.mytg.adapter.ReserveAdapter;
import org.cafemember.messenger.mytg.fragments.MyChannelFragment;
import org.cafemember.messenger.mytg.util.FileConvert;
import org.cafemember.ui.ActionBar.BaseFragment;
import org.cafemember.ui.DialogsActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.cafemember.messenger.AndroidUtilities;
import org.cafemember.messenger.ApplicationLoader;
import org.cafemember.messenger.FileLog;
import org.cafemember.messenger.LocaleController;
import org.cafemember.messenger.MessageObject;
import org.cafemember.messenger.MessagesController;
import org.cafemember.messenger.R;
import org.cafemember.messenger.mytg.listeners.OnChannelReady;
import org.cafemember.messenger.mytg.listeners.OnCoinsReady;
import org.cafemember.messenger.mytg.listeners.OnJoinSuccess;
import org.cafemember.messenger.mytg.listeners.OnResponseReadyListener;
import org.cafemember.messenger.mytg.util.API;
import org.cafemember.messenger.mytg.util.Defaults;
import org.cafemember.tgnet.ConnectionsManager;
import org.cafemember.tgnet.RequestDelegate;
import org.cafemember.tgnet.TLObject;
import org.cafemember.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Masoud on 6/1/2016.
 */
public class Commands {

    private static Context context = ApplicationLoader.applicationContext;
    private static int lastMessage = 0;
    private static AlertDialog visibleDialog;

    public static boolean wait4Ans = true;
    public static boolean enter2Ad = false;
    public static JSONArray JoinCoins;
    public static JSONArray ViewCoins;
    private static int x = 0;
    private static int limit = 100;
    private static int offset = 0;
    private static int z = 0;

    public static void view(final int id){

        if(lastMessage == id){
            return;
        }
        lastMessage = id;
//        Toast.makeText(ApplicationLoader.applicationContext, id+" Marked.",Toast.LENGTH_SHORT).show();
        API.getInstance().run(String.format(Locale.ENGLISH, "/posts/view/%d", id), new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){
                    loadCoins(data);
                }
                else {
//                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static void join(final Channel channel, final OnJoinSuccess joinSuccess){
        join(channel, new OnResponseReadyListener(){

            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                joinSuccess.OnResponse(!error);
            }
        });

    }

    public static void join(final Channel channel, final OnResponseReadyListener joinSuccess){
        join(channel, joinSuccess, true);
    }
    public static void join(final Channel channel, final OnResponseReadyListener joinSuccess, final boolean joinServer){
        if(channel.inputChannel == null) {
            Defaults.getInstance().loadChannel(channel, new OnChannelReady() {
                @Override
                public void onReady(final Channel channel, boolean isOK) {
                    if (isOK) {
                        TLRPC.TL_channels_joinChannel req = new TLRPC.TL_channels_joinChannel();
                        req.channel = channel.inputChannel;
                        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                            @Override
                            public void run(final TLObject response, final TLRPC.TL_error error) {
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (error == null) {
                                            if(joinServer){
                                                joinChannel(channel, joinSuccess);
                                            }
                                            else {
                                                joinSuccess.OnResponseReady(false, null, "عضویت مجدد انجام شد");
                                            }
                                        }else {
                                            String errMsg = "شما به مدت 4 دقیقه نمی توانید عضو کانال شوید.\nاین محدودیت از سمت تلگرام است." +
                                                    "لطفا تا اتمام محدودیت عضو کانالی نشوید. در صورت عضو شدن این زمان دوباره تمدید خواهد شد.";
                                            if(error.code == 400 || error.text.equals("CHANNELS_TOO_MUCH")){// Too Much
                                                errMsg = "خطا در عضویت \n" +
                                                        "تعداد کانالهای هر شماره در تلگرام محدود است .\n" +
                                                        "تلگرام اجازه عضو شدن در کانال جدید به شما نمیدهد.\n" +
                                                        "از طریق شماره دیگری سکه جمع اوری کنید.";
                                            }else if(error.code == 420 || error.text.startsWith("FLOOD_WAIT_")){// Too Much
                                    errMsg = "در حال حاظر شما از طرف تلگرام محدود شدید";
                                            }
                                            joinSuccess.OnResponseReady(true, null,errMsg );
                                        }
                                    }
                                });
                            }
                        }, ConnectionsManager.RequestFlagFailOnServerErrors);
                    } else {
                        joinSuccess.OnResponseReady(true, null, "در حال حاظر شما از طرف تلگرام محدود شدید");
                    }
                }
            });
        }

        else {
            TLRPC.TL_channels_joinChannel req = new TLRPC.TL_channels_joinChannel();
            req.channel = channel.inputChannel;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                @Override
                public void run(final TLObject response, final TLRPC.TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (error == null) {

                                if(joinServer){
                                    joinChannel(channel, joinSuccess);
                                }
                                else {
                                    joinSuccess.OnResponseReady(false, null, "عضویت مجدد انجام شد");
                                }
                            } else {

                                String errMsg = "شما به مدت 4 دقیقه نمی توانید عضو کانال شوید.\nاین محدودیت از سمت تلگرام است." +
                                        "لطفا تا اتمام محدودیت عضو کانالی نشوید. در صورت عضو شدن این زمان دوباره تمدید خواهد شد.";
                                if(error.code == 400 || error.text.equals("CHANNELS_TOO_MUCH")){// Too Much  "USERNAME_NOT_OCCUPIED"
                                    errMsg = "خطا در عضویت \n" +
                                            "تعداد کانالهای هر شماره در تلگرام محدود است .\n" +
                                            "تلگرام اجازه عضو شدن در کانال جدید به شما نمیدهد.\n" +
                                            "از طریق شماره دیگری سکه جمع اوری کنید.";
                                }else if(error.code == 420 || error.text.startsWith("FLOOD_WAIT_")){// Too Much
                                    errMsg = "در حال حاظر شما از طرف تلگرام محدود شدید";
                                }
                                joinSuccess.OnResponseReady(true, null,errMsg );
                            }
                        }
                    });
                }
            }, ConnectionsManager.RequestFlagFailOnServerErrors);
        }


    }

    private static void joinChannel(final Channel channel, final OnResponseReadyListener joinSuccess){
        TLRPC.FileLocation loc = channel.photo;
        String url;
            url = String.format(Locale.ENGLISH, "/channels/join/%d", channel.id);

        String body = "";
        try {
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            body = "{\"version\":"+versionCode+"}";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        API.getInstance().post(url, body, new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){
                    loadCoins(data);
                    joinSuccess.OnResponseReady(error,data,message);
                }
                else {
                    joinSuccess.OnResponseReady(error,data,message);
                }
            }
        });
    }

    public static void left(){

    }

    public static void report(int id, String reason,OnResponseReadyListener onResponseReadyListener){
        API.getInstance().post(String.format(Locale.ENGLISH, "/channels/report/%d",id), "{\"reason\":\""+reason+"\"}", onResponseReadyListener);
    }

    public static void coinsPrice(final OnResponseReadyListener onResponseReadyListener){
        API.getInstance().run(String.format(Locale.ENGLISH, "/coin/getCoinsPrice"), onResponseReadyListener);
    }

    public static void defaultCoins(final OnJoinSuccess onJoinSuccess){
        if(JoinCoins == null || ViewCoins == null) {
            API.getInstance().run(String.format(Locale.ENGLISH, "/coin/getDefaultCoins"), new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                    if(!error){
                        try {
                            data = data.getJSONObject("data");
                            ViewCoins = data.getJSONArray("viewCoins");
                            JoinCoins = data.getJSONArray("joinCoins");
                            onJoinSuccess.OnResponse(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        onJoinSuccess.OnResponse(false);
                    }
                }
            });
        }
        else {
            onJoinSuccess.OnResponse(true);
        }
    }

    public static void buy(String id){
        Intent i = null;
       /* if(BuildVars.MARKET_NAME.equals("iranapps")){
            i = new Intent(context,IranPaymentActivity.class);
        }
        else {*/
         // i = new Intent(context,PaymentActivity.class);
//        }
      i = new Intent(context, PayActivityNivad.class);
        i.putExtra("sku",id);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void checkBoughtItem(String id, String details, final OnResponseReadyListener success) {

        API.getInstance().post(String.format(Locale.ENGLISH, "/coin/buyCoin/%s/"+BuildVars.MARKET_NAME, id), details, new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){

                    //Log.e("sina Error","if");
                    loadCoins(data);
                    //Log.e("sina Error","if");
                    success.OnResponseReady(error,data,message);
                    //Log.e("sina Error","if");
                }
                else {
                    //Log.e("sina Error","else");
                    //Log.e("sina Error ",message+" / "+data.toString());
                    success.OnResponseReady(error,data,message);
                }
            }
        });
    }

    public static void transfare(String user, int amount, int type, final OnResponseReadyListener onResponseReadyListener){
        API.getInstance().post(String.format(Locale.ENGLISH, "/coin/transfare/%d/%s/%d", type,user,amount), "", new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){
                    loadCoins(data);

                }
                onResponseReadyListener.OnResponseReady(error,data,message);
            }
        });

    }

    public static void login(String phone, final BaseFragment base,  final OnResponseReadyListener onResponseReadyListener){
        if(phone != null )
        {

            if(phone.length() >= 10 ){
                phone = "98"+phone.substring(phone.length()-10);
            }
        }
        API.getInstance().post(String.format(Locale.ENGLISH, "/user/login/%s", phone), "", new OnResponseReadyListener() {
                           @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){
                    try {
                        loadCoins(data);
                        data = data.getJSONObject("data");
                        String token = data.getString("token");

                        if(data.has("channel_name")){
                            String channelName = data.getString("channel_name");
                            Defaults.getInstance().setMyChannelName(channelName);
                        }
                        if(data.has("support")){
                            String channelName = data.getString("support");
                            Defaults.getInstance().setSupport(channelName);
                        }
                        if(data.has("help_channel_id")){
                            int  helpcChannelId = data.getInt("help_channel_id");
                            Defaults.getInstance().setHelpChannelId(helpcChannelId);
                        }
                        if(data.has("channel_id")){
                            int  channelId = data.getInt("channel_id");
                            Defaults.getInstance().setMyChannelId(channelId);
                        }
                        Defaults.getInstance().setMyToken(token);
                        if( data.has("first") ){
                            Log.e("log","Has First");
                            if(data.getBoolean("first")){
                                Log.e("log","Yess");
                                AlertDialog.Builder builder = null;
                                builder = new AlertDialog.Builder(base.getParentActivity());
                                builder.setTitle("هدیه ثبت نام");
                                builder.setMessage(AndroidUtilities.replaceTags(LocaleController.getString("giftText2", R.string.giftText2)));

                                builder.setPositiveButton("تایید", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                base.showDialogNoCancel(builder.create());
//                                showAlertDialog(builder.create());
                            }

                        }
                        final Defaults def = Defaults.getInstance();
                        if(!def.isChannelSet()) {
                            def.loadMyChannel(new OnJoinSuccess() {
                                @Override
                                public void OnResponse(boolean ok) {
                                    if(ok){
                                        def.setChannelSet(true);
                                    }
                                    else {
                                        AlertDialog.Builder builder = null;
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                                            builder = new AlertDialog.Builder(context, R.style.MyDialog);
                                        }
                                        else {
                                            builder = new AlertDialog.Builder(context);
                                        }
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        builder.setMessage(LocaleController.getString("MyChannelError", R.string.MyChannelError)+"  \n@"+def.getMyChannelName());
                                        builder.setNegativeButton(LocaleController.getString("MyCancel", R.string.MyCancel), null);
                                        showAlertDialog(builder.create());
                                    }

                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(onResponseReadyListener != null){
                    onResponseReadyListener.OnResponseReady(error, data, message);
                }
            }
        });
    }

    public static void ref(String phone, final OnResponseReadyListener onResponseReadyListener){
        API.getInstance().post(String.format(Locale.ENGLISH, "/user/ref/%s", phone), "", new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){
//                    Toast.makeText(context, "معرف با موفقیت ثبت شد",Toast.LENGTH_LONG).show();
                    onResponseReadyListener.OnResponseReady(error, data, "معرف با موفقیت ثبت شد");
                }
                else {
                    onResponseReadyListener.OnResponseReady(error, data, message);
//                    Toast.makeText(context, message,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static ConcurrentHashMap<Long, TLRPC.Dialog> dialogs_dict = new ConcurrentHashMap<>(100, 1.0f, 2);
    public static HashMap<Integer, TLRPC.Chat> total_chatsDict = new HashMap<>();

    public static ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>();

    public static void checkChannelsTrigger(final ArrayList<TLRPC.Dialog> dsf,final DialogsActivity dialogsActivity){
        Log.e("CHK", "Checking Channels...");
//        dialogs_dict.clear();
//        checkChannels(dsf,dialogsActivity);
        limit = 100;
        offset = 0;
        dialogs_dict.clear();
        dialogs.clear();
        total_chatsDict.clear();
        checkChannels(dsf, dialogsActivity);
    }


    public static void checkChannelsV2(final ArrayList<TLRPC.Dialog> dsf,final DialogsActivity dialogsActivity) {
//        Log.e("CHK","Start Checking Channels ...");


        TLRPC.TL_channels_getDialogs req = new TLRPC.TL_channels_getDialogs();
        req.limit = limit;
        req.offset = offset;

        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(TLObject response, TLRPC.TL_error error) {
                if (error == null) {
                    final TLRPC.messages_Dialogs org_telegram_tgnet_TLRPC_messages_Dialogs = (TLRPC.messages_Dialogs) response;
                    ArrayList myDialogs ;
                    myDialogs = org_telegram_tgnet_TLRPC_messages_Dialogs.chats;
                    if (org_telegram_tgnet_TLRPC_messages_Dialogs.count != 0) {
                        z = org_telegram_tgnet_TLRPC_messages_Dialogs.count;
                                        Log.e("CHK","Limit: "+limit);

                    }
//                    dialogs.addAll(org_telegram_tgnet_TLRPC_messages_Dialogs.chats);
                    if ((dialogs.size() < z || myDialogs.size() != 0) && z != 0) {
                        offset = dialogs.size() - 1;
                        Log.e("CHK","offset: "+offset);
                        checkChannelsV2(dsf,dialogsActivity);
                        return;
                    }
                    Log.e("CHK","Done: "+dialogs.size());
                    getJoined(dialogsActivity);
                }
            }
        });
    }

    private static void getJoined(final DialogsActivity dialogsActivity){
        getJoinedChannels(new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
//                Log.e("CHK","Error: "+error);
                if(!error){
                    JSONArray channelsId = null;
                    try {
                        channelsId = data.getJSONArray("data");
                        int size = channelsId.length();
                        HashMap<Integer,Channel> lastChannels = new HashMap<Integer, Channel>();
                        HashMap<String, Channel> lastChannelsByName = new HashMap<String, Channel>();
                        for(int i=0 ; i < size ; i++) {
                            JSONObject item = channelsId.getJSONObject(i);
                            Channel currentChannel = new Channel(item.getString("name"), item.getInt("tg_id"));
                            lastChannels.put((int)currentChannel.id,currentChannel);
                            lastChannelsByName.put(currentChannel.name, currentChannel);
                        }
                        /*for (TLRPC.Chat dialog:dialogs){
                            *//*if(dialog instanceof TLRPC.TL_dialogChannel){
                                long id = -dialog.id;
                                if(id == 0){
                                    id = dialog.peer.channel_id;
                                }
                            }*//*
                            Channel left = lastChannels.remove(dialog.id);
                            if(left == null){
                                Log.e("CHK","Not In Lis: "+dialog.username+" ,ID: "+dialog.id);
                                left = lastChannelsByName.get(dialog.username);
                                if(left != null){
                                    lastChannels.remove((int)left.id);
                                    Log.e("CHK","But Has Name: "+dialog.username+" ,ID: "+dialog.id);
                                }
                            }
                            }*/
                        if (lastChannels.size() > 0) {

                            /*for(Channel ch : lastChannels.values()){
                                for(TLRPC.Chat dl: dialogs){
                                    if(dl.username != null && dl.username.equals(ch.name)){
                                        Log.e("CHK","Left Name: "+dl.username+" ,ID: "+dl.id);
                                        lastChannels.remove((int)lastChannelsByName.get(dl.username).id);
                                    }
                                }
                            }*/
                        if(lastChannels.size() > 0) {
                            Log.e("CHK","left: "+lastChannels.size());
//                        if(true) {
//                            Log.e("CHK","Create");
                            String [] items = new String[lastChannels.size()];
                            final ArrayList<Channel> ids = new ArrayList<>();
                            int i = 0;
                            for(Channel channel:lastChannels.values()){
                                items[i] = channel.name;
                                ids.add(channel);
                                i++;
                            }
                            final boolean[] checkedItems = new boolean[items.length];
                            AlertDialog.Builder builder = null;
                            builder = new AlertDialog.Builder(dialogsActivity.getParentActivity());
//                            }
                            int coin = items.length * 2;
                                builder.setTitle("شما کانال های زیر را ترک کردید." + " در صورت خروج از هر کدام 2 سکه از شما کم می شود.");

                                for (int j = 0; j < checkedItems.length; j++) {
                                    checkedItems[j] = true;
                                }
                            builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    checkedItems[which] = isChecked;
                                }
                            });


                            builder.setPositiveButton(LocaleController.getString("ReJoin", R.string.ReJoin), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    reJoin(ids, checkedItems, new OnJoinSuccess() {
                                        @Override
                                        public void OnResponse(boolean ok) {
                                            if (visibleDialog != null) {
                                                visibleDialog.dismiss();
                                                visibleDialog = null;
                                            }

                                            if(!ok){
                                                Toast.makeText(context,"خطا در بازیابی کانال ها",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                            /*builder.setNeutralButton(LocaleController.getString("SelectAll", R.string.SelectAll), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int id) {
                                    *//*for (int j = 0 ; j < checkedItems.length ; j++){
                                        checkedItems[j]=true;
                                    }*//*
                                    ListView list = ((AlertDialog) dialogInterface).getListView();
                                    for (int i=0; i < list.getCount(); i++) {
                                        list.setItemChecked(i, true);
                                    }
                                }
                            });*/
                            builder.setNegativeButton(LocaleController.getString("LeftAll", R.string.LeftAll), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    for (int j = 0 ; j < checkedItems.length ; j++){
                                        checkedItems[j]=false;
                                    }
                                    reJoin(ids, checkedItems, new OnJoinSuccess() {
                                        @Override
                                        public void OnResponse(boolean ok) {
                                            if (visibleDialog != null) {
                                                visibleDialog.dismiss();
                                                visibleDialog = null;
                                            }
                                            if(!ok){
                                                Toast.makeText(context,"خطا در بازیابی کانال ها",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                            builder.setCancelable(false);


                            builder.setOnKeyListener(new Dialog.OnKeyListener() {

                                @Override
                                public boolean onKey(DialogInterface arg0, int keyCode,
                                                     KeyEvent event) {
                                    // TODO Auto-generated method stub
                                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                                        if (x < 1) {
                                            Toast.makeText(context, LocaleController.getString("backClickAgain", R.string.backClickAgain), Toast.LENGTH_SHORT).show();
                                            x++;
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    x = 0;
                                                }
                                            }, 1000);
                                            return true;

                                        } else {
                                            if (context instanceof Activity) {
                                                ((Activity) context).finish();
                                            }
                                            return true;
                                        }

                                    }
                                    return false;
                                }
                            });


                            dialogsActivity.showDialogNoCancel(builder.create());
                            /*AlertDialog dialog = builder.show();

                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            dialog.show();*/
//                            System.out.println("Left Channels: "+ Arrays.toString(lastChannels.toArray()));
                        }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public static void checkChannels(final ArrayList<TLRPC.Dialog> dsf,final DialogsActivity dialogsActivity){
        Log.e("CHK","Start Checking Channels ...");


        TLRPC.TL_messages_getDialogs req = new TLRPC.TL_messages_getDialogs();
        req.limit = 100;
        req.offset_date = (int) (System.currentTimeMillis()/1000);
        boolean found = false;
        for (int a = dialogs.size() - 1; a >= 0; a--) {
//        for (int a = 0 ; a < dialogs.size(); a++) {
            TLRPC.Dialog dialog = dialogs.get(a);
            int lower_id = (int) dialog.id;
            int high_id = (int) (dialog.id >> 32);
            if (lower_id != 0 && high_id != 1 && dialog.top_message > 0) {
                MessageObject message = MessagesController.getInstance().dialogMessage.get(dialog.id);
                req.offset_date = dialog.last_message_date;
                break;
               /* if (message != null && message.getId() > 0) {
                    req.offset_date = Math.max(dialog.last_message_date_i, message.messageOwner.date);
//                    req.offset_id = message.messageOwner.id;
                    int id;
                    if (message.messageOwner.to_id.channel_id != 0) {
                        id = -message.messageOwner.to_id.channel_id;
                    } else if (message.messageOwner.to_id.chat_id != 0) {
                        id = -message.messageOwner.to_id.chat_id;
                    } else {
                        id = message.messageOwner.to_id.user_id;
                    }
//                    req.offset_peer = MessagesController.getInstance().getInputPeer(id);
                    found = true;
                    break;
                }*/
            }
        }
        req.offset_peer = new TLRPC.TL_inputPeerEmpty();
        if (!found) {
        }
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(TLObject response, TLRPC.TL_error error) {
                if (error == null) {
                    final TLRPC.messages_Dialogs dialogsRes = (TLRPC.messages_Dialogs) response;
                    final HashMap<Integer, TLRPC.Chat> chatsDict = new HashMap<>();
                    final HashMap<Long, TLRPC.Dialog> new_dialogs_dict = new HashMap<>();
                    final HashMap<Long, MessageObject> new_dialogMessage = new HashMap<>();
                    final HashMap<Integer, Integer> notImportantDates = new HashMap<>();
                    final HashMap<Integer, TLRPC.User> usersDict = new HashMap<>();

                    for (int a = 0; a < dialogsRes.users.size(); a++) {
                        TLRPC.User u = dialogsRes.users.get(a);
                        usersDict.put(u.id, u);
                    }
                    for (int a = 0; a < dialogsRes.chats.size(); a++) {
                        TLRPC.Chat c = dialogsRes.chats.get(a);
                        chatsDict.put(c.id, c);
                        total_chatsDict.put(c.id, c);
                    }
                    for (int a = 0; a < dialogsRes.messages.size(); a++) {
                        TLRPC.Message message = dialogsRes.messages.get(a);
                        if (message.to_id.channel_id != 0) {
                            if (!MessageObject.isImportant(message)) {
                                notImportantDates.put(-message.to_id.channel_id, message.date);
                            }
                            TLRPC.Chat chat = chatsDict.get(message.to_id.channel_id);
                            if (chat != null && chat.left/* && !chat.megagroup*/) {
                                continue;
                            }
                            if (chat != null && chat.megagroup) {
                                message.flags |= TLRPC.MESSAGE_FLAG_MEGAGROUP;
                            }
                        } else if (message.to_id.chat_id != 0) {
                            TLRPC.Chat chat = chatsDict.get(message.to_id.chat_id);
                            if (chat != null && chat.migrated_to != null) {
                                continue;
                            }
                        }
                        if (message.post && !message.out) {
                            message.media_unread = true;
                        }
                        MessageObject messageObject = new MessageObject(message, usersDict, chatsDict, false);
                        MessageObject currentMessage = new_dialogMessage.get(messageObject.getDialogId());
                        if (currentMessage == null || messageObject.isMegagroup() || messageObject.isImportant()) {
                            new_dialogMessage.put(messageObject.getDialogId(), messageObject);
                        }
                    }
                    for (int a = 0; a < dialogsRes.dialogs.size(); a++) {
                        TLRPC.Dialog d = dialogsRes.dialogs.get(a);
                        if (d.id == 0 && d.peer != null) {
                            if (d.peer.user_id != 0) {
                                d.id = d.peer.user_id;
                            } else if (d.peer.chat_id != 0) {
                                d.id = -d.peer.chat_id;
                            } else if (d.peer.channel_id != 0) {
                                d.id = -d.peer.channel_id;
                            }
                        }
                        if (d.id == 0) {
                            continue;
                        }
                        if (d.last_message_date == 0) {
                            MessageObject mess = new_dialogMessage.get(d.id);
                            if (mess != null) {
                                d.last_message_date = mess.messageOwner.date;
                            }
                        }
                        if (d.last_message_date_i == 0 && d.top_not_important_message != 0) {
                            Integer date = notImportantDates.get((int) d.id);
                            if (date != null) {
                                d.last_message_date_i = date;
                            }
                        }
                        if (d instanceof TLRPC.TL_dialogChannel) {
                            TLRPC.Chat chat = chatsDict.get(-(int) d.id);
                            if (chat != null && chat.megagroup) {
                                d.top_message = Math.max(d.top_message, d.top_not_important_message);
                                d.unread_count = Math.max(d.unread_count, d.unread_not_important_count);
                            }
                            if (chat != null && chat.left/* && !chat.megagroup*/) {
                                continue;
                            }
                        } else if ((int) d.id < 0) {
                            TLRPC.Chat chat = chatsDict.get(-(int) d.id);
                            if (chat != null && chat.migrated_to != null) {
                                continue;
                            }
                        }
                        new_dialogs_dict.put(d.id, d);
                    }
                    boolean added = false;
                    for (HashMap.Entry<Long, TLRPC.Dialog> pair : new_dialogs_dict.entrySet()) {
                        Long key = pair.getKey();
                        TLRPC.Dialog value = pair.getValue();
                        TLRPC.Dialog currentDialog = dialogs_dict.get(key);
                        if(currentDialog == null){
                            added = true;
                            dialogs_dict.put(key, value);
                        }
                    }
                    dialogs.clear();
                    dialogs.addAll(dialogs_dict.values());
                    Collections.sort(dialogs, new Comparator<TLRPC.Dialog>() {
                        @Override
                        public int compare(TLRPC.Dialog tl_dialog, TLRPC.Dialog tl_dialog2) {
                            if (tl_dialog.last_message_date == tl_dialog2.last_message_date) {
                                return 0;
                            } else if (tl_dialog.last_message_date < tl_dialog2.last_message_date) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    if(added){
                        Log.e("CHK","Added: "+dialogs_dict.size());
                        checkChannels(null, dialogsActivity);
                        return;
                    } else {
                    Log.e("CHK","Done With: "+dialogs.size());

                    if(dialogs != null && dialogs.size() > 0)
                        getJoinedChannels(new OnResponseReadyListener() {
                            @Override
                            public void OnResponseReady(boolean error, JSONObject data, String message) {
//                Log.e("CHK","Error: "+error);
                                if(!error){
                                    JSONArray channelsId = null;
                                    try {
                                        channelsId = data.getJSONArray("data");
                                        int size = channelsId.length();
                                            HashMap<Integer, Channel> lastChannels = new HashMap<Integer, Channel>();
                                            HashMap<String, Channel> lastChannelsByName = new HashMap<String, Channel>();
                                        for(int i=0 ; i < size ; i++) {
                                            JSONObject item = channelsId.getJSONObject(i);
                                            Channel currentChannel = new Channel(item.getString("name"), item.getInt("tg_id"));
                                                lastChannels.put((int) currentChannel.id, currentChannel);
                                                lastChannelsByName.put(currentChannel.name, currentChannel);
                                        }

                                            for (TLRPC.Dialog dialog : dialogs) {
                                            /*if(dialog instanceof TLRPC.TL_dialogChannel){
                                                long id = -dialog.id;
                                if(id == 0){
                                    id = dialog.peer.channel_id;
                                }
                                            }*/
                                                Channel left = lastChannels.remove(-(int) dialog.id);
                                                if (left == null) {
                                                    Log.e("CHK", "Not In Lis: " + dialog.id);
                                                    if (dialog instanceof TLRPC.TL_dialogChannel) {
                                                        TLRPC.Chat chat = total_chatsDict.get(-(int) dialog.id);
                                                        if (chat != null) {
                                                            left = lastChannelsByName.get(chat.username);
                                                            Log.e("CHK", "Chat: " + chat.username + " ,ID: " + chat.id);
                                                            if (left != null) {
                                                                lastChannels.remove((int) left.id);
                                                                Log.e("CHK", "But Has Name: " + chat.username + " ,ID: " + chat.id);
                                                            }
                                                        }
                                                    }
                                                }
                                        }
                                        if(lastChannels.size() > 0) {
                                                Log.e("CHK", "Total Last: " + lastChannels.size());

                                                for (Channel ch : lastChannels.values()) {
                                                    for (TLRPC.Dialog dl : dialogs) {

                                                        if (dl instanceof TLRPC.TL_dialogChannel) {

                                                            TLRPC.Chat chat = total_chatsDict.get(-(int) dl.id);
                                                            if (chat != null && chat.username != null && chat.username.equals(ch.name)) {
                                                                Log.e("CHK", "Left Name: " + chat.username + " ,ID: " + chat.id);
                                                                lastChannels.remove((int) lastChannelsByName.get(chat.username).id);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (lastChannels.size() > 0) {
                                                Log.e("CHK", "Total Last 2: " + lastChannels.size());
//                        if(true) {
//                            Log.e("CHK","Create");
                                            String [] items = new String[lastChannels.size()];
                                            final ArrayList<Channel> ids = new ArrayList<>();
                                            int i = 0;
                                            for(Channel channel:lastChannels.values()){
                                                items[i] = channel.name;
                                                ids.add(channel);
                                                i++;
                                            }
                                            final boolean[] checkedItems = new boolean[items.length];
                                            AlertDialog.Builder builder = null;
                                            builder = new AlertDialog.Builder(dialogsActivity.getParentActivity());
//                            }
                                            int coin = items.length * 2;
                                                builder.setTitle("شما کانال های زیر را ترک کردید." + " در صورت خروج از هر کدام 2 سکه از شما کم می شود.");

                                                for (int j = 0; j < checkedItems.length; j++) {
                                                    checkedItems[j] = true;
                                                }

                                builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        checkedItems[which] = isChecked;
                                    }
                                });


                                builder.setPositiveButton(LocaleController.getString("ReJoin", R.string.ReJoin), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    reJoin(ids, checkedItems, new OnJoinSuccess() {
                                                        @Override
                                                        public void OnResponse(boolean ok) {
                                                    if (visibleDialog != null) {
                                                        visibleDialog.dismiss();
                                                        visibleDialog = null;
                                                    }

                                                            if(!ok){
                                                                Toast.makeText(context,"خطا در بازیابی کانال ها",Toast.LENGTH_SHORT).show();
                                                            }
                                                }
                                            });
                                    }
                                });
                            /*builder.setNeutralButton(LocaleController.getString("SelectAll", R.string.SelectAll), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int id) {
                                    *//*for (int j = 0 ; j < checkedItems.length ; j++){
                                        checkedItems[j]=true;
                                    }*//*
                                    ListView list = ((AlertDialog) dialogInterface).getListView();
                                    for (int i=0; i < list.getCount(); i++) {
                                        list.setItemChecked(i, true);
                                    }
                                }
                            });*/
                                builder.setNegativeButton(LocaleController.getString("LeftAll", R.string.LeftAll), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                            for (int j = 0 ; j < checkedItems.length ; j++){
                                                checkedItems[j]=false;
                                            }
                                            reJoin(ids, checkedItems, new OnJoinSuccess() {
                                                @Override
                                                public void OnResponse(boolean ok) {
                                                if (visibleDialog != null) {
                                                    visibleDialog.dismiss();
                                                    visibleDialog = null;
                                                    }
                                                    if(!ok){
                                                        Toast.makeText(context,"خطا در بازیابی کانال ها",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    }
                                });
                                builder.setCancelable(false);


                                builder.setOnKeyListener(new Dialog.OnKeyListener() {

                                    @Override
                                    public boolean onKey(DialogInterface arg0, int keyCode,
                                                         KeyEvent event) {
                                        // TODO Auto-generated method stub
                                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                                            if (x < 1) {
                                                Toast.makeText(context, LocaleController.getString("backClickAgain", R.string.backClickAgain), Toast.LENGTH_SHORT).show();
                                                x++;
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        x = 0;
                                                    }
                                                }, 1000);
                                                return true;

                                            } else {
                                                                dialogsActivity.getParentActivity().onBackPressed();
                                                    /*if (context instanceof Activity) {
                                                    ((Activity) context).finish();
                                                    }*/
                                                return true;
                                            }

                                        }
                                        return false;
                                    }
                                });


                                dialogsActivity.showDialogNoCancel(builder.create());
                            /*AlertDialog dialog = builder.show();

                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            dialog.show();*/
//                            System.out.println("Left Channels: "+ Arrays.toString(lastChannels.toArray()));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                }
            }
            }
        });

    }

    public static void reJoin(ArrayList<Channel> ids, boolean[] checkedItems,final OnJoinSuccess onJoinSuccess ){
        final JSONArray channelsToLeft = new JSONArray();
        ArrayList<Channel> channelsToReJoin = new ArrayList<>();
        int size = ids.size();
        int reJoinSize = 0;
        for(int i = 0 ; i<size ; i++){
            if(checkedItems[i]){
                channelsToReJoin.add(ids.get(i));
                reJoinSize++;
            }
            else {

                channelsToLeft.put(ids.get(i).id);
            }
        }
            final int joins = reJoinSize;
            for(final Channel ch: channelsToReJoin){
                            join(ch, new OnResponseReadyListener() {
                                int joinCompletes = 0;
                                int steps = 0;
                                @Override
                                public void OnResponseReady(boolean error, JSONObject data, String message) {
                                    steps ++;
                                    if(!error){
                                        joinCompletes++;

                                    }
                                    if (steps == joins) {

                                        if(joinCompletes == joins) {
                                            try {
//                                                Log.e("LEFT",channelsToLeft.toString(2));
                                                API.getInstance().post("/channels/leftAll", channelsToLeft.toString(2), new OnResponseReadyListener() {
                                                    @Override
                                                    public void OnResponseReady(boolean error, JSONObject data, String message) {
                                                        if (!error) {
                                                            loadCoins(data);
                                                        }
                                                        onJoinSuccess.OnResponse(!error);
                                                    }
                                                });

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else {
                                            onJoinSuccess.OnResponse(false);
                                        }

                                    }
                                }
                            },false);

                        }

        if(reJoinSize == 0){
            try {
//                Log.e("LEFT",channelsToLeft.toString(2));
                API.getInstance().post("/channels/leftAll", channelsToLeft.toString(2), new OnResponseReadyListener() {
                    @Override
                    public void OnResponseReady(boolean error, JSONObject data, String message) {
                        if(!error){
                            loadCoins(data);
                        }
                        onJoinSuccess.OnResponse(!error);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public static void addChannel(final TLRPC.Chat channel, int count){
        int channelId = channel.id;
            API.getInstance().post(String.format(Locale.ENGLISH, "/channels/add/%d/%s/%d", channelId, channel.username, count), "", new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                    if(error){
                        Toast.makeText(context, message,Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(context, channel.username+" اضافه شد",Toast.LENGTH_LONG).show();
                        loadCoins(data);

                    }
                }
            });


    }

    public static void addMyChannel(final Channel channel,final OnResponseReadyListener onJoinSuccess){
        int channelId = (int)channel.id;
        JSONObject js = new JSONObject();
        try {
            js.put("title",channel.title);
//        String body = "{\"title\":\""+channel.title+"\"}";
            API.getInstance().post(String.format(Locale.ENGLISH, "/channels/addMy/%d/%s", channelId, channel.name), js.toString(0), onJoinSuccess);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public static void updateChannel(final Channel channel,  final Bitmap bitmap){
        int channelId = (int)channel.id;
        JSONObject js = new JSONObject();
        try {
            js.put("title",channel.title);
            js.put("byteString",FileConvert.getStringFromBitmap(bitmap));
//        String body = "{\"title\":\""+channel.title+"\"}";
//            API.getInstance().post(String.format(Locale.ENGLISH, "/channels/addMy/%d/%s", channelId, channel.name), js.toString(0), onJoinSuccess);

            API.getInstance().post(String.format(Locale.ENGLISH, "/channels/update/%s", channel.name),js.toString() , new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
//                Log.e("UPDT",channel.name+": "+!error);
                    if(!error){
                        channel.hasPhoto = true;
                        channel.byteString = "http://www.membergiri.ir/api/tg/img/"+channel.id+".jpg";
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        String body;
//            body = "{\"byteString\":\""+ FileConvert.getStringFromBitmap(bitmap)+"\"}";



    }
    public static void migrateChannel(final Channel channel){
        int channelId = (int)channel.id;

        String body;
            body = "{\"name\":\""+ channel.name+"\",\"id\":"+channelId+"}";

        API.getInstance().post("/channels/migrate",body , new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
//                Log.e("UPDT",channel.name+": "+!error);
            }
        });


    }
    public static void addChannel(final Channel channel, int count, final Bitmap bitmap, final DialogsActivity dialogsActivity, final MyChannelFragment myChannelFragment){
        int channelId = (int)channel.id;
        TLRPC.FileLocation loc = channel.photo;

        String body = "";
        if(channel.hasPhoto){
            body = "";
        }
        else if(bitmap != null){
            body = "{\"byteString\":\""+ FileConvert.getStringFromBitmap(bitmap)+"\"}";
        }
        API.getInstance().post(String.format(Locale.ENGLISH, "/channels/add/%d/%s/%d", channelId, channel.name, count),body , new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {

                AlertDialog.Builder builder ;
                builder = new AlertDialog.Builder(dialogsActivity.getParentActivity());

                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                if(error){
//                    Toast.makeText(context, message,Toast.LENGTH_LONG).show();
                    builder.setMessage(message);
                }
                else {
//                    Toast.makeText(context, channel.title+" اضافه شد",Toast.LENGTH_LONG).show();
                    loadCoins(data);

                    builder.setMessage(LocaleController.getString("MemberBegirAlert", R.string.MemberBegirAlert));

                }
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                builder.setCancelable(true);
                dialogsActivity.showDialog(builder.create());
                myChannelFragment.setLoader(View.GONE);
            }
        });


    }
    public static void addChannel(final Channel channel, final Bitmap bitmap){
//        Log.d("COMMAND","AddChannel Triggerd");
        final int channelId = (int) channel.id;
        AlertDialog.Builder builder ;
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            builder = new AlertDialog.Builder(context);
        }
        else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(context, R.style.MyDialog);
        }
        else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(LocaleController.getString("MemberBegirTitle", R.string.MemberBegirTitle));

                            /*builder.setItems(Defaults.MEMBERS_COUNT , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Commands.addChannel(chat,Integer.parseInt(Defaults.MEMBERS_COUNT[which]));
                                }
                            });*/
        ReserveAdapter reserveAdapter = new ReserveAdapter(context,R.layout.adapter_buy_coin,channel);
        reserveAdapter.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Log.d("COMMAND","OnClick Triggerd 2");
                final int count = which;
                Commands.addChannel(channel,count,bitmap,null,null);
                if (visibleDialog != null) {
                    visibleDialog.dismiss();
                    visibleDialog = null;
                }
            }
        });
        builder.setAdapter(reserveAdapter, null);
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.setCancelable(true);
        showAlertDialog(builder.create());




    }

    public static void removeChannel(final Channel channel,final OnJoinSuccess onJoinSuccess){
        int channelId = (int) channel.id;
        API.getInstance().post(String.format(Locale.ENGLISH, "/channels/remove/%d", channelId), "", new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                onJoinSuccess.OnResponse(!error);
                if(error){
                    Toast.makeText(context, message,Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(context, channel.title+" حذف شد",Toast.LENGTH_LONG).show();
                    loadCoins(data);

                }
            }
        });


    }


    public static void addPost(MessageObject message, int count){

        int id = message.getId();
        int channel_id = message.messageOwner.to_id.channel_id;
        TLRPC.Chat chat = MessagesController.getInstance().getChat(channel_id);
//        long access_hash = chat.access_hash;
        CharSequence text = message.caption != null? message.caption:(message.messageText != null ?message.messageText:"None");
        String data = "{\"text\":\""+text+"\"}";
        final String channel_name = chat.username;

            API.getInstance().post(String.format(Locale.ENGLISH,"/posts/add/%s/%d/%d",channel_name,id,count),data, new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                    if(error){
                        Toast.makeText(context, message,Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(context, "پست شما از "+channel_name+" اضافه شد",Toast.LENGTH_LONG).show();
                        loadCoins(data);
                    }
                }
            });



    }

    private static void loadCoins(JSONObject data){
        boolean isView = false;
        String key = "joinCoins";
        try {
            data = data.getJSONObject("data");
            if(data.has("viewCoins")){
                key = "viewCoins";
                isView = true;
            }
            else if(data.has("joinCoins")){
                key = "joinCoins";
                isView = false;
            }
            else {
                return;
            }
            int coins = data.getInt(key);
            if(isView){
                ApplicationLoader.setViewCoins(coins);
            }
            else{
                ApplicationLoader.setJoinCoins(coins);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void loadCoins(final DialogsActivity dialogsActivity, final OnCoinsReady onCoinsReady){
//        if( ApplicationLoader.joinCoins == 0){
            ApplicationLoader.setJoinCoins(Defaults.getInstance().getMyCoin(),true);
            API.getInstance().run(String.format(Locale.ENGLISH,"/coin"), new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                    if(error){
                        onCoinsReady.onCoins(0,0);
                    }
                    else {
                        try {
                            data = data.getJSONObject("data");
                            int viewCoins = data.getInt("viewCoins");
                            int joinCoins = data.getInt("joinCoins");
                            Defaults.getInstance().setMyCoin(joinCoins);

//                            ApplicationLoader.setViewCoins(viewCoins);
//                            ApplicationLoader.setJoinCoins(joinCoins);
                            onCoinsReady.onCoins(viewCoins, joinCoins);
                            if(dialogsActivity != null && data.has("messageID") ){
//                                    Log.e("log","Yess");
                                if(Defaults.getInstance().isNewMessage(data.getInt("messageID"))) {
//                                    Log.e("log","NEWWWW");
                                    if(data.has("message")) {
                                        AlertDialog.Builder builder  = new AlertDialog.Builder(dialogsActivity.getParentActivity());
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        builder.setMessage(data.getString("message"));

                                        builder.setPositiveButton("تایید", null);
                                        dialogsActivity.showDialog(builder.create());
                                    }
                                }

                            }
//                            Log.e("log","NO");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        /*}
        else {
            ApplicationLoader.setJoinCoins(Defaults.getInstance().getMyCoin(),true);
            onCoinsReady.onCoins(ApplicationLoader.viewCoins,ApplicationLoader.joinCoins);
        }*/


    }

    public static void getNewChannels(OnResponseReadyListener listener){
        String one = "";
        /*if(Defaults.getInstance().showTaki()){
            one = "/one";
        }*/
        API.getInstance().run(String.format(Locale.ENGLISH,"/channels"+one),listener);
    }

    public static void getMyChannels(OnResponseReadyListener listener){
        API.getInstance().run(String.format(Locale.ENGLISH,"/channels/getMy"),listener);
    }

    public static void getJoinedChannels(OnResponseReadyListener listener){
        API.getInstance().run(String.format(Locale.ENGLISH,"/channels/self"),listener);
    }

    public static void getHistory(OnResponseReadyListener listener){
        API.getInstance().run(String.format(Locale.ENGLISH,"/user/history"),listener);
    }

    public static void getCompInfo(OnResponseReadyListener listener){
        API.getInstance().run(String.format(Locale.ENGLISH,"/user/comp/get"),listener);
    }

    public static void reg4Comp(final OnResponseReadyListener listener){
        API.getInstance().run(String.format(Locale.ENGLISH, "/user/comp/reg"), new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if(!error){
                    loadCoins(data);
                }
                listener.OnResponseReady(error,data,message);
            }
        });
    }

    public static void getWinners(OnResponseReadyListener listener){
        API.getInstance().run(String.format(Locale.ENGLISH,"/user/comp/winners"),listener);
    }

    public static Dialog showAlertDialog(AlertDialog dialog) {
//        Log.d("COMMAND","Show Alert");
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            visibleDialog = dialog;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                visibleDialog.getListView().setDivider(context.getDrawable(R.drawable.transparent));
            }
            else {
                visibleDialog.getListView().setDivider(context.getResources().getDrawable(R.drawable.transparent));
            }
            visibleDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            visibleDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            visibleDialog.setCanceledOnTouchOutside(false);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    visibleDialog = null;
                }
            });
            FontManager.instance().setTypefaceImmediate(visibleDialog.getCurrentFocus());
            visibleDialog.show();
            return visibleDialog;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return null;
    }

    public static void doTapsell(int shitil, final OnResponseReadyListener success) {
        API.getInstance().post(String.format(Locale.ENGLISH, "/coin/shitil/%d", shitil), "", new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if (!error) {

                    loadCoins(data);
                    success.OnResponseReady(false, data, message);
                } else {
                    success.OnResponseReady(true, null, message);
                    Toast.makeText(ApplicationLoader.applicationContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void doAdPlay(int shitil, final OnResponseReadyListener success) {
        API.getInstance().post(String.format(Locale.ENGLISH, "/coin/adplay/%d", shitil), "", new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                if (!error) {

                    loadCoins(data);
                    success.OnResponseReady(false, data, message);
                } else {
                    success.OnResponseReady(true, null, message);
                    Toast.makeText(ApplicationLoader.applicationContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public static void checkVersion(final DialogsActivity dialogsActivity){
        try {
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            API.getInstance().run("/checkVersion/" + versionCode+"/"+ BuildVars.MARKET_NAME, new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                    if(!error){
                        try {
                            data = data.getJSONObject("data");
                            /*data = new JSONObject();
                            data.put("ok",false);
                            data.put("link","http://google.com");
                            data.put("force",true);
                            data.put("changes","نسخه جدید آماده است");*/
                            boolean isOK = data.getBoolean("ok");
                            boolean force = false;
                            if(!isOK){
                                final String dlLink = data.getString("link");
                                String changes = data.getString("changes");
                                if(changes == null || changes.length() == 0){
                                    changes = "نسخه جدید برنامه آماده است.\nبرای دریافت آن روی گزینه دانلود کلیک کنید";
                                }
                                if(data.has("force")){
                                    force = data.getBoolean("force");
                                }
                                AlertDialog.Builder builder = null;
                                builder = new AlertDialog.Builder(dialogsActivity.getParentActivity());

                                builder.setTitle("آپدیت جدید");

                                builder.setMessage(changes);

                                builder.setPositiveButton("دانلود", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(dlLink));
                                        dialogsActivity.getParentActivity().startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.setCancelable(!force);
                                if(!force) {
                                    builder.setNegativeButton("بیخیال", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                                    dialogsActivity.showDialog(builder.create());
                                }
                                else {

                                    builder.setOnKeyListener(new Dialog.OnKeyListener() {

                                        @Override
                                        public boolean onKey(DialogInterface arg0, int keyCode,
                                                             KeyEvent event) {
                                            // TODO Auto-generated method stub
                                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                if (x < 1) {
                                                    Toast.makeText(context, LocaleController.getString("backClickAgain", R.string.backClickAgain), Toast.LENGTH_SHORT).show();
                                                    x++;
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            x = 0;
                                                        }
                                                    }, 1000);
                                                    return true;

                                                } else {
                                                    dialogsActivity.getParentActivity().onBackPressed();

                                                    return true;
                                                }

                                            }
                                            return false;
                                        }
                                    });
                                    dialogsActivity.showDialogNoCancel(builder.create());
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void sendLog(final String url, final String message){
        /*JSONObject js = new JSONObject();
        try {
            js.put("path",url);
            js.put("message",message);

            API.getInstance().post("/users/log",js.toString(0) , new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                Log.d("LOG",url+": "+message);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/

    }

}
