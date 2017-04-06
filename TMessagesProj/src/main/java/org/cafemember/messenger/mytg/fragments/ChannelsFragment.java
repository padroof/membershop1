package org.cafemember.messenger.mytg.fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.cafemember.messenger.AndroidUtilities;
import org.cafemember.messenger.ApplicationLoader;
import org.cafemember.messenger.ImageReceiver;
import org.cafemember.messenger.LocaleController;
import org.cafemember.messenger.mytg.listeners.MyAlarmReceiver;
import org.cafemember.messenger.mytg.listeners.OnCoinsReady;
import org.cafemember.messenger.mytg.listeners.Refrashable;
import org.cafemember.tgnet.TLRPC;
import org.cafemember.ui.Components.AvatarDrawable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.cafemember.messenger.R;
import org.cafemember.messenger.mytg.Channel;
import org.cafemember.messenger.mytg.Commands;
import org.cafemember.messenger.mytg.FontManager;
import org.cafemember.messenger.mytg.adapter.ChannelsAdapter;
import org.cafemember.messenger.mytg.listeners.OnChannelReady;
import org.cafemember.messenger.mytg.listeners.OnResponseReadyListener;
import org.cafemember.messenger.mytg.util.Defaults;
import org.cafemember.ui.DialogsActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Masoud on 6/2/2016.
 */
@SuppressLint("ValidFragment")
public class ChannelsFragment extends Fragment implements Refrashable, SwipeRefreshLayout.OnRefreshListener {
    private ChannelsAdapter adapter;

    private final DialogsActivity dialogsActivity;
    private LinearLayout loading;
    private ListView channelsLis;
    private View layout;
    private long lastCheck = 0;

//    private SwipeRefreshLayout swiper;
    private LinearLayout errorHolder;
    private LinearLayout one;
    private LinearLayout notfound;
    private boolean taki;

    TextView name ;
    CircleImageView image ;
    Button join;
    Button autojoin;
    Button refresh;
    ImageButton reload;
    Button report;
    TextView title ;
    ImageReceiver avatarImage;
    AvatarDrawable avatarDrawable;
    Channel showingChannel;
    private int loadTakiCount = 0;
    private int loadListCount = 0;
    private int lastSeen = 0;
    private int totalChannels = 0;
    private boolean channelReady = false;
    private boolean onCreate = false;
    private boolean isLoaded = false;
    private boolean isCreated = false;
    private boolean checkLeft = false;
    private  int nextF;
  public Handler handler;
  public Boolean automember = false;
  public Button canceljoin;

  @SuppressLint("ValidFragment")
    public ChannelsFragment(DialogsActivity dialogsActivity){
        this.dialogsActivity = dialogsActivity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.channels_layout, null);
        channelsLis = (ListView)layout.findViewById(R.id.channelsList);
        loading = (LinearLayout)layout.findViewById(R.id.holder);
        reload = (ImageButton)layout.findViewById(R.id.reload);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMore();
            }
        });
        loading.setVisibility(View.GONE);
//        swiper = (SwipeRefreshLayout) layout.findViewById(R.id.swip);
        one = (LinearLayout) layout.findViewById(R.id.one);
        notfound = (LinearLayout) layout.findViewById(R.id.notfound);
        errorHolder = (LinearLayout) layout.findViewById(R.id.error);

        adapter = new ChannelsAdapter(getContext(),R.layout.channel_item,new ArrayList<Channel>(),ChannelsFragment.this, dialogsActivity);
        channelsLis.setAdapter(adapter);
//        swiper.setOnRefreshListener(this);
//        swiper.setColorSchemeColors(R.color.colorPrimary,R.color.colorPrimaryDark);
        FontManager.instance().setTypefaceImmediate(layout);
        initTaki(layout);
        loadListCount++;
        checkLeft = false;
        return layout;

    }
    
    private void initTaki(View v){
        refresh = (Button)v.findViewById(R.id.refresh);
    name = (TextView)v.findViewById(R.id.name);
    title = (TextView)v.findViewById(R.id.title);
    image = (CircleImageView)v.findViewById(R.id.image);
    join = (Button)v.findViewById(R.id.join);
     autojoin = (Button)v.findViewById(R.id.btnautojoin);
     canceljoin = (Button)v.findViewById(R.id.btncanceljoin);
    report = (Button)v.findViewById(R.id.report);


        avatarImage = new ImageReceiver(v);
        avatarDrawable = new AvatarDrawable();
        avatarImage.setRoundRadius(AndroidUtilities.dp(26));
        int avatarLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 13 : 9);
        int avatarTop = AndroidUtilities.dp(10);
        avatarImage.setImageCoords(avatarLeft, avatarTop, AndroidUtilities.dp(52), AndroidUtilities.dp(52));

        FontManager.instance().setTypefaceImmediate(v);
        onCreate = true;
        loadTaki();
//        loadMore();
        
    }

    public void showChannel(int next,final Boolean auto){
        int size = adapter.getCount();

        if(next >= size){
//            loadListCount++;
            if(size == 0){
                one.setVisibility(View.GONE);
                notfound.setVisibility(View.VISIBLE);
                loadMore();
                return;
            }
            else {
                next = 0 ;

            }
        }

        final Channel showingChannel = adapter.getItem(next);
        adapter.remove(adapter.getItem(next));
        adapter.notifyDataSetChanged();
      notfound.setVisibility(View.GONE);
        one.setVisibility(View.VISIBLE);
        avatarDrawable.setInfo((int) showingChannel.id, showingChannel.name, null, showingChannel.id < 0);
        Bitmap bitmap = null;
        if(showingChannel.hasPhoto){
//            Log.e("MY",showingChannel.name+"Has Photo");
//            bitmap = showingChannel.getBitMap();
            showingChannel.setBitMap(image);
        }
        else {
//            Log.e("MY",showingChannel.name+"Has NOT Photo");
            if(showingChannel.photo != null) {
//                Log.e("MY",showingChannel.name+"Has Online");
                TLRPC.FileLocation photo = null;
                photo = showingChannel.photo;
                avatarImage.setImage(photo, "50_50", avatarDrawable, null, false);
                bitmap = avatarImage.getBitmap();
                if(bitmap != null) {
                    Commands.updateChannel(showingChannel, bitmap);
                }
            }

        }

        if(bitmap != null){
//            Log.e("MY",showingChannel.name+"Ready");
            image.setImageBitmap(bitmap);
        }
        /*else
        {
//            Log.e("MY",showingChannel.name+"Default");
            image.setImageResource(R.drawable.default_channel_icon);
        }*/
       /*
       sina - no click
       image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent telegram = new Intent(Intent.ACTION_VIEW , Uri.parse("https://telegram.me/"+ showingChannel.name));
                getContext().startActivity(telegram);
            }
        }); */
        name.setText(showingChannel.name);
        title.setText(showingChannel.title);
         nextF = next;
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChannel(nextF+1,false);
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("گزارش کانال");

// Set up the input
                final EditText input = new EditText(getContext());
                input.setHint("دلیل گزارش");
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        Commands.report((int) showingChannel.id, m_Text,new OnResponseReadyListener() {
                            @Override
                            public void OnResponseReady(boolean error, JSONObject data, String message) {
                                Toast.makeText(getContext(),error?"خطا در گزارش کانال":"کانال گزارش شد",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });
      canceljoin.setOnClickListener(new View.OnClickListener() {
          @Override
           public void onClick(View v) {
            if(automember)
            {
              automember = false;
              canceljoin.setEnabled(false);
            }

          }
         });
        autojoin.setOnClickListener(new View.OnClickListener() {
          @Override

          public void onClick(View v) {
            setLoader(View.VISIBLE);

            automember =true;
            canceljoin.setEnabled(true);
              Commands.join(showingChannel, new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                  setLoader(View.GONE);
                  if (!error) {
//                            loadMore();
                    Toast.makeText(getContext(), "عضویت با موفقیت انجام شد", Toast.LENGTH_LONG).show();
                            /*
                            sina - no open dialog
                            if(Defaults.getInstance().openOnJoin()){
                                dialogsActivity.showChannel(showingChannel.id);
                            }*/
                  } else {
                    if (message == null || message.length() == 0) {

                      Toast.makeText(getContext(), "خطا در عضویت کانال", Toast.LENGTH_LONG).show();

                    } else {
                      Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                    }

                  }
                  showChannel(nextF + 1, true);
                }
              });

            }

        });
          join.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  setLoader(View.VISIBLE);
                  Commands.join(showingChannel, new OnResponseReadyListener() {
                      @Override
                      public void OnResponseReady(boolean error, JSONObject data, String message) {
                          setLoader(View.GONE);
                          if (!error) {
//                            loadMore();
                              Toast.makeText(getContext(), "عضویت با موفقیت انجام شد", Toast.LENGTH_LONG).show();
                            /*
                            sina - no open dialog
                            if(Defaults.getInstance().openOnJoin()){
                                dialogsActivity.showChannel(showingChannel.id);
                            }*/
                          } else {
                              if (message == null || message.length() == 0) {

                                  Toast.makeText(getContext(), "خطا در عضویت کانال", Toast.LENGTH_LONG).show();

                              } else {
                                  Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                              }

                          }
                          showChannel(nextF + 1,false);
                      }
                  });
              }
          });
      if(auto)
      {
        if(automember) {
          Handler handler = new Handler();
          handler.postDelayed(new Runnable() {
            @Override
            public void run() {

              Commands.join(showingChannel, new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                  setLoader(View.GONE);
                  if (!error) {
//                            loadMore();
                     Toast.makeText(getContext(), "عضویت با موفقیت انجام شد", Toast.LENGTH_LONG).show();
                            /*
                            sina - no open dialog
                            if(Defaults.getInstance().openOnJoin()){
                                dialogsActivity.showChannel(showingChannel.id);
                            }*/
                  } else {
                    if (message == null || message.length() == 0) {

                      Toast.makeText(getContext(), "خطا در عضویت کانال", Toast.LENGTH_LONG).show();

                    } else {
                      Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                    }

                  }
                  showChannel(nextF + 1, true);

                }
              });
            }
          }, 2000);
        }
      }
    }
  public  void autojoin()
  {
    // int size = adapter.getCount();
    // for(int i=0;i<size;i++) {


    //  }
  }
    public void loadTaki(){
        taki = Defaults.getInstance().showTaki();
        if(taki){
//
//            one.setVisibility(View.VISIBLE);
            channelsLis.setVisibility(View.INVISIBLE);
            if(adapter.getCount() > 0){
                showChannel(0,false);
            }
            else {
                if(!onCreate){

                    loadMore();
                }
                else {
                    onCreate = false;
                }
            }

        }
        else {
            one.setVisibility(View.GONE);
            channelsLis.setVisibility(View.VISIBLE);
        }
//        loadMore();
    }

    public void loadMore(){
        /*if(!Defaults.getInstance().fetchAccess()){
            Toast.makeText(getContext(), LocaleController.getString("BlockAlert",R.string.BlockAlert),Toast.LENGTH_LONG).show();
            if(swiper.isRefreshing()){
                swiper.setRefreshing(false);
            }
            errorHolder.setVisibility(View.VISIBLE);
            one.setVisibility(View.GONE);
            adapter.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        boolean blocking = false;

            if(loadListCount >1){
                blocking = true;
                loadListCount = 0;
            }

        if(blocking){
            if(swiper.isRefreshing()){
                swiper.setRefreshing(false);
            }
            one.setVisibility(View.GONE);
            adapter.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), LocaleController.getString("BlockAlert",R.string.BlockAlert),Toast.LENGTH_LONG).show();
            errorHolder.setVisibility(View.VISIBLE);
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(getContext().ALARM_SERVICE);
            PendingIntent pi =PendingIntent.getBroadcast(getContext(),464,new Intent(getContext(), MyAlarmReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC,System.currentTimeMillis()+1*60*1000,pi);
            Defaults.getInstance().setFetchAccess(false);


            return;
        }*/


//        if(!swiper.isRefreshing()){
            loading.setVisibility(View.VISIBLE);
//        }
            Commands.getNewChannels(new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
//                    if(!swiper.isRefreshing()){
                        loading.setVisibility(View.GONE);
//                    }
//                    if(!checkLeft){

                        checkLeft = true;
//                    }
                if (!error) {
                        errorHolder.setVisibility(View.GONE);
                        try {
                            JSONArray channelsId = data.getJSONArray("data");
                            final int size = channelsId.length();
                            adapter.clear();
                            channelReady = false;
                            for (int i = 0; i < size; i++) {
                                JSONObject item = channelsId.getJSONObject(i);
                                final int cu = i;
                                final Channel currentChannel = new Channel(item.getString("name"), item.getInt("tg_id"));

                                String byteString = null;
                                if(item.has("title")  && item.getString("title").length() > 0){
                                    currentChannel.title = item.getString("title");
                                }
                                if(item.has("byteString")  && item.getString("byteString").length() > 0){
//                                    Log.e("MyCh",currentChannel.name+" Has Byte");
                                    byteString = item.getString("byteString");
                                    currentChannel.setPhoto(byteString);
                                        adapter.add(currentChannel);
                                    if (!channelReady) {
                                        channelReady = true;
                                        if (taki) {

                                            showChannel(0,false);
                                        }
                                    }
                                }
                                else {
                                    final int last = i;
                                    Defaults.getInstance().loadChannel(currentChannel, new OnChannelReady() {
                                        @Override
                                        public void onReady(Channel channel, boolean isOk) {
                                            if (isOk) {
                                                adapter.add(currentChannel);
                                                adapter.notifyDataSetChanged();
                                                if (!channelReady) {
                                                    channelReady = true;
                                                    if (taki) {

                                                        showChannel(0,false);
                                                    }
                                                }
                                            }

                                            if (last == size - 1 && !channelReady) {
                                                AlertDialog.Builder builder;
                                                builder = new AlertDialog.Builder(dialogsActivity.getParentActivity());

                                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));

                                                builder.setMessage(LocaleController.getString("BlockAlert2", R.string.BlockAlert2));
                                                builder.setNegativeButton("تایید", null);
                                                builder.setCancelable(true);
                                                dialogsActivity.showDialog(builder.create());
                                                errorHolder.setVisibility(View.VISIBLE);
//                                                Toast.makeText(getContext(), LocaleController.getString("BlockAlert2",R.string.BlockAlert2),Toast.LENGTH_LONG).show();
                                            }
                                                /*else {
                                                    currentChannel.title = currentChannel.name;
                                                    adapter.add(currentChannel);
                                                    adapter.notifyDataSetChanged();
                                                }*/


                                        }
                                    });
//                                }
                                }

                            }
                            adapter.notifyDataSetChanged();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        loadListCount--;
                        errorHolder.setVisibility(View.VISIBLE);
                    }
                    /*if(swiper.isRefreshing()){
                        swiper.setRefreshing(false);
                    }*/
                }
            });


    }

    public void setLoader(int visibility){
        loading.setVisibility(visibility);
    }


    @Override
    public void refresh() {
        if(!isLoaded && isCreated){
            isLoaded = true;
            loadMore();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isCreated= true;
        refresh();
    }

    @Override
    public void onRefresh() {
        loadListCount++;
//        loadMore();
    }
}
