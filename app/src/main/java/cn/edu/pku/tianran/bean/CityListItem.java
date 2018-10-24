package cn.edu.pku.tianran.bean;

//定义一个用于在ListView每一项中展示的类
public class CityListItem {
    private String name;
    private String number;

    public CityListItem(String name,String number){
        this.name = name;
        this.number = number;

    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
