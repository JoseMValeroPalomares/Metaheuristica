package algorithms;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import models.ExecutionLogger;

public class tabuSearch {
    private final int maxIterations;
    private final int tenure;
    private final double stagnationRate;
    private final double intensifyProb;  
    private static final double EPS = 1e-9;

    protected int n;
    protected double[][] matrix;

    // Matriz cuadrada n x n:
    //   triangulo superior (fila < columna) = MCP (tenencia restante del arco)
    //   triangulo inferior (fila > columna) = MLP (frecuencia del arco)
    protected short[][] memory;
    private final ArrayList<int[]> tabuList = new ArrayList<>(); // celdas MCP activas

    protected int[] current, bestMoment, bestGlobal;
    protected double currentCost, bestMomentCost, bestGlobalCost;

    protected int iterations, restarts, intensifications, diversifications;

    private final Random rnd;
    private final int neighborLimit;
    private final ExecutionLogger log; // puede ser null

    // Estado temporal de la exploracion del entorno
    private double moveDelta;
    private int moveI, moveJ;

    public tabuSearch(ArrayList<Integer> solution, double[][] matrix, long seed,
                  int maxIterations, double stagnationPercent, int tenure,
                  double oscillationPercent) {
        this(solution, matrix, seed, maxIterations, stagnationPercent, tenure,
         oscillationPercent, Integer.MAX_VALUE, null);
    }

    public tabuSearch(ArrayList<Integer> solution, double[][] matrix, long seed,
                  int maxIterations, double stagnationPercent, int tenure,
                  double oscillationPercent, int neighborLimit, ExecutionLogger log) {
        this.maxIterations = maxIterations;
        this.tenure = tenure;
        this.stagnationRate = stagnationPercent / 100.0;
        this.intensifyProb = oscillationPercent / 100.0;

        this.n = solution.size();
        this.matrix = matrix;
        this.rnd = new Random(seed);
        this.neighborLimit = neighborLimit;
        this.log = log;
        this.memory = new short[n][n];

        this.current = new int[n];
        for (int k = 0; k < n; k++) current[k] = solution.get(k);
        this.currentCost = calculateCost(current);

        this.bestMoment = current.clone();
        this.bestMomentCost = currentCost;
        this.bestGlobal = current.clone();
        this.bestGlobalCost = currentCost;

        search();

        // Se recalcula para eliminar el error acumulado de sumar deltas
        this.bestGlobalCost = calculateCost(bestGlobal);
    }

    private void search() {
        if (n < 4) return;

        int threshold = (int) (maxIterations * stagnationRate);
        int sinMejora = 0;

        if (log != null) {
            log.kv("Tenencia tabu", tenure);
            log.kv("Estancamiento", threshold + " movimientos sin mejorar");
            log.kv("Limite de vecinos", neighborLimit == Integer.MAX_VALUE ? "entorno completo" : neighborLimit);
            log.line("Iteraciones:");
        }

        for (int iter = 1; iter <= maxIterations; iter++) {
            iterations = iter;

            // 1. Explorar el entorno y elegir el mejor vecino no tabu
            moveDelta = Double.MAX_VALUE;
            moveI = -1;
            moveJ = -1;
            explore();

            // 2. MCP: decrementar los movimientos tabu existentes
            decrementTabu();

            boolean moved = moveI >= 0;
            int i = moveI, j = moveJ;
            int c1 = 0, c2 = 0, c3 = 0, c4 = 0;

            if (moved) {
                c1 = current[i];
                c2 = current[i + 1];
                c3 = current[j];
                c4 = current[(j + 1) % n];

                reverse(i + 1, j);
                currentCost += moveDelta;

                // Se marcan como tabu los dos arcos eliminados (no se pueden volver a anadir)
                markTabu(c1, c2);
                markTabu(c3, c4);
            }

            // 3. MLP: sumar 1 a cada arco de la solucion actual
            updateLongTermMemory();

            // 4. Actualizar mejor momento y mejor global
            if (currentCost < bestMomentCost - EPS) {
                bestMoment = current.clone();
                bestMomentCost = currentCost;
                sinMejora = 0;
            } else {
                sinMejora++;
            }
            if (currentCost < bestGlobalCost - EPS) {
                bestGlobal = current.clone();
                bestGlobalCost = currentCost;
            }

            if (log != null) {
                if (moved) {
                    log.line(String.format(Locale.US,
                        "  Iter %5d | 2-opt pos(%d,%d) | quita (%d-%d),(%d-%d) | añade (%d-%d),(%d-%d) | delta=%.4f | actual=%.4f | mejorMomento=%.4f | mejorGlobal=%.4f | sinMejora=%d",
                        iter, i, j, c1, c2, c3, c4, c1, c3, c2, c4,
                        moveDelta, currentCost, bestMomentCost, bestGlobalCost, sinMejora));
                } else {
                    log.line(String.format(Locale.US,
                        "  Iter %5d | sin movimientos posibles (todos tabu) | actual=%.4f | sinMejora=%d",
                        iter, currentCost, sinMejora));
                }
            }

            // 5. Estancamiento: oscilacion estrategica
            if (sinMejora >= threshold && iter < maxIterations) {
                boolean intensify = rnd.nextDouble() < intensifyProb;
                restart(intensify, iter);
                sinMejora = 0;
            }
        }

        if (log != null) {
            log.line("");
            log.kv("Reinicios", restarts + " (intensificacion=" + intensifications
                    + ", diversificacion=" + diversifications + ")");
        }
    }

    // ---------- Entorno ----------

    private void explore() {
        long total = (long) n * (n - 3) / 2;

        if (total <= neighborLimit) {
            // Entorno completo
            for (int i = 0; i <= n - 3; i++) {
                for (int j = i + 2; j < n; j++) {
                    if (i == 0 && j == n - 1) continue; // arcos adyacentes (extremos)
                    evaluate(i, j);
                }
            }
        } else {
            // Entorno demasiado grande: muestra aleatoria de vecinos
            for (int k = 0; k < neighborLimit; k++) {
                int i = rnd.nextInt(n - 2);
                int j = i + 2 + rnd.nextInt(n - i - 2);
                if (i == 0 && j == n - 1) continue;
                evaluate(i, j);
            }
        }
    }

    private void evaluate(int i, int j) {
        int c1 = current[i];
        int c2 = current[i + 1];
        int c3 = current[j];
        int c4 = current[(j + 1) % n]; // si j es la ultima, el sucesor es la primera

        // El movimiento anade (c1,c3) y (c2,c4). Sin criterio de aspiracion.
        if (isTabu(c1, c3) || isTabu(c2, c4)) return;

        double delta = matrix[c1][c3] + matrix[c2][c4] - matrix[c1][c2] - matrix[c3][c4];
        if (delta < moveDelta) {
            moveDelta = delta;
            moveI = i;
            moveJ = j;
        }
    }

    private void reverse(int l, int r) {
        while (l < r) {
            int tmp = current[l];
            current[l] = current[r];
            current[r] = tmp;
            l++;
            r--;
        }
    }

    // ---------- MCP (triangulo superior) ----------

    private boolean isTabu(int x, int y) {
        int a = Math.min(x, y), b = Math.max(x, y);
        return memory[a][b] > 0;
    }

    private void markTabu(int x, int y) {
        int a = Math.min(x, y), b = Math.max(x, y);
        if (memory[a][b] == 0) tabuList.add(new int[]{a, b});
        memory[a][b] = (short) tenure;
    }

    private void decrementTabu() {
        for (int k = tabuList.size() - 1; k >= 0; k--) {
            int[] e = tabuList.get(k);
            memory[e[0]][e[1]]--;
            if (memory[e[0]][e[1]] <= 0) {
                memory[e[0]][e[1]] = 0;
                tabuList.remove(k);
            }
        }
    }

    private void resetShortTermMemory() {
        for (int[] e : tabuList) memory[e[0]][e[1]] = 0;
        tabuList.clear();
    }

    // ---------- MLP (triangulo inferior) ----------

    private void updateLongTermMemory() {
        for (int k = 0; k < n; k++) {
            int a = current[k];
            int b = current[(k + 1) % n];
            memory[Math.max(a, b)][Math.min(a, b)]++;
        }
    }

    private int frequency(int a, int b) {
        return memory[Math.max(a, b)][Math.min(a, b)];
    }

    // ---------- Oscilacion estrategica ----------

    private void restart(boolean intensify, int iter) {
        current = buildFromLongTermMemory(intensify);
        currentCost = calculateCost(current);

        // Se reinician todas las estructuras salvo la MLP y el mejor global
        resetShortTermMemory();
        bestMoment = current.clone();
        bestMomentCost = currentCost;

        if (currentCost < bestGlobalCost - EPS) {
            bestGlobal = current.clone();
            bestGlobalCost = currentCost;
        }

        restarts++;
        if (intensify) intensifications++; else diversifications++;

        if (log != null) {
            log.line(String.format(Locale.US,
                "  >> Estancamiento en iter %d: reinicio por %s | nueva solucion coste=%.4f | mejorGlobal=%.4f",
                iter, intensify ? "INTENSIFICACION" : "DIVERSIFICACION", currentCost, bestGlobalCost));
        }
    }

    // Construye una ruta con la MLP:
    //  intensificar  -> siguiente ciudad por el arco MAS frecuente
    //  diversificar  -> siguiente ciudad por el arco MENOS frecuente
    // En empate se elige la mas cercana.
    private int[] buildFromLongTermMemory(boolean intensify) {
        int[] route = new int[n];
        boolean[] visited = new boolean[n];

        int city = rnd.nextInt(n);
        route[0] = city;
        visited[city] = true;

        for (int k = 1; k < n; k++) {
            int best = -1;
            int bestFreq = 0;
            double bestDist = 0;

            for (int x = 0; x < n; x++) {
                if (visited[x]) continue;
                int f = frequency(city, x);
                double d = matrix[city][x];

                boolean better;
                if (best == -1) better = true;
                else if (f != bestFreq) better = intensify ? f > bestFreq : f < bestFreq;
                else better = d < bestDist;

                if (better) {
                    best = x;
                    bestFreq = f;
                    bestDist = d;
                }
            }
            route[k] = best;
            visited[best] = true;
            city = best;
        }
        return route;
    }

    public double calculateCost(int[] r) {
        double cost = 0;
        for (int k = 0; k < r.length; k++) {
            cost += matrix[r[k]][r[(k + 1) % r.length]];
        }
        return cost;
    }

    public ArrayList<Integer> getSolution() {
        ArrayList<Integer> res = new ArrayList<>(n);
        for (int c : bestGlobal) res.add(c);
        return res;
    }

    public double getCost() { return Math.round(bestGlobalCost * 100.0) / 100.0; }
    public int getIterations() { return iterations; }
    public int getRestarts() { return restarts; }
    public int getIntensifications() { return intensifications; }
    public int getDiversifications() { return diversifications; }
}