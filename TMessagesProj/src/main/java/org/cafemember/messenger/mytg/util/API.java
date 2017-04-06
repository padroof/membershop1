package org.cafemember.messenger.mytg.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.cafemember.messenger.mytg.Commands;
import org.json.JSONException;
import org.json.JSONObject;
import org.cafemember.messenger.ApplicationLoader;
import org.cafemember.messenger.mytg.listeners.OnResponseReadyListener;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class API {
    private static String SERVER_PATH = "http://www.membergiri.ir/api/tg";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static API instance;
    private Context context;
    private API(Context context){
        this.context = context;
    }

    public static API getInstance(){
        if(instance != null){
            return instance;
        }
        instance = new API(ApplicationLoader.applicationContext);
        return instance;
    }

            /*OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build();*/
    public void post(String route, String json, OnResponseReadyListener onResponseReadyListener){
    //    Log.e("API","Post Request For: "+route);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(getUrl(route))
                .post(body)
                .build();
        APITask task = new APITask(request, onResponseReadyListener);
        if(Build.VERSION.SDK_INT >= 11)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    public void post(String route, String json) {
        post(route, json, null);
    }

    public void run(String route) {
        run(route, null);
    }
    public void run(String route, OnResponseReadyListener onResponseReadyListener) {
       // Log.e("API","Get Request For: "+route);
        Request request = new Request.Builder()
                .url(getUrl(route))
                .build();

        APITask task = new APITask(request, onResponseReadyListener);
        if(Build.VERSION.SDK_INT >= 11)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();

    }

    private static String getUrl(String route){
        if(route.toLowerCase().contains("login") || route.toLowerCase().contains("register")){
            return SERVER_PATH+route;
        }
        return SERVER_PATH+route+"?token="+Defaults.getInstance().getMyToken();
    }

    class APITask extends AsyncTask<String, Void, String>{

        private  OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        private Request request;
        private OnResponseReadyListener onResponseReadyListener;
        APITask(Request request, OnResponseReadyListener onResponseReadyListener){
            this.request = request;
            this.onResponseReadyListener = onResponseReadyListener;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Response response = client.newCall(request).execute();
                ResponseBody body = response.body();
                String res = body.string();
                return res;
            }
            catch (ConnectException e){
//                System.out.println("Out 1");
                e.printStackTrace();
                return null;
            }
            catch (Exception e){
                e.printStackTrace();
//                Commands.sendLog(request.url().url().getPath(),e.getMessage());
// System.out.println("Out 2");

                return null;
            }


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
         //   Log.e("API","Response For: "+request.url());
            if(onResponseReadyListener == null){
                System.err.println("Response: "+s);
//                onResponseReadyListener.OnResponseReady(true,null,"خطا در ارتباط با سرور");
                return;
            }

            if(s == null){
//                Commands.sendLog(request.url().url().getPath(),"No Connection");
                onResponseReadyListener.OnResponseReady(true,null,"خطا در ارتباط با اینترنت");
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                if(jsonObject.has("dev")){
                    String dev = jsonObject.getString("dev");
//                    Commands.sendLog(request.url().url().getPath(),dev);
                    System.err.println("Dev Error: "+dev);
                }
                onResponseReadyListener.OnResponseReady(jsonObject.getBoolean("error"), jsonObject, jsonObject.getString("message"));
            } catch (JSONException e) {
//                Commands.sendLog(request.url().url().getPath(),e.getMessage());
                onResponseReadyListener.OnResponseReady(true,null,"خطا در ارتباط با سرور");
                e.printStackTrace();
            }
        }
    }
}
