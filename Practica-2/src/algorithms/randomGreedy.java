package algorithms;

import java.util.ArrayList;
import models.pairVector;
import java.util.Random;

public class randomGreedy extends greedy {

    private Random rand;
    private int K;

    public randomGreedy(double[][] matrix, Random rand, int K) {
        super(matrix, false); // Llamar al constructor del padre.
        this.rand = rand;
        this.K = K;
        executeGreedy();
    }


    @Override
    protected void executeGreedy() {
        // Copia del vector ordenado <ciudad, distancia_al_resto>; se van eliminando las elegidas
        ArrayList<pairVector> remaining = new ArrayList<>(sumCities);

        while (!remaining.isEmpty()) {
            int limitK = Math.min(K, remaining.size());
            int randomIndex = rand.nextInt(limitK); // aleatorio entre las K mas prometedoras

            int city = remaining.remove(randomIndex).getId();
            if (greedyRute.isEmpty()) this.initialCity = city;
            greedyRute.add(city);
        }
    }

}
