package cn.edu.pku.tianran.weathergo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.example.tianran.weathergo.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.tianran.adapter.ViewPagerAdapter;
import cn.edu.pku.tianran.bean.TodayWeather;
import cn.edu.pku.tianran.util.BDLocationUtils;
import cn.edu.pku.tianran.util.NetUtil;

public class MainActivity extends Activity implements OnClickListener,ViewPager.OnPageChangeListener{
    //定义整型变量UPDATE_TODAY_WEATHER，表示更新操作
    private static final int UPDATE_TODAY_WEATHER = 1;
    //为更新按钮图标创建变量
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;

    //初始化界面控件
    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,
            temperatureTv,climateTv,windTv,city_name_Tv;
    private ImageView weatherImg,pmImg;

    //当前城市代码
    private String cityCode;

    //六天天⽓气信息展示
    //显示两个展示⻚页
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private ArrayList<View> views;
    //为引导⻚页增加⼩小圆点
    private ImageView[] dots; //存放⼩小圆点的集合
    private int[] ids = {R.id.dot1,R.id.dot2};
    private TextView
            week_today,temperature,climate,wind,week_today1,temperature1,climate1,wind1
            ,week_today2,temperature2,climate2,wind2;
    private TextView
            week_today3,temperature3,climate3,wind3,week_today4,temperature4,climate4,wind4
            ,week_today5,temperature5,climate5,wind5;
    //六日天气图片
    private ImageView climateImg,climateImg1,climateImg2,climateImg3,climateImg4,climateImg5;




    //新建一个Handler对象
    private Handler mHandler = new Handler() {
        //重写handleMessage方法对Message进行处理
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                //如果信息为更新，则执行更新
                case UPDATE_TODAY_WEATHER:
                    //在主线程中更新UI
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        //给更新按钮图片添加监听器
        mUpdateBtn.setOnClickListener(this);

        //判断并显示网络状态
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("WeatherGo","网络ok");
            Toast.makeText(this, "网络ok！", Toast.LENGTH_SHORT).show();
        }
        else{
            Log.d("WeatherGo","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了",Toast.LENGTH_SHORT).show();
        }

        //初始化城市选择控件，并添加监听器
        BDLocationUtils bdLocationUtils = new BDLocationUtils(MainActivity.this);
        bdLocationUtils.doLocation();//开启定位
        bdLocationUtils.mLocationClient.start();//开始定位


        //初始化滑动页面
        initViews();

        //初始化小圆点
        initDots();

        //初始化界面信息
        initView();
    }





    //初始化滑动页面
    void initViews(){


        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.six_day1,null));
        views.add(inflater.inflate(R.layout.six_day2,null));
        vpAdapter = new ViewPagerAdapter(views);
        vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        //为pageviewer配置监听事件
        vp.setOnPageChangeListener(this);


    }

    //初始化小圆点
    void initDots() {
        dots = new ImageView[views.size()];
        for (int i = 0; i < views.size(); i++) {
            dots[i] = (ImageView) findViewById(ids[i]);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int
            positionOffsetPixels) {
    }
    @Override
    //根据所选页面进行操作
    public void onPageSelected(int position) {
        for (int a = 0;a<ids.length;a++){
            if(a==position){
                dots[a].setImageResource(R.drawable.focused);
            }else {
                dots[a].setImageResource(R.drawable.unfocused);
            }
        }
    }
    @Override
    public void onPageScrollStateChanged(int state) {
    }


    //初始化全部界面
    void initView(){

        //六日天气
        //1
        week_today=views.get(0).findViewById(R.id.week_today);
        temperature=views.get(0).findViewById(R.id.temperature);
        climate=views.get(0).findViewById(R.id.climate);
        wind=views.get(0).findViewById(R.id.wind);
        //2
        week_today1=views.get(0).findViewById(R.id.week_today1);
        temperature1=views.get(0).findViewById(R.id.temperature1);
        climate1=views.get(0).findViewById(R.id.climate1);
        wind1=views.get(0).findViewById(R.id.wind1);
        //3
        week_today2=views.get(0).findViewById(R.id.week_today2);
        temperature2=views.get(0).findViewById(R.id.temperature2);
        climate2=views.get(0).findViewById(R.id.climate2);
        wind2=views.get(0).findViewById(R.id.wind2);
        //4
        week_today3=views.get(1).findViewById(R.id.week_today3);
        temperature3=views.get(1).findViewById(R.id.temperature3);
        climate3=views.get(1).findViewById(R.id.climate3);
        wind3=views.get(1).findViewById(R.id.wind3);
        //5
        week_today4=views.get(1).findViewById(R.id.week_today4);
        temperature4=views.get(1).findViewById(R.id.temperature4);
        climate4=views.get(1).findViewById(R.id.climate4);
        wind4=views.get(1).findViewById(R.id.wind4);
        //6
        week_today5=views.get(1).findViewById(R.id.week_today5);
        temperature5=views.get(1).findViewById(R.id.temperature5);
        climate5=views.get(1).findViewById(R.id.climate5);
        wind5=views.get(1).findViewById(R.id.wind5);

        //六日天气变量设置初始值
        //1
        week_today.setText("N/A");
        temperature.setText("N/A");
        climate.setText("N/A");
        wind.setText("N/A");
        //2
        week_today1.setText("N/A");
        temperature1.setText("N/A");
        climate1.setText("N/A");
        wind1.setText("N/A");
        //3
        week_today2.setText("N/A");
        temperature2.setText("N/A");
        climate2.setText("N/A");
        wind2.setText("N/A");
        //4
        week_today3.setText("N/A");
        temperature3.setText("N/A");
        climate3.setText("N/A");
        wind3.setText("N/A");
        //5
        week_today4.setText("N/A");
        temperature4.setText("N/A");
        climate4.setText("N/A");
        wind4.setText("N/A");
        //6
        week_today5.setText("N/A");
        temperature5.setText("N/A");
        climate5.setText("N/A");
        wind5.setText("N/A");

        //六日天气图片连接
        climateImg = (ImageView) views.get(0).findViewById(R.id.weather_img);
        climateImg1 = (ImageView) views.get(0).findViewById(R.id.weather_img1);
        climateImg2 = (ImageView) views.get(0).findViewById(R.id.weather_img2);
        climateImg3 = (ImageView) views.get(1).findViewById(R.id.weather_img3);
        climateImg4 = (ImageView) views.get(1).findViewById(R.id.weather_img4);
        climateImg5 = (ImageView) views.get(1).findViewById(R.id.weather_img5);

        //今日天气
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);

        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

        //初始值都设置为N/A
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

    }


    /**
     * 获取某城市天气情况
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode){
        //根据城市代码得到天气情况的地址
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("WeatherGo",address);

        //新建一个线程来获得天气情况
        new Thread(new Runnable() {
            @Override
            public void run() {
                //创建HttpURLConnection实例变量
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try{
                    //新建一个URL对象，并传入目标的地址
                    URL url = new URL(address);
                    //调用openConnection（）方法得到一个实例
                    con = (HttpURLConnection)url.openConnection();
                    //设置http请求方法为GET
                    con.setRequestMethod("GET");
                    //设置超时
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    //获取服务器返回的输入流
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    //依次读入各行
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("WeatherGo", str);
                    }
                    String responseStr=response.toString();
                    Log.d("WeatherGo", responseStr);

                    //解析获得的回复到TodayWeather结构
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null){
                        Log.d("WeatherGo",todayWeather.toString());
                        //新建一个消息实例，填入what字段和obj字段
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        //发出消息
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                //断开连接
                finally {
                    if(con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View view){
        //点击城市选择图标，则转到城市选择界面
        if(view.getId() == R.id.title_city_manager){
            //利用Intent来切换活动，目标活动SelectCity
            Intent i = new Intent(this,SelectCity.class);
            i.putExtra("cityCode",cityCode);
            startActivityForResult(i,1);
        }

        //更新按钮事件
        if (view.getId() == R.id.title_update_btn){
            //得到sharedpreferences对象
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            //获取当前显示城市代码，如果获取不到则取默认值101010100（北京）
            cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("WeatherGo","cityCode");

            //检测并输出网络网络状况
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("WeatherGo","网络ok");

                //根据城市代码获取对应的天气情况
                queryWeatherCode(cityCode);

            }else{
                Log.d("WeatherGo","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("WeatherGo", "选择的城市代码为" + newCityCode);

            //将返回城市代码存入到sharedpreferrence
            //得到sharedpreferences对象
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            //创建编辑器
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //存入
            editor.putString("main_city_code",newCityCode);
            editor.apply();


            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("WeatherGo", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("WeatherGo", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    //使用PULL方式从长的字符串中解析出需要的信息
        //去看WeatherApi的格式！！！！！
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;

        //计数（yesterday另算）
        //计数是第几个数据，这三个数据总共有从今天开始往后的五天。
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        //计数是第几个数据，这三个数据总共有从今天开始往后的五天，且每天早晚各一组。
        int fengxiangCount=-1;//最开头还有额外的一组
        int fengliCount =-1;//最开头还有额外的一组
        int typeCount =0;//最开头没有单独的type数据

        try{
            //获取一个XmlPullParserFactory的实例
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            //通过fac实例得到XmlPullParser对象
            XmlPullParser xmlPullParser = fac.newPullParser();
            //将XML数据设置进去
            xmlPullParser.setInput(new StringReader(xmldata));
            //得到当前的解析事件
            int eventType = xmlPullParser.getEventType();
            Log.d("WeatherGo","parseXML");
            //循环进行解析
            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null) {
                            //根据解析得到的信息更新todayWeather中的内容
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == -1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == -1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fl_1")) {//yesterday
                                eventType = xmlPullParser.next();
                                todayWeather.setWind(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind1(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind2(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                                eventType = xmlPullParser.next();
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind3(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 5) {
                                eventType = xmlPullParser.next();
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 6) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind4(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 7) {
                                eventType = xmlPullParser.next();
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 8) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind5(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("date_1")) {//yesterday
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today(xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                todayWeather.setWeek_today1(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today2(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today3(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today4(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today5(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("high_1")) {//yesterday
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureH1(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH2(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH4(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH5(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low_1")) {//yesterday
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureL1(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureL2(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureL3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureL4(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureL5(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type_1")) {//yesterday
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                todayWeather.setClimate1(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                todayWeather.setClimate2(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                todayWeather.setClimate3(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 5) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 6) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                todayWeather.setClimate4(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 7) {
                                eventType = xmlPullParser.next();
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 8) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                todayWeather.setClimate5(xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
            }catch (XmlPullParserException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return todayWeather;
    }

    //利用TodayWeather对象更新UI中的控件
    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        //六天天气预报
        week_today.setText(todayWeather.getWeek_today());
        week_today1.setText(todayWeather.getWeek_today1());
        week_today2.setText(todayWeather.getWeek_today2());
        week_today3.setText(todayWeather.getWeek_today3());
        week_today4.setText(todayWeather.getWeek_today4());
        week_today5.setText(todayWeather.getWeek_today5());
        wind.setText(todayWeather.getWind());
        wind1.setText(todayWeather.getWind1());
        wind2.setText(todayWeather.getWind2());
        wind3.setText(todayWeather.getWind3());
        wind4.setText(todayWeather.getWind4());
        wind5.setText(todayWeather.getWind5());
        climate.setText(todayWeather.getClimate());
        climate1.setText(todayWeather.getClimate1());
        climate2.setText(todayWeather.getClimate2());
        climate3.setText(todayWeather.getClimate3());
        climate4.setText(todayWeather.getClimate4());
        climate5.setText(todayWeather.getClimate5());
        temperature.setText(todayWeather.getTemperatureH()+"~"+todayWeather.getTemperatureL());
        temperature1.setText(todayWeather.getTemperatureH1()+"~"+todayWeather.getTemperatureL1());
        temperature2.setText(todayWeather.getTemperatureH2()+"~"+todayWeather.getTemperatureL2());
        temperature3.setText(todayWeather.getTemperatureH3()+"~"+todayWeather.getTemperatureL3());
        temperature4.setText(todayWeather.getTemperatureH4()+"~"+todayWeather.getTemperatureL4());
        temperature5.setText(todayWeather.getTemperatureH5()+"~"+todayWeather.getTemperatureL5());

        //若没有pm2.5信息则先跳过
        if(todayWeather.getPm25()!=null){
            double pmValue = Double.parseDouble(todayWeather.getPm25());
            if (pmValue>=0){
                if(pmValue<51) pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
                else if (pmValue<101) pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
                else if (pmValue<151) pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
                else if (pmValue<201) pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
                else if (pmValue<301) pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
                else pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
            }
        }

        //更新今日天气图标
        //若没有天气类型信息则先跳过
        if(todayWeather.getType()!=null){
            String weatherType =todayWeather.getType();
            if (weatherType.equals("晴")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
            else if (weatherType.equals("暴雪")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
            else if (weatherType.equals("暴雨")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
            else if (weatherType.equals("大暴雨")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            else if (weatherType.equals("大雪")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
            else if (weatherType.equals("大雨"))weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
            else if (weatherType.equals("多云")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
            else if (weatherType.equals("雷阵雨")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            else if (weatherType.equals("雷阵雨冰雹")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            else if (weatherType.equals("沙尘暴")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            else if (weatherType.equals("特大暴雨")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            else if (weatherType.equals("雾")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
            else if (weatherType.equals("小雪")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            else if (weatherType.equals("小雨")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            else if (weatherType.equals("阴")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
            else if (weatherType.equals("雨夹雪")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            else if (weatherType.equals("阵雪")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            else if (weatherType.equals("阵雨")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            else if (weatherType.equals("中雪")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            else if (weatherType.equals("中雨")) weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }

        //更新六日天气图标
        //若没有天气类型信息则先跳过
        if(todayWeather.getClimate()!=null){
            String weatherType =todayWeather.getClimate();
            if (weatherType.equals("晴")) climateImg.setImageResource(R.drawable.biz_plugin_weather_qing);
            else if (weatherType.equals("暴雪")) climateImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
            else if (weatherType.equals("暴雨")) climateImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
            else if (weatherType.equals("大暴雨")) climateImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            else if (weatherType.equals("大雪")) climateImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
            else if (weatherType.equals("大雨"))climateImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
            else if (weatherType.equals("多云")) climateImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
            else if (weatherType.equals("雷阵雨")) climateImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            else if (weatherType.equals("雷阵雨冰雹")) climateImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            else if (weatherType.equals("沙尘暴")) climateImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            else if (weatherType.equals("特大暴雨")) climateImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            else if (weatherType.equals("雾")) climateImg.setImageResource(R.drawable.biz_plugin_weather_wu);
            else if (weatherType.equals("小雪")) climateImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            else if (weatherType.equals("小雨")) climateImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            else if (weatherType.equals("阴")) climateImg.setImageResource(R.drawable.biz_plugin_weather_yin);
            else if (weatherType.equals("雨夹雪")) climateImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            else if (weatherType.equals("阵雪")) climateImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            else if (weatherType.equals("阵雨")) climateImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            else if (weatherType.equals("中雪")) climateImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            else if (weatherType.equals("中雨")) climateImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }
        if(todayWeather.getClimate1()!=null){
            String weatherType =todayWeather.getClimate1();
            if (weatherType.equals("晴")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_qing);
            else if (weatherType.equals("暴雪")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_baoxue);
            else if (weatherType.equals("暴雨")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_baoyu);
            else if (weatherType.equals("大暴雨")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            else if (weatherType.equals("大雪")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_daxue);
            else if (weatherType.equals("大雨"))climateImg1.setImageResource(R.drawable.biz_plugin_weather_dayu);
            else if (weatherType.equals("多云")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_duoyun);
            else if (weatherType.equals("雷阵雨")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            else if (weatherType.equals("雷阵雨冰雹")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            else if (weatherType.equals("沙尘暴")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            else if (weatherType.equals("特大暴雨")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            else if (weatherType.equals("雾")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_wu);
            else if (weatherType.equals("小雪")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            else if (weatherType.equals("小雨")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            else if (weatherType.equals("阴")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_yin);
            else if (weatherType.equals("雨夹雪")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            else if (weatherType.equals("阵雪")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            else if (weatherType.equals("阵雨")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            else if (weatherType.equals("中雪")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            else if (weatherType.equals("中雨")) climateImg1.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }
        if(todayWeather.getClimate2()!=null){
            String weatherType =todayWeather.getClimate2();
            if (weatherType.equals("晴")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_qing);
            else if (weatherType.equals("暴雪")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_baoxue);
            else if (weatherType.equals("暴雨")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_baoyu);
            else if (weatherType.equals("大暴雨")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            else if (weatherType.equals("大雪")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_daxue);
            else if (weatherType.equals("大雨"))climateImg2.setImageResource(R.drawable.biz_plugin_weather_dayu);
            else if (weatherType.equals("多云")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_duoyun);
            else if (weatherType.equals("雷阵雨")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            else if (weatherType.equals("雷阵雨冰雹")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            else if (weatherType.equals("沙尘暴")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            else if (weatherType.equals("特大暴雨")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            else if (weatherType.equals("雾")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_wu);
            else if (weatherType.equals("小雪")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            else if (weatherType.equals("小雨")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            else if (weatherType.equals("阴")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_yin);
            else if (weatherType.equals("雨夹雪")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            else if (weatherType.equals("阵雪")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            else if (weatherType.equals("阵雨")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            else if (weatherType.equals("中雪")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            else if (weatherType.equals("中雨")) climateImg2.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }
        if(todayWeather.getClimate3()!=null){
            String weatherType =todayWeather.getClimate3();
            if (weatherType.equals("晴")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_qing);
            else if (weatherType.equals("暴雪")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_baoxue);
            else if (weatherType.equals("暴雨")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_baoyu);
            else if (weatherType.equals("大暴雨")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            else if (weatherType.equals("大雪")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_daxue);
            else if (weatherType.equals("大雨"))climateImg3.setImageResource(R.drawable.biz_plugin_weather_dayu);
            else if (weatherType.equals("多云")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_duoyun);
            else if (weatherType.equals("雷阵雨")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            else if (weatherType.equals("雷阵雨冰雹")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            else if (weatherType.equals("沙尘暴")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            else if (weatherType.equals("特大暴雨")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            else if (weatherType.equals("雾")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_wu);
            else if (weatherType.equals("小雪")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            else if (weatherType.equals("小雨")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            else if (weatherType.equals("阴")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_yin);
            else if (weatherType.equals("雨夹雪")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            else if (weatherType.equals("阵雪")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            else if (weatherType.equals("阵雨")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            else if (weatherType.equals("中雪")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            else if (weatherType.equals("中雨")) climateImg3.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }
        if(todayWeather.getClimate4()!=null){
            String weatherType =todayWeather.getClimate4();
            if (weatherType.equals("晴")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_qing);
            else if (weatherType.equals("暴雪")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_baoxue);
            else if (weatherType.equals("暴雨")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_baoyu);
            else if (weatherType.equals("大暴雨")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            else if (weatherType.equals("大雪")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_daxue);
            else if (weatherType.equals("大雨"))climateImg4.setImageResource(R.drawable.biz_plugin_weather_dayu);
            else if (weatherType.equals("多云")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_duoyun);
            else if (weatherType.equals("雷阵雨")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            else if (weatherType.equals("雷阵雨冰雹")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            else if (weatherType.equals("沙尘暴")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            else if (weatherType.equals("特大暴雨")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            else if (weatherType.equals("雾")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_wu);
            else if (weatherType.equals("小雪")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            else if (weatherType.equals("小雨")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            else if (weatherType.equals("阴")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_yin);
            else if (weatherType.equals("雨夹雪")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            else if (weatherType.equals("阵雪")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            else if (weatherType.equals("阵雨")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            else if (weatherType.equals("中雪")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            else if (weatherType.equals("中雨")) climateImg4.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }
        if(todayWeather.getClimate5()!=null){
            String weatherType =todayWeather.getClimate5();
            if (weatherType.equals("晴")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_qing);
            else if (weatherType.equals("暴雪")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_baoxue);
            else if (weatherType.equals("暴雨")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_baoyu);
            else if (weatherType.equals("大暴雨")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
            else if (weatherType.equals("大雪")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_daxue);
            else if (weatherType.equals("大雨"))climateImg5.setImageResource(R.drawable.biz_plugin_weather_dayu);
            else if (weatherType.equals("多云")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_duoyun);
            else if (weatherType.equals("雷阵雨")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
            else if (weatherType.equals("雷阵雨冰雹")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
            else if (weatherType.equals("沙尘暴")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
            else if (weatherType.equals("特大暴雨")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
            else if (weatherType.equals("雾")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_wu);
            else if (weatherType.equals("小雪")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
            else if (weatherType.equals("小雨")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
            else if (weatherType.equals("阴")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_yin);
            else if (weatherType.equals("雨夹雪")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
            else if (weatherType.equals("阵雪")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
            else if (weatherType.equals("阵雨")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
            else if (weatherType.equals("中雪")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
            else if (weatherType.equals("中雨")) climateImg5.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }


        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }
}

