package cn.edu.pku.tianran.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tianran.weathergo.R;
import java.util.List;
import cn.edu.pku.tianran.bean.CityListItem;

//自定义适配器
public class CityAdapter extends ArrayAdapter<CityListItem> {
    private int resoureceId;

    public CityAdapter(Context context,int textViewResourceId,List<CityListItem> objects){
        super(context,textViewResourceId,objects);
        resoureceId = textViewResourceId;
    }

    @Override
    //此方法在每个子项被滚动到屏幕内的时候会被调用
    public View getView(int position, View convertView, ViewGroup parent){
        //得到item实例
        CityListItem cityitem = getItem(position);
        View view;
        if (convertView == null){
            //利用LayoutInflater为这个子项加载自定义的布局
            view = LayoutInflater.from(getContext()).inflate(resoureceId,parent,false);
        }else{
            view = convertView;
        }
        TextView cityname = (TextView) view.findViewById(R.id.city_name);
        TextView citycode = (TextView) view.findViewById(R.id.city_number);
        cityname.setText(cityitem.getName());
        citycode.setText(cityitem.getNumber());
        return view;
    }
}
