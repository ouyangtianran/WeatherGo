package cn.edu.pku.tianran.weathergo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.tianran.weathergo.R;

import cn.edu.pku.tianran.app.MyApplication;
import cn.edu.pku.tianran.bean.City;


public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackBtn;

    private Myadapter myadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        initViews();


    }

    private void initViews(){
        mBackBtn = findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        mClearEditText = (ClearEditText) findViewById(R.id.search_city);

        mList = (ListView) findViewById(R.id.title_list);
        MyApplication myApplication = (MyApplication) getApplication();
        cityList = myApplication.getCityList();
        for(City city : citylist){
            filterDataList.add(city);
        }
        myadapter = new Myadapter(SelectCity.this,cityList);
        mList.setAdapter(myadapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
           @Override
           public void onItemClick(AdapterView adapterView,View view,int position,long l){
               City city = filterDataList.get(position);
               Intent i =new Intent();
               i.putExtra("cityCode",city.getNumber());
               setResult(RESULT_OK);
               finish();
           }
        });

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode","101160101");
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }
}
