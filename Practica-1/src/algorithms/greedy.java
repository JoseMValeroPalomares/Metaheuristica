package algorithms;

import java.util.ArrayList;


// Con la matriz de distancias, hay q sumarla y tenemos q obtener un vector<ciudad, suma_distancias> donde ciudad es el id
// de la ciudad y suma_distancias es la suma de las distancias desde esa ciudad al resto de ciudades.
// Después hay q ordenar el vector y de ahí cogemos la ciudad con menor suma_distancias, y esa será nuestra primera ciudad
// Una vez hecho esto empezamos con el greedy
public class Greedy {
    boolean[] visited;
    ArrayList<Integer> solution;
    double cost;
    int min = 999999;
    int initial_node;
    double[] sum_vector;

    public Greedy(double[][] distanceMatrix) {
        n = distanceMatrix.length;
        this.visited = new boolean[n];
        this.solution = new ArrayList<>();
        this.cost = 0.0;
        this.sum_vector = new double[n];

        for (int i = 0; i < n; i++) {
            double sumRow = 0;
            for (int j = 0; j < n; j++) {
                sumaRow += distanceMatrix[i][j];
            }
            sum_vector[i] = sumRow;
        }

        double minSum = sum_vector[0];
        this.initial_node = 0;
        for (int i = 1; i < n; i++) {
            if (sum_vector[i] < minSum) {
                minSum = sum_vector[i];
                this.initial_node = i;
            }
        }

        for (int i = initial_node; i < n; i++) {
            for (j = 0; j < n; j++) {
                if (distanceMatrix[i][j] != 0 || visited[j] == false)  {
                    if (distanceMatrix[i][j] < min) {
                        min = distanceMatrix[i][j];
                        visited[i] = true;
                    }
                        
                }
            }
        }
        
    }

}