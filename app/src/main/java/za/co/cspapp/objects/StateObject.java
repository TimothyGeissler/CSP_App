package za.co.cspapp.objects;

public class StateObject {

    private int id;
    private String state;

    public StateObject(int id, String state) {
            this.id = id;
            this.state = state;
    }

    //GETTERS
    public int getId() { return id; }
    public String getState() {
        return state;
    }

    //SETTERS
    public void setId(int id) {
        this.id = id;
    }
    public void setState(String state) {
        this.state = state;
    }

}