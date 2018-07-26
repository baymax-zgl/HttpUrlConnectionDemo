package com.hudun.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HttpUrlConnectionUtil.getInstance().get(new HttpUrlConnectionUtil.OnHttpUtilListener() {
            @Override
            public void onError(String e) {

            }

            @Override
            public void onSuccess(String json) {
                //请求得到的数据
            }
        }, "写入你的URL");


        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("user", "小明");
        hashMap.put("age", "60");
        HttpUrlConnectionUtil.getInstance().post(new HttpUrlConnectionUtil.OnHttpUtilListener() {
            @Override
            public void onError(String e) {

            }

            @Override
            public void onSuccess(String json) {
                //请求得到的数据
            }
        }, "写入你的URL", hashMap);

        new HttpUrlConnectionAsyncTask().uploadFile(new HttpUrlConnectionAsyncTask.OnHttpProgressUtilListener() {
            @Override
            public void onError(String e) {

            }

            @Override
            public void onProgress(Integer length) {
                //上传进度
            }

            @Override
            public void onSuccess(String json) {
                //上传完成服务器返回的数据
            }
        }, "上传URL", "上传文件地址");
        new HttpUrlConnectionAsyncTask().downloadFile(new HttpUrlConnectionAsyncTask.OnHttpProgressUtilListener() {
            @Override
            public void onError(String e) {

            }

            @Override
            public void onProgress(Integer length) {
                //下载进度
            }

            @Override
            public void onSuccess(String json) {
                //下载完成
            }
        }, "上传URL", "下载文件地址");
    }
    int chuncks=0;//流块
    double chunckProgress=0.0;//进度
    private void upload(){
        //每一块大小
        int blockLength=1024*1024*2;
        File file = new File("文件路径");
        final long fileSize=file.length();
        //当前第几块
        int chunck = 0;
        //换算总共分多少块
        if (file.length() % blockLength == 0L) {
            chuncks= (int) (file.length() / blockLength);
        } else {
            chuncks= (int) (file.length()/ blockLength + 1);
        }
        while (chunck<chuncks){
            //换算出第几块的byte[]
            byte[] block = getBlock((long) (chunck * blockLength), file, blockLength);
            new HttpUrlConnectionAsyncTask().uploadFileBlock(new HttpUrlConnectionAsyncTask.OnHttpProgressUtilListener() {

                @Override
                public void onError(String e) {

                }

                @Override
                public void onProgress(Integer progress) {
                    //换算进度
                    double d = (new BigDecimal(progress / fileSize).setScale(2, BigDecimal.ROUND_HALF_UP)).doubleValue();
                    int pro= (int) (d*100);
                    //这个就是上传进度，已经换算为1-100
                    Log.d(TAG, "onProgress: "+pro);
                }

                @Override
                public void onSuccess(String json) {
                    //上传成功

                }
            },"上传URL 需要传入当前块chunck和总块数chuncks参数，需接口支持才行",block);
            chunck++;
        }
    }

    private byte[] getBlock(Long offset, File file, int blockSize) {
        byte[] result = new byte[blockSize];
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(file, "r");
            //将文件记录指针定位到pos位置
            accessFile.seek(offset);
            int readSize = accessFile.read(result);
            if (readSize == -1) {
                return null;
            } else if (readSize == blockSize) {
                return result;
            } else {
                byte[] tmpByte = new byte[readSize];
                System.arraycopy(result, 0, tmpByte, 0, readSize);
                return tmpByte;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
