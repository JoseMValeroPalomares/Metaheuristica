import algorithms.randomGreedy;
import models.ExecutionLogger;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class Main {

    private static final String LOG_DIR = "Practica-2/logs";

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
        Configurador configurator = new Configurador("Practica-2/config-files/config.txt");

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