package cn.edu.pku.tianran.weathergo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tianran.weathergo.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.tianran.adapter.CityAdapter;
import cn.edu.pku.tianran.app.MyApplication;
import cn.edu.pku.tianran.bean.City;
import cn.edu.pku.tianran.bean.CityListItem;


public class SelectCity extends Activity implements View.OnClickListener {
    //初始化返回按钮
    private ImageView mBackBtn;

    //ListView控件
    private ListView mListView;
    //从数据库中获得的城市列表
    private List<City> cityList;
    //用于在ListView中展示的城市列表
    private List<CityListItem> filterCityList = new ArrayList<>();


    //当前显示城市代码
    private String cityCode;


    //输入框控件
    private ClearEditText mClearEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        initViews();


    }

    private void initViews(){

        //初始化输入框变量
        mClearEditText =  findViewById(R.id.edit_text);

        //为返回按钮增加监听器
        mBackBtn = findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mListView = findViewById(R.id.title_list);
        MyApplication myApplication = MyApplication.getInstance();
        //得到数据库生成的城市列表
        cityList = myApplication.getCityList();


        //以下为显示初始化的列表信息（没有搜索）
        //提取需要的信息，初始化需要展示的城市列表内容
        for (City city:cityList){
            filterCityList.add(new CityListItem(city.getCity(),city.getNumber()));
        }
        //创建CityAdapter对象
        CityAdapter adapter = new CityAdapter(SelectCity.this,R.layout.city_list_item,
                filterCityList);
        //传给ListView
        mListView.setAdapter(adapter);
        //添加监听器，为点击事件增加功能
       mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityListItem city = filterCityList.get(position);
                Toast.makeText(SelectCity.this,city.getName(),Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                //在finish之前传递数据
                i.putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,i);
                finish();
            }
        });



        //给输入框增加监听器，根据输入值来搜索，更新列表显示
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //输入框为空则更新为原来的列表，否则更新为过滤后的列表
                filterData(s.toString());


                //创建CityAdapter对象
                CityAdapter adapter = new CityAdapter(SelectCity.this,R.layout.city_list_item,
                        filterCityList);
                //传给ListView
                mListView.setAdapter(adapter);
                //添加监听器，为点击事件增加功能
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        CityListItem city = filterCityList.get(position);
                        Toast.makeText(SelectCity.this,city.getName(),Toast.LENGTH_SHORT).show();
                        Intent i = new Intent();
                        //在finish之前传递数据
                        i.putExtra("cityCode",city.getNumber());
                        setResult(RESULT_OK,i);
                        finish();
                    }
                });

            }
        });

    }

    private void filterData(String filterStr){
        filterCityList = new ArrayList<CityListItem>();
        Log.d("Filter",filterStr);

        if(TextUtils.isEmpty(filterStr)){
            for(City city:cityList){

                    filterCityList.add(new CityListItem(city.getCity(),city.getNumber()));

            }
        }else{
            filterCityList.clear();
            for(City city:cityList){
                if (city.getCity().indexOf(filterStr.toString())!=-1){
                    filterCityList.add(new CityListItem(city.getCity(),city.getNumber()));
                }
            }
        }
        //adapter.updateListView(filterDataList);
    }



    @Override
    public void onClick(View v){
        switch (v.getId()){
            //点击返回图标，返回上一活动
            case R.id.title_back:
                //获取前一活动传来的citycode
                Intent pre = getIntent();
                cityCode = pre.getStringExtra("cityCode");
                Log.d("cityCode:",cityCode);

                //返回
                Intent i = new Intent();
                //在finish之前传递数据
                i.putExtra("cityCode",cityCode);
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }




}
