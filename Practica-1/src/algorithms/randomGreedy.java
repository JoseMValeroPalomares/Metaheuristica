package algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import models.pairVector;
import java.util.Random;

public class randomGreedy {
    
    double[][] matrix;
    int n; // longitud
    ArrayList<pairVector> sumCities;
    int initialCity;
    ArrayList<Integer> greedyRute; // solucion
    long seed;
    static int K = 5;

    public randomGreedy(double[][] matrix, long seed) {
        this.matrix = matrix;
        this.n = matrix.length;
        this.sumCities = new ArrayList<>();
        this.greedyRute = new ArrayList<>();
        this.seed = seed;

        sumDistances();

        sortVector();

        Random rand = new Random(this.seed);
        int limitK = Math.min(K, n);
        int randomNum = rand.nextInt(limitK);
        this.initialCity = sumCities.get(randomNum).getId();
        
        executeGreedy(rand);
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
        sumCities.sort(Comparator.comparingDouble(p -> p.getSum_distance()));
    }

    private void executeGreedy(Random rand) {
        boolean[] visited = new boolean[n];

        int currentNode = this.initialCity;
        visited[currentNode] = true;
        greedyRute.add(currentNode);

        for (int step = 1; step < n; step++) {

            ArrayList<pairVector> candidates = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (!visited[j]) {
                    double dist = matrix[currentNode][j];
                
                    if (candidates.size() < K) {
                        candidates.add(new pairVector(j, dist));
                        candidates.sort(Comparator.comparingDouble(p -> p.getSum_distance()));
                    } 
                    
                    else if (dist < candidates.get(K - 1).getSum_distance()) {
                        candidates.remove(K - 1);
                        candidates.add(new pairVector(j, dist));
                        candidates.sort(Comparator.comparingDouble(p -> p.getSum_distance()));
                    }
                }
            }

            if (candidates.isEmpty()) break;

            int kActual = Math.min(K, candidates.size());
            int randomIndex = rand.nextInt(kActual);
        
            int nextCity = candidates.get(randomIndex).getId();
            visited[nextCity] = true;
            greedyRute.add(nextCity);

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
