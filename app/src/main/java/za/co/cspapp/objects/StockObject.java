package za.co.cspapp.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class StockObject implements Serializable {

    private int id;
    private String stock_num;
    private String trim;
    private int mileage;
    private int year;
    private int price;
    private int cost_price;
    private int state;
    private int colour;
    private String vin;
    private String regNo;
    private ArrayList<PhotoObject> photos;
    private PhotoObject heroPic;

    public StockObject( JSONObject item ) {
        try{
            this.id = item.getInt("id");
            if(item.isNull("stock_num")){this.stock_num = "";}else{this.stock_num = item.getString("stock_num");}
            if(item.isNull("trim")){this.trim = "";}else{this.trim = item.getString("trim");}
            if(item.isNull("mileage")){this.mileage = 0;}else{this.mileage = item.getInt("mileage");}
            if(item.isNull("year")){this.year = 0;}else{this.year = item.getInt("year");}
            if(item.isNull("price")){this.price = 0;}else{this.price = item.getInt("price");}
            if(item.isNull("cost_price")){this.cost_price = 0;}else{this.cost_price = item.getInt("cost_price");}
            if(item.isNull("regNo")){this.regNo = "";}else{this.regNo = item.getString("regNo");}
            if(item.isNull("vin")){this.vin = "";}else{this.vin = item.getString("vin");}
            if(item.isNull("colour")){this.colour = 0;}else{this.colour = item.getInt("colour");}
            if(item.isNull("state")){this.state = 0;}else{this.state = item.getInt("state");}
            this.photos = getPhots(item.getJSONArray("photos"));
        } catch (JSONException e) {

        }
    }

    //GETTERS
    public int getId() { return id; }

    public String getStockNum() {
        return stock_num;
    }
    public String getTrim() {
        return trim;
    }
    public int getMileage() {
        return mileage;
    }
    public int getYear() {
        return year;
    }
    public int getPrice() {
        return price;
    }
    public int getCostPrice() {
        return cost_price;
    }
    public String getVin() {
        return vin;
    }
    public String getRegNo() {
        return regNo;
    }
    public int getState() {
        return state;
    }
    public int getColour() {
        return colour;
    }

    public ArrayList<PhotoObject> getPhotos(){
        return photos;
    }

    public PhotoObject getHeroPic(){
        return heroPic;
    }

    public ArrayList<PhotoObject> getPhots(JSONArray phts) {
        ArrayList<PhotoObject> photoList = new ArrayList<PhotoObject>();
        for(int x = 0; x < phts.length(); x++){
            try{
                int id = phts.getJSONObject(x).getInt("id");
                String cdn = "https://iris.carsalesportal.co.za/cdn";
                String directory = phts.getJSONObject(x).getString("directory");
                String thmb = "thumb-" + phts.getJSONObject(x).getString("photo");
                String pht = phts.getJSONObject(x).getString("photo");
                String thumb = cdn + directory + thmb;
                String photo = cdn + directory + pht;
                PhotoObject item = new PhotoObject(id, photo, thumb, this.id);
                if(x == 0){
                    this.heroPic = item;
                }
                photoList.add(item);
            } catch (JSONException e) {


            }
        }

        return photoList;
    }


    //SETTERS
    public void setId(int id) {
        this.id = id;
    }
    public void setStockNum(String stock_num) {
        this.stock_num = stock_num;
    }
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public void setCostPrice(int cost_price) {
        this.cost_price = cost_price;
    }
    public void setVin(String vin) {
        this.vin = vin;
    }
    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }
    public void setState(int state) {
        this.state = state;
    }
    public void setColour(int colour) {
        this.colour = colour;
    }

}