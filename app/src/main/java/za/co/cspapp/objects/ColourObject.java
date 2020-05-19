package za.co.cspapp.objects;

import org.json.JSONException;
import org.json.JSONObject;

public class ColourObject {

    private int id;
    private String colour;

    public ColourObject(JSONObject item ) {
        try{
            this.id = item.getInt("id");
            if(item.isNull("colour")){this.colour = "";}else{this.colour = item.getString("colour");}
        } catch (JSONException e) {

        }
    }

    //GETTERS
    public int getId() { return id; }
    public String getColour() {
        return colour;
    }

    //SETTERS
    public void setId(int id) {
        this.id = id;
    }
    public void setColour(String colour) {
        this.colour = colour;
    }

}