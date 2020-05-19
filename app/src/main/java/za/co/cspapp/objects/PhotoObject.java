package za.co.cspapp.objects;

import java.io.Serializable;

public class PhotoObject implements Serializable {

    private int id;
    private String photo;
    private String thumb;
    private int stock_id;

    public PhotoObject(int id, String photo, String thumb, int stock_id) {
        this.id = id;
        this.photo = photo;
        this.thumb = thumb;
        this.stock_id = stock_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStockId() {
        return stock_id;
    }

    public void setStockId(int stock_id) {
        this.stock_id = stock_id;
    }

    public String getThumb() {
        return thumb;
    }

    public String getPhoto() {
        return photo;
    }

}