

public class City {
    int id;
    double x;
    double y;

    public City(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    // Metodo util para poder imprimir una ciudad facilmente con println
    @Override
    public String toString() {
        return "Ciudad " + id + " -> (x=" + x + ", y=" + y + ")";
    }
}