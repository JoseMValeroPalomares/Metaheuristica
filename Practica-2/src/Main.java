import models.ExecutionLogger;
import java.io.IOException;
import java.nio.file.Paths;
import algorithms.genAlg;


public class Main {

    private static final String LOG_DIR = "logs";

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
        Configurador configurator = new Configurador("config-files/config.txt");

        for (int i = 0; i < configurator.getFiles().size(); i++) {
            String actualFile = configurator.getFiles().get(i);
            String base = baseName(actualFile);

            System.out.println("\n========================================");
            System.out.println("Procesando archivo: " + actualFile);
            System.out.println("========================================");

            filesReader file = new filesReader(actualFile);
            double[][] distanceMatrix = file.getDistanceMatrix();
            int nCities = distanceMatrix.length;


            for (int j = 0; j < configurator.getAlgorithms().size(); j++) {
                String alg = configurator.getAlgorithms().get(j);
                boolean term = configurator.toTerminal(alg);

                if (term) System.out.println("\n--- Ejecutando algoritmo: " + alg + " ---");

                try {
                    switch (alg.toLowerCase()) {
                        case "gen_alg": {
                            for (int s = 0; s < configurator.getSeeds().size(); s++) {
                                long seed = configurator.getSeeds().get(s);

                                for (int c = 0; c < configurator.get_cruces().size(); c++) {
                                    String cruce = configurator.get_cruces().get(c);

                                    for (int e = 0; e < configurator.get_Es().size(); e++) {
                                        int E = configurator.get_Es().get(e);

                                        for (int b = 0; b < configurator.get_kBests().size(); b++) {
                                            int kBest = configurator.get_kBests().get(b);

                                            for (int w = 0; w < configurator.get_kWorsts().size(); w++) {
                                                int kWorst = configurator.get_kWorsts().get(w);

                                                String nombreLog = base + "_GEN_" + cruce + "_E" + E
                                                                + "_kB" + kBest + "_kW" + kWorst + "_s" + seed;

                                                try (ExecutionLogger log = openLog(configurator, alg, nombreLog)) {
                                                    header(log, actualFile, alg, null, nCities);

                                                    if (log != null) {
                                                        log.kv("Configuracion", "Cruce=" + cruce + " M=100 E=" + E
                                                                            + " kBest=" + kBest + " kWorst=" + kWorst);
                                                        log.line("");
                                                    }

                                                    genAlg gen_alg = new genAlg(distanceMatrix, E, kBest, kWorst, cruce, seed);
                                                    gen_alg.setLogger(log); 
                                                    gen_alg.ejecutar();

                                                    if (term) {
                                                        gen_alg.mostrarPoblacion();
                                                    }
                                                }
                                        
                                            }
                                        
                                        }
                                        
                                    }
                                }

                                System.out.println("");
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