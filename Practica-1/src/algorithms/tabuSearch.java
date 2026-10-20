package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import models.ExecutionLogger;

public class tabuSearch {

    // Parametros config.txt
    private int maxIterations;
    private int tenure;
    private double stagnationRate;
    private double intensifyProb;

    protected ArrayList<Integer> solution; // Solucion actual
    protected ArrayList<Integer> bestMoment; // Mejor solucion encontrada    
    protected ArrayList<Integer> bestGlobal; // Mejor solucion global
    protected double currentCost;
    protected double bestMomentCost;
    protected double bestGlobalCost;

    protected int iterations;
    protected int[] mask;
    protected int n;
    protected double[][] matrix;
    protected int[] pos; // pos[ciudad] = posicion de la ciudad en la ruta

    private ExecutionLogger log; // puede ser null


    public tabuSearch(ArrayList<Integer> solution, double[][] matrix, int maxIterations, double stagnationPercent, int tenure, 
                        double oscillationPercent,ExecutionLogger log) {

        this.maxIterations = maxIterations;
        this.tenure = tenure;
        this.stagnationRate = stagnationPercent / 100.0;
        this.intensifyProb = oscillationPercent / 100.0;

        this.solution = new ArrayList<>(solution);
        this.currentCost = calculateCost(this.solution);
        this.bestMoment = new ArrayList<>(this.solution);
        this.bestGlobal = new ArrayList<>(this.solution);
        this.bestMomentCost = this.currentCost;
        this.bestGlobalCost = this.currentCost;
        
        this.matrix = matrix;
        this.iterations = 0;
        this.pos = new int[n];
        this.log = log;
        this.n = solution.size();
        this.mask = new int[n];

        for (int k = 0; k < n; k++) {
            pos[this.solution.get(k)] = k;
        }

        search();
    }

    public void search() {
        int city = 0;
        int noImprovement = 0;

        while (noImprovement < n && iterations < maxIterations) {
            if (mask[city] == 1) {
                noImprovement++;
            } else if (tryImprovement(city)) {
                noImprovement = 0;
            } else {
                noImprovement++;
                mask[city] = 1;
            }
            city = (city + 1) % n;
        }

        if (log != null) {
            String motivo = (iterations >= maxIterations)
                    ? "maximo de iteraciones (" + maxIterations + ")"
                    : "optimo local (todos los bits DLB a 1)";
            log.kv("Motivo de parada", motivo);
            log.kv("Mascara DLB final", Arrays.toString(mask));
        }
    }

    private boolean tryImprovement(int c1) {
        int i = pos[c1];

        for (int offset = 1; offset < n; offset++) {
            int j = (i + offset) % n;
            int c2 = solution.get(j);

            double delta = checkMove(i, j);

            if (delta < 0) {
                if (log != null) {
                    log.line(String.format(Locale.US,
                        "  Iter %4d | swap pos(%d,%d) ciudades (%d,%d) | delta=%.4f | coste %.4f -> %.4f",
                        iterations + 1, i, j, c1, c2,
                        delta, currentCost, currentCost + delta));
                }
                apply2opt(i, j);
                currentCost += delta;
                iterations++;

                mask[c1] = 0;
                mask[c2] = 0;
                return true;
            }
        }  
        return false;
    }

    private double checkMove(int i, int j) { // Recibe dos posiciones de la ruta y devuelve cuánto variaría el coste si intercambiaras sus ciudades:
        int a = solution.get(i);
        int b = solution.get(j);

        int pi = (i - 1 + n) % n, si = (i + 1) % n;
        int pj = (j - 1 + n) % n, sj = (j + 1) % n;

        // Caso 1: j va justo despues de i  ->  ... p a b s ...  pasa a  ... p b a s ...
        if (si == j) {
            int p = solution.get(pi), s = solution.get(sj);
            return matrix[p][b] + matrix[a][s] - matrix[p][a] - matrix[b][s];
        }

        // Caso 2: i va justo despues de j (ocurre con la ultima y la primera ciudad)
        if (sj == i) {
            int p = solution.get(pj), s = solution.get(si);
            return matrix[p][a] + matrix[b][s] - matrix[p][b] - matrix[a][s];
        }

        // Caso 3: no adyacentes -> cambian 4 arcos
        int p1 = solution.get(pi), s1 = solution.get(si);
        int p2 = solution.get(pj), s2 = solution.get(sj);
        return matrix[p1][b] + matrix[b][s1] + matrix[p2][a] + matrix[a][s2]
            - matrix[p1][a] - matrix[a][s1] - matrix[p2][b] - matrix[b][s2];
    }

    private void apply2opt(int i, int j) { // Intercambia las ciudades
        int a = solution.get(i);
        int b = solution.get(j);
        solution.set(i, b);
        solution.set(j, a);
        pos[b] = i;
        pos[a] = j;
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
    public double getCost() { return Math.round(currentCost * 100.0) / 100.0;}
    public int getIterations() { return iterations; }
}