
package algorithms;

import java.util.ArrayList;
import java.util.Collections;

public class localSearch {
    private static final int MAX_ITERATIONS = 10000;

    protected int iterations;
    protected int[] mask;
    protected int n;
    protected ArrayList<Integer> solution;
    protected double[][] matrix;
    protected double currentCost;

    public localSearch(ArrayList<Integer> solution, double[][] matrix) {
        this.n = solution.size();
        this.mask = new int[n]; 
        this.solution = new ArrayList<>(solution);
        this.matrix = matrix;
        this.iterations = 0;
        this.currentCost = calculateCost(this.solution);

        search();
    }


    public void search() {
        int index = 0;
        int noImprovement = 0; // posiciones consecutivas sin mejora

        while (noImprovement < n && iterations < MAX_ITERATIONS) { // si noImprovement >= n, todo mask es 1 (no mejora)

            if (mask[index] == 1) {
                noImprovement++;
            } else if (tryImprovement(index)) {
                noImprovement = 0;
            } else {
                noImprovement++;
                mask[index] = 1;
            }
            
            index = (index + 1) % n;
        }
    }

    private boolean tryImprovement(int i) {
        int c1 = solution.get(i); // primera ciudad
        int c2 = solution.get((i + 1) % n); // ciudad sucesora

        for (int offset = 2; offset <= n - 2; offset++) {
            int j = (i + offset) % n;

            int c3 = solution.get(j);
            int c4 = solution.get((j + 1) % n);

            double delta = matrix[c1][c3] + matrix[c2][c4] - matrix[c1][c2] - matrix[c3][c4];

            if (delta < 0) {
                apply2opt(i, j);
                currentCost += delta;
                iterations++;

                mask[i] = 0;
                mask[j] = 0;
                mask[(i + 1) % n] = 0;
                mask[(j + 1) % n] = 0;

                return true;
            }
        }

        return false;
    }

    private void apply2opt(int i, int j) {
        int a = Math.min(i, j);
        int b = Math.max(i, j);  
        Collections.reverse(solution.subList(a + 1, b + 1));
    }


    private double calculateCost(ArrayList<Integer> sol) {
        double cost = 0;
        for (int i = 0; i < sol.size(); i++) {
            cost += matrix[sol.get(i)][sol.get((i + 1) % sol.size())];
        }
        return cost; 
    }


    public void printMask() {
        System.out.print("Mascara DLB -> [ ");
        for (int i = 0; i < n; i++) {
            System.out.print(mask[i]);
        }
        System.out.println("]");
    }
    

    public ArrayList<Integer> getSolution() { return solution; }
    public double getCost() { return currentCost; }
    public int getIterations() { return iterations; }
}

/*
mask esta indexada por posicion, pero tras cada reverse las ciudades cambian de posicion. 
Un bit a 1 en la posicion 3 pasa a describir otra ciudad distinta de la que se evaluo. En la practica:

Puedes dejar ciudades prometedoras marcadas como apagadas.
El codigo funciona y termina, pero el DLB es menos preciso de lo que deberia.

Si tu profesor explico la mascara como "vector del tamano de la solucion", por posicion es valido y es lo mas sencillo. 
Si quieres que sea exacta, hay que indexarla por ciudad con un vector pos[] (como en mi version anterior). Preguntale cual espera. */