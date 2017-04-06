package org.cafemember.messenger.mytg.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.cafemember.messenger.mytg.Channel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.cafemember.messenger.MessageObject;
import org.cafemember.messenger.R;
import org.cafemember.messenger.mytg.Commands;
import org.cafemember.messenger.mytg.FontManager;
import org.cafemember.messenger.mytg.listeners.OnJoinSuccess;
import org.cafemember.messenger.mytg.util.Defaults;
import org.cafemember.tgnet.TLRPC;

import java.util.ArrayList;

/**
 * Created by Masoud on 6/2/2016.
 */
public class ReserveAdapter extends ArrayAdapter<JSONObject> {


    private JSONArray  coins;
    int type = 1;
    private TLRPC.Chat chat;
    private Channel channel;
    private MessageObject message;
    private AlertDialog.Builder builder;
    private DialogInterface.OnClickListener onClickListener;
    public ReserveAdapter(Context context, int resource, Channel channel) {
        super(context, resource);
        this.channel = channel;
        coins = Commands.JoinCoins;

    }


    public void setOnClickListener(DialogInterface.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final JSONObject coin = getItem(position);
        CoinViewHolder viewHolder ;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.adapter_reserve_coin, parent, false);
            viewHolder = new CoinViewHolder();
            viewHolder.description = (TextView)v.findViewById(R.id.description);
            viewHolder.image = (ImageView)v.findViewById(R.id.image);
            viewHolder.buy = (Button)v.findViewById(R.id.buy);
            v.setTag(viewHolder);
        }

        else {
            viewHolder = (CoinViewHolder)v.getTag();
        }
            String typeString = "عضو";
            /*if(type == 1){
                viewHolder.image.setImageResource(R.drawable.contact_green);
                viewHolder.buy.setBackgroundResource(R.drawable.buy_member);
            }
            else if(type == 2){
                typeString = "بازدید";
                viewHolder.image.setImageResource(R.drawable.eye_blue);
                viewHolder.buy.setBackgroundResource(R.drawable.buy_view);
            }*/
        try {
            final int count = Integer.parseInt(coin.getString("count"));
            final String info = coin.getString("info");
//            final int count = Integer.parseInt(coin.getString("count"));
            String price = coin.getString("price");
//            int id = Integer.parseInt(coin.getString("id"));
            viewHolder.description.setText(info);
            viewHolder.buy.setText(price);
            viewHolder.buy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Log.d("COMMAND","OnClick Triggerd 1");
                        if(onClickListener != null){
                            onClickListener.onClick(null, count);
                        }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


        FontManager.instance().setTypefaceImmediate(v);
        return v;

    }

    @Override
    public JSONObject getItem(int position) {
        try {
            return coins.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getCount() {
        return coins.length();
    }

    public class CoinViewHolder {

        TextView description;
        ImageView image ;
        Button buy;
    }
}
