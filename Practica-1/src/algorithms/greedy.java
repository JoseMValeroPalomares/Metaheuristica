package algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import models.pairVector;


public class greedy {
    
    protected double[][] matrix;
    protected int n; // longitud
    protected ArrayList<pairVector> sumCities;
    protected int initialCity;
    protected ArrayList<Integer> greedyRute; // solucion

    public greedy(double[][] matrix) {
        this(matrix, true);
    }

    public greedy(double[][] matrix, boolean runExecution) {
        this.matrix = matrix;
        this.n = matrix.length;
        this.sumCities = new ArrayList<>();
        this.greedyRute = new ArrayList<>();

        sumDistances();

        sortVector();
        
        if (runExecution) { // Se ejecuta si el que lo ha llamado es "new greedy(matrix)", es decir, si no lo ha llamado randomGreedy.java
            executeGreedy();
        }
    }


    protected void sumDistances() {
        for (int i = 0; i < n; i++) {
            double total = 0;
            for (int j = 0; j < n; j++) {
                total += matrix[i][j];
            }
            sumCities.add(new pairVector(i, total));
        }
    }

    protected void sortVector() {
        sumCities.sort(Comparator.comparingDouble(p -> p.getSum_distance()));
    }

    protected void executeGreedy() {
        this.initialCity = sumCities.get(0).getId();

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