package algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import models.pairVector;
import java.util.Random;

public class randomGreedy extends greedy {

    private long seed;
    private static int K = 5;

    public randomGreedy(double[][] matrix, long seed) {
        super(matrix, false); // Llamar al constructor del padre.
        this.seed = seed;
        executeGreedy();
    }


    @Override 
    protected void executeGreedy() {
        // Inicializacion numero random
        Random rand = new Random(this.seed);
        int limitK = Math.min(K, n);
        int randomNum = rand.nextInt(limitK);

        this.initialCity = sumCities.get(randomNum).getId();

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

}
