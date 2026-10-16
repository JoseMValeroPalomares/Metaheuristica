import algorithms.greedy;
import algorithms.randomGreedy;
import algorithms.localSearch;
import models.ExecutionLogger;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Main {

    private static final String LOG_DIR = "Practica-1/logs";

    private static String baseName(String path) {
        String f = Paths.get(path).getFileName().toString();
        int dot = f.lastIndexOf('.');
        return dot > 0 ? f.substring(0, dot) : f;
    }

    // Abre el logger solo si el algoritmo esta en "log=" del config; si no, devuelve null
    private static ExecutionLogger openLog(Configurador cfg, String alg, String name) throws IOException {
        return cfg.toLog(alg) ? new ExecutionLogger(LOG_DIR, name) : null;
    }

    private static void header(ExecutionLogger log, String file, String alg,
                               Long seed, int nCities) {
        if (log == null) return;
        log.section("EJECUCION");
        log.kv("Fecha", java.time.LocalDateTime.now());
        log.kv("Archivo", file);
        log.kv("Algoritmo", alg);
        if (seed != null) log.kv("Semilla", seed);
        log.kv("Numero de ciudades", nCities);
        log.line("");
    }

    public static void main(String[] args) {
        Configurador configurator = new Configurador("Practica-1/config-files/config.txt");

        for (int i = 0; i < configurator.getFiles().size(); i++) {
            String actualFile = configurator.getFiles().get(i);
            String base = baseName(actualFile);

            System.out.println("\n========================================");
            System.out.println("Procesando archivo: " + actualFile);
            System.out.println("========================================");

            filesReader file = new filesReader(actualFile);
            double[][] distanceMatrix = file.getDistanceMatrix();
            int nCities = distanceMatrix.length;

            // soluciones del greedy aleatorio por semilla (una por archivo)
            Map<Long, randomGreedy> initialSolutions = new HashMap<>();

            for (int j = 0; j < configurator.getAlgorithms().size(); j++) {
                String alg = configurator.getAlgorithms().get(j);
                boolean term = configurator.toTerminal(alg);

                if (term) System.out.println("\n--- Ejecutando algoritmo: " + alg + " ---");

                try {
                    switch (alg.toLowerCase()) {

                        case "greedy": {
                            try (ExecutionLogger log = openLog(configurator, alg, base + "_" + alg)) {
                                header(log, actualFile, alg, null, nCities);

                                long t0 = System.nanoTime();
                                greedy g = new greedy(distanceMatrix);
                                double ms = (System.nanoTime() - t0) / 1_000_000.0;
                                String time = String.format(Locale.US, "%.4f", ms);

                                if (log != null) {
                                    log.kv("Ruta", ExecutionLogger.route(g.getRute()));
                                    log.kv("Coste", g.getCost());
                                    log.kv("Tiempo (ms)", time);
                                }
                                if (term) {
                                    System.out.println("Coste: " + g.getCost());
                                    System.out.println("Tiempo: " + time + " ms");
                                }
                            }
                            break;
                        }

                        case "random_greedy": {
                            for (int s = 0; s < configurator.getSeeds().size(); s++) {
                                long seed = configurator.getSeeds().get(s);
                                if (term) System.out.println("\n--- Ejecutando semilla: " + s + " ---");

                                try (ExecutionLogger log = openLog(configurator, alg,
                                        base + "_" + alg + "_seed" + seed)) {
                                    header(log, actualFile, alg, seed, nCities);

                                    long t0 = System.nanoTime();
                                    randomGreedy rg = new randomGreedy(distanceMatrix, seed);
                                    double ms = (System.nanoTime() - t0) / 1_000_000.0;
                                    String time = String.format(Locale.US, "%.4f", ms);

                                    initialSolutions.put(seed, rg); // se guarda para local_search

                                    if (log != null) {
                                        log.kv("Ciudad inicial", rg.getInitialCity());
                                        log.kv("Ruta", ExecutionLogger.route(rg.getRute()));
                                        log.kv("Coste", rg.getCost());
                                        log.kv("Tiempo (ms)", time);
                                    }
                                    if (term) {
                                        System.out.println("Coste: " + rg.getCost());
                                        System.out.println("Tiempo: " + time + " ms");
                                    }
                                }
                            }
                            break;
                        }

                        case "local_search": {
                            for (int s = 0; s < configurator.getSeeds().size(); s++) {
                                long seed = configurator.getSeeds().get(s);
                                if (term) System.out.println("\n--- Ejecutando semilla: " + s + " ---");

                                try (ExecutionLogger log = openLog(configurator, alg,
                                        base + "_" + alg + "_seed" + seed)) {
                                    header(log, actualFile, alg, seed, nCities);

                                    // reutiliza el greedy aleatorio si ya existe
                                    randomGreedy init = initialSolutions.get(seed);
                                    if (init == null) {
                                        init = new randomGreedy(distanceMatrix, seed);
                                        initialSolutions.put(seed, init);
                                    }

                                    if (log != null) {
                                        log.kv("Ciudad inicial", init.getInitialCity());
                                        log.kv("Ruta inicial", ExecutionLogger.route(init.getRute()));
                                        log.kv("Coste inicial", init.getCost());
                                        log.line("Movimientos:");
                                    }

                                    // el cronometro mide solo la busqueda local
                                    long t0 = System.nanoTime();
                                    localSearch ls = new localSearch(init.getRute(), distanceMatrix, log);
                                    double ms = (System.nanoTime() - t0) / 1_000_000.0;
                                    String time = String.format(Locale.US, "%.4f", ms);

                                    if (log != null) {
                                        log.kv("Ruta final", ExecutionLogger.route(ls.getSolution()));
                                        log.kv("Coste final", String.format(Locale.US, "%.2f", ls.getCost()));
                                        log.kv("Iteraciones", ls.getIterations());
                                        log.kv("Tiempo (ms)", time);
                                    }
                                    if (term) {
                                        System.out.println("Coste inicial (greedy aleatorio): " + init.getCost());
                                        System.out.println("Coste final (busqueda local): " + ls.getCost());
                                        System.out.println("Iteraciones: " + ls.getIterations());
                                        System.out.println("Tiempo (solo busqueda local): " + time + " ms");
                                    }
                                }
                            }
                            break;
                        }

                        default:
                            System.out.println("Algoritmo desconocido: " + alg);
                    }
                } catch (IOException e) {
                    System.out.println("Error con el log: " + e);
                }
            }
        }
    }
}