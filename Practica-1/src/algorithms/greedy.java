package algorithms;

import java.util.ArrayList;
import java.util.Comparator;

// Con la matriz de distancias, hay q sumarla y tenemos q obtener un vector<ciudad, suma_distancias> donde ciudad es el id
// de la ciudad y suma_distancias es la suma de las distancias desde esa ciudad al resto de ciudades.
// Después hay q ordenar el vector y de ahí cogemos la ciudad con menor suma_distancias, y esa será nuestra primera ciudad
// Una vez hecho esto empezamos con el greedy

public class greedy {
    
    static class pairVector {
        int id;
        double sum_distance;

        public pairVector(int id, double sum_distance) {
            this.id = id;
            this.sum_distance = sum_distance;
        }
    }
    
    double[][] matrix;
    int n; // longitud
    ArrayList<pairVector> sumCities;
    int initialCity;
    ArrayList<Integer> greedyRute; // solucion

    public greedy(double[][] matrix) {
        this.matrix = matrix;
        this.n = matrix.length;
        this.sumCities = new ArrayList<>();
        this.greedyRute = new ArrayList<>();

        sumDistances();

        sortVector();

        this.initialCity = sumCities.get(0).id;
        
        executeGreedy();
    }


    private void sumDistances() {
        for (int i = 0; i < n; i++) {
            double total = 0;
            for (int j = 0; j < n; j++) {
                total += matrix[i][j];
            }
            sumCities.add(new pairVector(i, total));
        }
    }

    private void sortVector() {
        sumCities.sort(Comparator.comparingDouble(p -> p.sum_distance));
    }

    private void executeGreedy() {
        boolean[] visited = new boolean[n];

        int currentNode = this.initialCity;
        visited[currentNode] = true;
        greedyRute.add(currentNode);

        for (int step = 1; step < n; step++) {
            int nextCity = -1;
            double minDist = 87896789;

            for (int candidate = 0; candidate < n; candidate++) {
                if (!visited[candidate] && matrix[currentNode][candidate] < minDist) {
                    minDist = matrix[currentNode][candidate];
                    nextCity = candidate;
                }
            }


            if (nextCity != -1) {
                visited[nextCity] = true;
                currentNode = nextCity;
                greedyRute.add(currentNode);

            }

        }
    }

    public ArrayList<Integer> getRute() {
        return greedyRute;
    }

    public double getCost() {
        double cost = 0.0;
        int n = greedyRute.size();

        for (int i = 0; i < n - 1; i++) {
            int actualCity = greedyRute.get(i);
            int nextCity = greedyRute.get(i + 1);
            cost += matrix[actualCity][nextCity];
        }
    
        if (n > 0) {
            int lastCity = greedyRute.get(n - 1);
            int firstCity = greedyRute.get(0);
            cost += matrix[lastCity][firstCity];
        }

        cost = Math.round(cost * 100.0) / 100.0;

        return cost;
    }
}