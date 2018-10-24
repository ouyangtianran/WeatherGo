package cn.edu.pku.tianran.weathergo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import cn.edu.pku.tianran.bean.TodayWeather;
import cn.edu.pku.tianran.util.NetUtil;

public class MainActivity extends Activity implements OnClickListener{
    //定义整型变量UPDATE_TODAY_WEATHER，表示更新操作
    private static final int UPDATE_TODAY_WEATHER = 1;
    //为更新按钮图标创建变量
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;

    //初始化界面控件
    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,
            temperatureTv,climateTv,windTv,city_name_Tv;
    private ImageView weatherImg,pmImg;

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
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        //初始化界面信息
        initView();
    }

    //初始化界面控件内容
    void initView(){
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

        pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
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
            startActivityForResult(i,1);
        }

        //更新按钮事件
        if (view.getId() == R.id.title_update_btn){
            //得到sharedpreferences对象
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            //设置默认的城市代码
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
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
        private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        //初始化部分天气信息的数值
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount =0;

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
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
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


        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }
}

