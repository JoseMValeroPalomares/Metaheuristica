package models;

public class City {
    public int id;
    public double x;
    public double y;

    public City(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Ciudad " + id + " -> (x=" + x + ", y=" + y + ")";
    }
}
