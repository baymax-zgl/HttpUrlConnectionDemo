package com.hudun.myapplication;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpUrlConnectionUtil {
    private HttpUrlConnectionUtil() {

    }

    //创建线程池
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private OnHttpUtilListener onHttpUtilListener;

    private static class Holder {
        private static HttpUrlConnectionUtil INSTANCE = new HttpUrlConnectionUtil();
    }

    public static HttpUrlConnectionUtil getInstance() {
        return Holder.INSTANCE;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //成功
                    onHttpUtilListener.onSuccess((String) msg.obj);
                    break;
                case 2:
                    //错误
                    onHttpUtilListener.onError((String) msg.obj);
                    break;
                case 3:
                    break;
            }
        }
    };

    public void get(OnHttpUtilListener onHttpUtilListener, final String urlPath) {
        this.onHttpUtilListener = onHttpUtilListener;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    //获得URL对象
                    URL url = new URL(urlPath);
                    //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
                    connection = (HttpURLConnection) url.openConnection();
                    // 默认为GET
                    connection.setRequestMethod("GET");
                    //不使用缓存
                    connection.setUseCaches(false);
                    //设置超时时间
                    connection.setConnectTimeout(10000);
                    //设置读取超时时间
                    connection.setReadTimeout(10000);
                    //设置是否从httpUrlConnection读入，默认情况下是true;
                    connection.setDoInput(true);
                    //很多项目需要传入cookie解开注释（自行修改）
//                    connection.setRequestProperty("Cookie", "my_cookie");
                    //相应码是否为200
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        //获得输入流
                        inputStream = connection.getInputStream();
                        //包装字节流为字符流
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        //这块是获取服务器返回的cookie（自行修改）
//                        String cookie = connection.getHeaderField("set-cookie");
                        //通过handler更新UI
                        Message message = handler.obtainMessage();
                        message.obj = response.toString();
                        message.what = 1;
                        handler.sendMessage(message);
                    } else {

                        Message message = handler.obtainMessage();
                        message.obj = String.valueOf(connection.getResponseCode());
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.obj = e.getMessage();
                    message.what = 2;
                    handler.sendMessage(message);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    //关闭读写流
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        //加入线程池
        executorService.execute(runnable);
    }
    public void post(OnHttpUtilListener onHttpUtilListener, final String urlPath, final Map<String, String> params) {
        this.onHttpUtilListener = onHttpUtilListener;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                OutputStream outputStream = null;
                StringBuffer body = getParamString(params);
                byte[] data = body.toString().getBytes();
                try {
                    //获得URL对象
                    URL url = new URL(urlPath);
                    //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
                    connection = (HttpURLConnection) url.openConnection();
                    // 默认为GET
                    connection.setRequestMethod("POST");
                    //不使用缓存
                    connection.setUseCaches(false);
                    //设置超时时间
                    connection.setConnectTimeout(10000);
                    //设置读取超时时间
                    connection.setReadTimeout(10000);
                    //设置是否从httpUrlConnection读入，默认情况下是true;
                    connection.setDoInput(true);
                    //设置为true后才能写入参数
                    connection.setDoOutput(true);
                    //post请求需要设置标头
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Charset", "UTF-8");
                    //表单参数类型标头
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    //很多项目需要传入cookie解开注释（自行修改）
//                    connection.setRequestProperty("Cookie", "my_cookie");
                    //获取写入流
                    outputStream=connection.getOutputStream();
                    //写入表单参数
                    outputStream.write(data);
                    //相应码是否为200
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        //获得输入流
                        inputStream = connection.getInputStream();
                        //包装字节流为字符流
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        //这块是获取服务器返回的cookie（自行修改）
//                        String cookie = connection.getHeaderField("set-cookie");
                        //通过handler更新UI
                        Message message = handler.obtainMessage();
                        message.obj = response.toString();
                        message.what = 1;
                        handler.sendMessage(message);
                    } else {

                        Message message = handler.obtainMessage();
                        message.obj = String.valueOf(connection.getResponseCode());
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.obj = e.getMessage();
                    message.what = 2;
                    handler.sendMessage(message);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    //关闭读写流
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        //加入线程池
        executorService.execute(runnable);
    }

    //post请求参数
    private StringBuffer getParamString(Map<String, String> params){
        StringBuffer result = new StringBuffer();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> param = iterator.next();
            String key = param.getKey();
            String value = param.getValue();
            result.append(key).append('=').append(value);
            if (iterator.hasNext()){
                result.append('&');
            }
        }
        return result;
    }
    //回调接口
    interface OnHttpUtilListener {
        void onError(String e);

        void onSuccess(String json);
    }
}
