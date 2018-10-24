package cn.edu.pku.tianran.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.tianran.bean.City;

public class CityDB {
    public static final String CITY_DB_NAME = "city.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;
    //初始化城市数据库
    public CityDB(Context context, String path) {
        db = context.openOrCreateDatabase(path, Context.MODE_PRIVATE, null);
    }
    //构建城市泛型的列表
    public List<City> getAllCity() {
        List<City> list = new ArrayList<City>();
        //获取Cursor
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME, null);
        //依次获得每次城市各项信息，并存入一个列表
        while (c.moveToNext()) {
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, firstPY, allPY,allFirstPY);
            list.add(item);
        }
        return list;
    }

}
