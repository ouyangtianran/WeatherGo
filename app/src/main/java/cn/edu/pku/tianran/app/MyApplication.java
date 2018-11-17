package cn.edu.pku.tianran.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.tianran.bean.City;
import cn.edu.pku.tianran.db.CityDB;

public class MyApplication extends Application{
    private static final String TAG = "MyAPP";

    private static MyApplication myApplication;
    private CityDB mCityDB;

    private List<City> mCityList;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        myApplication = this;
        //打开或者创建数据库
        mCityDB = openCityDB();
        //初始化城市列表
        initCityList();




    }

    //初始化城市列表
    private void initCityList(){
        mCityList = new ArrayList<City>();
        //新建一个线程初始化城市列表
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                prepareCityList();
            }
        }).start();
    }


    //准备城市列表,输出城市和代码两项
    private boolean prepareCityList() {
        //初始化mCityList
        mCityList = mCityDB.getAllCity();
        int i=0;
        for (City city : mCityList) {
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }

    //返回城市列表
    public List<City> getCityList() {
        return mCityList;
    }




    public static MyApplication getInstance(){
            return myApplication;
        }

    //打开或者创建一个数据库
    private CityDB openCityDB() {
        //构建数据库地址
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath
                ()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG, path);
        //数据库不存在则建立
        if (!db.exists()) {
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if (!dirFirstFolder.exists()) {
                dirFirstFolder.mkdirs();
                Log.i("MyApp", "mkdirs");
            }
            Log.i("MyApp", "db is not exists");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }

    }

