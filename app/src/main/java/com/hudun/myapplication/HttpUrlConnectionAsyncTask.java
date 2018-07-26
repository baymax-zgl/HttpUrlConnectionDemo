package com.hudun.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 括号里的类型
 * 第一个代表doInBackground方法需要传入的类型
 * 第二个代表onProgressUpdate方法需要传入的类型
 * 第一个代表onPostExecute方法需要传入的类型
 */
public class HttpUrlConnectionAsyncTask extends AsyncTask<Integer, Integer, String> {

    private Integer UPLOAD = 1;
    private Integer DOWNLOAD = 2;


    private OnHttpProgressUtilListener onHttpProgressUtilListener;
    private String urlPath;
    private String filePath;
    private byte[] data;
    public void uploadFileBlock(OnHttpProgressUtilListener onHttpProgressUtilListener, String urlPath, byte[] data) {
        this.urlPath = urlPath;
        this.data = data;
        //调用doInBackground方法（方法里面是异步执行）
        execute(UPLOAD);
    }

    public void uploadFile(OnHttpProgressUtilListener onHttpProgressUtilListener, String urlPath, String filePath) {
        this.urlPath = urlPath;
        this.filePath = filePath;
        //调用doInBackground方法（方法里面是异步执行）
        execute(UPLOAD);
    }


    public void downloadFile(OnHttpProgressUtilListener onHttpProgressUtilListener, String urlPath, String filePath) {
        this.urlPath = urlPath;
        this.filePath = filePath;
        //调用doInBackground方法（方法里面是异步执行）
        execute(2);
    }

    @Override
    protected String doInBackground(Integer... integers) {
        String result;
        if (integers[0].equals(UPLOAD)) {
            result = upload();
        } else if (integers[0].equals(DOWNLOAD)) {
            result = download();
        } else {
            result = uploadBlock();
        }
        return result;
    }

    private String upload() {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File file = new File(filePath);

        String result = "";
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
            //设置为true后才能写入参数
            connection.setRequestProperty("Content-Type", "multipart/form-data");
            connection.setRequestProperty("Content-Length", String.valueOf(file.length()));
            outputStream = new DataOutputStream(connection.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            int count = 0;
            // 计算上传进度
            Long progress = 0L;
            byte[] bufferOut = new byte[2048];
            while ((count = dataInputStream.read(bufferOut)) != -1) {
                outputStream.write(bufferOut, 0, count);
                progress += count;
                //换算进度
                double d = (new BigDecimal(progress / (double) file.length()).setScale(2, BigDecimal.ROUND_HALF_UP)).doubleValue();
                double d1 = d * 100;
                //传入的值为1-100
                onProgressUpdate((int) d1);
            }
            dataInputStream.close();
            //写入参数
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
                result = response.toString();
            } else {
                result = String.valueOf(connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            onCancelled(e.getMessage());
        } finally {
            //关闭
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private String uploadBlock() {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        String result = "";
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
            //设置为true后才能写入参数
            connection.setRequestProperty("Content-Type", "multipart/form-data");
            //这里需要改动
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            outputStream = new DataOutputStream(connection.getOutputStream());
            //这块需要改为ByteArrayInputStream写入流
            ByteArrayInputStream inputStreamByte = new ByteArrayInputStream(data);
            int count = 0;
            // 计算上传进度
            Integer progress = 0;
            byte[] bufferOut = new byte[2048];
            while ((count = inputStreamByte.read(bufferOut)) != -1) {
                outputStream.write(bufferOut, 0, count);
                progress += count;
                onProgressUpdate(progress);
            }
            inputStreamByte.close();
            //写入参数
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
                result = response.toString();
            } else {
                result = String.valueOf(connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            onCancelled(e.getMessage());
        } finally {
            //关闭
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private String download() {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String result = "";
        try {
            //获得URL对象
            URL url = new URL(urlPath);
            //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
            connection = (HttpURLConnection) url.openConnection();
            //建立实际链接
            connection.connect();
            inputStream = connection.getInputStream();
            //获取文件长度
            Double size = (double) connection.getContentLength();

            outputStream = new FileOutputStream(filePath);
            int count = 0;
            // 计算上传进度
            Long progress = 0L;
            byte[] bytes = new byte[2048];
            while ((count = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, count);
                //换算进度
                double d = (new BigDecimal(progress / size).setScale(2, BigDecimal.ROUND_HALF_UP)).doubleValue();
                double d1 = d * 100;
                //传入的值为1-100
                onProgressUpdate((int) d1);
            }
            result = "下载成功";
        } catch (Exception e) {
            e.printStackTrace();
            onCancelled(e.getMessage());
        } finally {
            //关闭
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        onHttpProgressUtilListener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        onHttpProgressUtilListener.onSuccess(s);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
        onHttpProgressUtilListener.onError(s);
    }

    interface OnHttpProgressUtilListener {
        void onError(String e);

        void onProgress(Integer length);

        void onSuccess(String json);
    }
}
