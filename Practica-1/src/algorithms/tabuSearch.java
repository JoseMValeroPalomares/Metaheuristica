package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import models.ExecutionLogger;

public class tabuSearch {
    private static final double EPS = 1e-10;

    // Parametros config.txt
    private int maxIterations;
    private int tenure;
    private double stagnationRate;
    private double intensifyProb;
    private Random rnd;

    protected int iterations;
    protected int[] mask;                // DLB indexado por ciudad
    protected int n;
    protected double[][] matrix;
    protected int[] pos;                 // pos[ciudad] = posicion en la ruta

    // Triangulo superior (min,max) = MCP | triangulo inferior (max,min) = MLP
    protected short[][] memory;
    private ArrayList<int[]> tabuList = new ArrayList<>();   // celdas de la MCP con valor > 0

    // Las tres soluciones del enunciado
    protected ArrayList<Integer> solution;     // solucion_actual
    protected double currentCost;
    protected ArrayList<Integer> bestMoment;   // mejor_momento_actual
    protected double bestMomentCost;
    protected ArrayList<Integer> bestGlobal;   // mejor_global
    protected double bestGlobalCost;

    private int firstConvergence = -1;
    private int tsIterations = 0;
    private int restarts = 0;
    private int intensifications = 0;
    private int diversifications = 0;

    private ExecutionLogger log; // puede ser null

    public tabuSearch(ArrayList<Integer> solution, double[][] matrix, long seed,
                      int maxIterations, double stagnationPercent, int tenure,
                      double oscillationPercent, ExecutionLogger log) {
        this.maxIterations = maxIterations;
        this.tenure = tenure;
        this.stagnationRate = stagnationPercent / 100.0;
        this.intensifyProb = oscillationPercent / 100.0;
        this.rnd = new Random(seed);

        this.log = log;
        this.n = solution.size();
        this.mask = new int[n];
        this.memory = new short[n][n];
        this.solution = new ArrayList<>(solution);
        this.matrix = matrix;
        this.iterations = 0;
        this.currentCost = calculateCost(this.solution);
        this.pos = new int[n];
        for (int k = 0; k < n; k++) {
            pos[this.solution.get(k)] = k;
        }

        this.bestMoment = new ArrayList<>(this.solution);
        this.bestMomentCost = currentCost;
        this.bestGlobal = new ArrayList<>(this.solution);
        this.bestGlobalCost = currentCost;

        updateLongTermMemory();   // la solucion inicial tambien cuenta
        search();
    }

    // ================= BUCLE PRINCIPAL =================

    public void search() {
        if (log != null) {
            log.kv("Iteraciones totales", maxIterations);
            log.kv("Tenencia tabu", tenure);
            log.kv("Estancamiento", Math.max(1, (int) (maxIterations * stagnationRate)) + " movimientos sin mejorar");
            log.kv("Oscilacion (intensif.)", String.format(Locale.US, "%.0f%%", intensifyProb * 100));
            log.line("");
        }

        while (iterations < maxIterations) {

            // ---- Fase 1: busqueda local con DLB ----
            if (log != null) log.line("FASE LS (busqueda local con DLB):");
            boolean converged = localSearchPhase();
            updateBestGlobal();

            if (!converged) {
                if (log != null) log.line("  >> Se agotaron las iteraciones antes de llegar al optimo local");
                break;
            }

            if (firstConvergence < 0) firstConvergence = iterations;
            bestMoment = new ArrayList<>(solution);
            bestMomentCost = currentCost;

            if (log != null) {
                log.line(String.format(Locale.US,
                    "  >> DLB todo a 1 en iteracion %d | coste=%.4f | comienza la FASE TS",
                    iterations, currentCost));
            }

            // ---- Fase 2: busqueda tabu hasta estancamiento ----
            boolean stagnated = tabuPhase();
            if (!stagnated || iterations >= maxIterations) break;

            // ---- Oscilacion estrategica: solucion nueva y vuelta a la fase LS ----
            restart(rnd.nextDouble() < intensifyProb);
        }

        if (log != null) {
            log.line("");
            log.kv("Iteraciones", iterations);
            log.kv("Iteraciones en fase TS", tsIterations);
            log.kv("Reinicios", restarts + " (intensificacion=" + intensifications
                    + ", diversificacion=" + diversifications + ")");
        }
    }

    // ================= FASE LS (primer mejor + DLB) =================

    // Devuelve true si el DLB queda todo a 1; false si se agotan las iteraciones antes
    private boolean localSearchPhase() {
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
        return noImprovement >= n;
    }

    private boolean tryImprovement(int c1) {
        int i = pos[c1];

        for (int offset = 1; offset < n; offset++) {
            int j = (i + offset) % n;
            int c2 = solution.get(j);

            double delta = checkMove(i, j);

            if (delta < -EPS) {
                if (log != null) {
                    log.line(String.format(Locale.US,
                        "  Iter %5d | LS swap pos(%d,%d) ciudades (%d,%d) | delta=%.4f | coste %.4f -> %.4f",
                        iterations + 1, i, j, c1, c2, delta, currentCost, currentCost + delta));
                }
                apply2opt(i, j);
                currentCost += delta;
                iterations++;
                updateLongTermMemory();

                mask[c1] = 0;
                mask[c2] = 0;
                return true;
            }
        }
        return false;
    }

    // ================= FASE TS =================

    // Devuelve true si termina por estancamiento; false si se agotan las iteraciones
    private boolean tabuPhase() {
        int threshold = Math.max(1, (int) (maxIterations * stagnationRate));
        int worsening = 0;   // movimientos seguidos que no mejoran a bestMoment
        int city = 0;

        if (log != null) log.line("FASE TS (busqueda tabu):");

        while (iterations < maxIterations && worsening < threshold) {
            int c1 = city;
            int i = pos[c1];

            // 1. Mejor intercambio de esta ciudad que no sea tabu, aunque empeore
            int bestJ = -1;
            double bestDelta = Double.MAX_VALUE;

            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                if (isTabu(c1, solution.get(j))) continue;    // MCP, sin aspiracion

                double delta = checkMove(i, j);
                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestJ = j;
                }
            }

            // Todos los movimientos de esta ciudad son tabu: no se mueve nada
            if (bestJ == -1) {
                decrementTabu();
                city = (city + 1) % n;
                continue;
            }

            // 2. Se aplica el movimiento y se actualizan las memorias
            int c2 = solution.get(bestJ);
            decrementTabu();               // MCP: -1 a los movimientos tabu existentes
            apply2opt(i, bestJ);
            markTabu(c1, c2);              // MCP: el par recien intercambiado pasa a ser tabu
            updateLongTermMemory();        // MLP
            currentCost += bestDelta;
            iterations++;
            tsIterations++;

            // 3. Mejor momento, mejor global y contador de estancamiento
            if (currentCost < bestMomentCost - EPS) {
                bestMoment = new ArrayList<>(solution);
                bestMomentCost = currentCost;
                worsening = 0;
            } else {
                worsening++;
            }
            updateBestGlobal();

            if (log != null) {
                log.line(String.format(Locale.US,
                    "  Iter %5d | TS swap pos(%d,%d) ciudades (%d,%d) | delta=%.4f | actual=%.4f | mejorMomento=%.4f | mejorGlobal=%.4f | empeoramientos=%d",
                    iterations, i, bestJ, c1, c2, bestDelta,
                    currentCost, bestMomentCost, bestGlobalCost, worsening));
            }

            city = (city + 1) % n;
        }
        return worsening >= threshold;
    }

    // ================= OSCILACION ESTRATEGICA =================

    private void restart(boolean intensify) {
        double before = currentCost;

        // Solucion nueva a partir de la MLP
        solution = buildFromLongTermMemory(intensify);
        for (int k = 0; k < n; k++) {
            pos[solution.get(k)] = k;
        }
        currentCost = calculateCost(solution);

        // Se reinician todas las estructuras salvo la MLP y el mejor global
        resetShortTermMemory();
        Arrays.fill(mask, 0);
        bestMoment = new ArrayList<>(solution);
        bestMomentCost = currentCost;
        updateBestGlobal();
        updateLongTermMemory();            // la solucion nueva tambien es una solucion visitada

        restarts++;
        if (intensify) intensifications++; else diversifications++;

        if (log != null) {
            log.line(String.format(Locale.US,
                "  >> ESTANCAMIENTO en iteracion %d: reinicio por %s | coste %.4f -> %.4f | mejorGlobal=%.4f",
                iterations, intensify ? "INTENSIFICACION" : "DIVERSIFICACION",
                before, currentCost, bestGlobalCost));
            log.line("");
        }
    }

    // Intensificar -> siguiente ciudad por el arco MAS frecuente
    // Diversificar -> siguiente ciudad por el arco MENOS frecuente
    // En caso de empate, la ciudad mas cercana. La ciudad de partida es aleatoria.
    private ArrayList<Integer> buildFromLongTermMemory(boolean intensify) {
        ArrayList<Integer> route = new ArrayList<>(n);
        boolean[] visited = new boolean[n];

        int city = rnd.nextInt(n);
        route.add(city);
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
            route.add(best);
            visited[best] = true;
            city = best;
        }
        return route;
    }

    // ================= MCP (triangulo superior) =================

    private boolean isTabu(int x, int y) {
        return memory[Math.min(x, y)][Math.max(x, y)] > 0;
    }

    private void markTabu(int x, int y) {
        int a = Math.min(x, y), b = Math.max(x, y);
        if (memory[a][b] == 0) tabuList.add(new int[]{a, b});
        memory[a][b] = (short) tenure;
    }

    private void decrementTabu() {
        for (int k = tabuList.size() - 1; k >= 0; k--) {
            int[] cell = tabuList.get(k);
            memory[cell[0]][cell[1]]--;
            if (memory[cell[0]][cell[1]] <= 0) {
                memory[cell[0]][cell[1]] = 0;
                tabuList.remove(k);
            }
        }
    }

    // Vacia la MCP sin tocar la MLP
    private void resetShortTermMemory() {
        for (int[] cell : tabuList) {
            memory[cell[0]][cell[1]] = 0;
        }
        tabuList.clear();
    }

    // ================= MLP (triangulo inferior) =================

    private void updateLongTermMemory() {
        for (int k = 0; k < n; k++) {
            int a = solution.get(k);
            int b = solution.get((k + 1) % n);
            memory[Math.max(a, b)][Math.min(a, b)]++;
        }
    }

    private int frequency(int x, int y) {
        return memory[Math.max(x, y)][Math.min(x, y)];
    }

    // Comprobacion: debe valer n * (iteraciones + 1 + reinicios)
    public long longTermMemorySum() {
        long sum = 0;
        for (int a = 0; a < n; a++) {
            for (int b = 0; b < a; b++) {
                sum += memory[a][b];
            }
        }
        return sum;
    }

    // ================= OPERADOR: INTERCAMBIO DE DOS CIUDADES =================

    private double checkMove(int i, int j) {
        int a = solution.get(i);
        int b = solution.get(j);

        int pi = (i - 1 + n) % n, si = (i + 1) % n;
        int pj = (j - 1 + n) % n, sj = (j + 1) % n;

        if (si == j) {
            int p = solution.get(pi), s = solution.get(sj);
            return matrix[p][b] + matrix[a][s] - matrix[p][a] - matrix[b][s];
        }
        if (sj == i) {
            int p = solution.get(pj), s = solution.get(si);
            return matrix[p][a] + matrix[b][s] - matrix[p][b] - matrix[a][s];
        }
        int p1 = solution.get(pi), s1 = solution.get(si);
        int p2 = solution.get(pj), s2 = solution.get(sj);
        return matrix[p1][b] + matrix[b][s1] + matrix[p2][a] + matrix[a][s2]
             - matrix[p1][a] - matrix[a][s1] - matrix[p2][b] - matrix[b][s2];
    }

    private void apply2opt(int i, int j) {
        int a = solution.get(i);
        int b = solution.get(j);
        solution.set(i, b);
        solution.set(j, a);
        pos[b] = i;
        pos[a] = j;
    }

    // ================= UTILIDADES =================

    private void updateBestGlobal() {
        if (currentCost < bestGlobalCost - EPS) {
            bestGlobal = new ArrayList<>(solution);
            bestGlobalCost = currentCost;
        }
    }

    public double calculateCost(ArrayList<Integer> sol) {
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

    public ArrayList<Integer> getSolution() { return bestGlobal; }
    public double getCost() { return Math.round(bestGlobalCost * 100.0) / 100.0; }
    public int getIterations() { return iterations; }
    public int getTsIterations() { return tsIterations; }
    public int getFirstConvergence() { return firstConvergence; }
    public int getRestarts() { return restarts; }
    public int getIntensifications() { return intensifications; }
    public int getDiversifications() { return diversifications; }
}