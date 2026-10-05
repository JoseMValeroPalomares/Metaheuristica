package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import models.ExecutionLogger;

public class localSearch {
    private static final int MAX_ITERATIONS = 10000;

    protected int iterations;
    protected int[] mask;
    protected int n;
    protected ArrayList<Integer> solution;
    protected double[][] matrix;
    protected double currentCost;
    protected int[] pos; // pos[ciudad] = posicion de la ciudad en la ruta

    private ExecutionLogger log; // puede ser null

    public localSearch(ArrayList<Integer> solution, double[][] matrix) {
        this(solution, matrix, null);
    }

    public localSearch(ArrayList<Integer> solution, double[][] matrix, ExecutionLogger log) {
        this.log = log;
        this.n = solution.size();
        this.mask = new int[n];
        this.solution = new ArrayList<>(solution);
        this.matrix = matrix;
        this.iterations = 0;
        this.currentCost = calculateCost(this.solution);
        this.pos = new int[n];
        for (int k = 0; k < n; k++) {
            pos[this.solution.get(k)] = k;
        }
        search();
    }

    public void search() {
        int city = 0;
        int noImprovement = 0;

        while (noImprovement < n && iterations < MAX_ITERATIONS) {
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
            String motivo = (iterations >= MAX_ITERATIONS)
                    ? "maximo de iteraciones (" + MAX_ITERATIONS + ")"
                    : "optimo local (todos los bits DLB a 1)";
            log.kv("Motivo de parada", motivo);
            log.kv("Mascara DLB final", Arrays.toString(mask));
        }
    }

    private boolean tryImprovement(int c1) {
        int i = pos[c1];
        int c2 = solution.get((i + 1) % n);

        for (int offset = 2; offset <= n - 2; offset++) {
            int j = (i + offset) % n;

            int c3 = solution.get(j);
            int c4 = solution.get((j + 1) % n);

            double delta = matrix[c1][c3] + matrix[c2][c4] - matrix[c1][c2] - matrix[c3][c4];

            if (delta < 0) {
                if (log != null) {
                    log.line(String.format(Locale.US,
                        "  Iter %4d | 2-opt pos(%d,%d) | quita (%d-%d),(%d-%d) | anade (%d-%d),(%d-%d) | delta=%.4f | coste %.4f -> %.4f",
                        iterations + 1, i, j,
                        c1, c2, c3, c4,
                        c1, c3, c2, c4,
                        delta, currentCost, currentCost + delta));
                }
                apply2opt(i, j);
                currentCost += delta;
                iterations++;
                mask[c1] = 0;
                mask[c2] = 0;
                mask[c3] = 0;
                mask[c4] = 0;
                return true;
            }
        }

        return false;
    }

    private void apply2opt(int i, int j) {
        int a = Math.min(i, j);
        int b = Math.max(i, j);

        int l = a + 1;
        int r = b;
        while (l < r) {
            int cl = solution.get(l);
            int cr = solution.get(r);

            solution.set(l, cr);
            solution.set(r, cl);

            pos[cr] = l;
            pos[cl] = r;

            l++;
            r--;
        }
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