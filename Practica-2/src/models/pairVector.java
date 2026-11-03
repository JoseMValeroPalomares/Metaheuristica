package models;

public class pairVector {
    int id;
    double sum_distance;

    public pairVector(int id, double sum_distance) {
        this.id = id;
        this.sum_distance = sum_distance;
    }

    public int getId() {
        return id;
    }

    public double getSum_distance() {
        return sum_distance;
    }
}
