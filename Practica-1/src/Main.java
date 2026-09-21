import algorithms.greedy;
import algorithms.randomGreedy;

public class Main {
    public static void main(String[] args) {
        Configurador configurator = new Configurador("Practica-1/config-files/config.txt");

        for (int i = 0; i < configurator.getFiles().size(); i++) {
            String actual_file = configurator.getFiles().get(i);

            System.out.println("\n========================================");
            System.out.println("Procesando archivo: " + actual_file);
            System.out.println("========================================");

            filesReader file = new filesReader(actual_file);

            double[][] distanceMatrix = file.getDistanceMatrix();

            for (int j = 0; j < configurator.getAlgorithms().size(); j++) {
                String actualAlgorithm = configurator.getAlgorithms().get(j);
                
                System.out.println("\n--- Ejecutando algoritmo: " + actualAlgorithm + " ---");

                
                switch (actualAlgorithm.toLowerCase()) {
                    case "greedy":
                        long startTimer = System.nanoTime();

                        greedy solverGreedy = new greedy(distanceMatrix);

                        long endTimer = System.nanoTime();

                        long timeNs = endTimer - startTimer;
                        double timeMs = timeNs / 1_000_000.0;
                        
                        String formatedTime = String.format(java.util.Locale.US, "%.4f", timeMs);
                    

                        System.out.println("Coste: " + solverGreedy.getCost());
                        System.out.println("Tiempo: " + formatedTime + " ms");
                        break;
                        
                    case "random_greedy":
                        for (int s = 0; s < configurator.getSeeds().size(); s++) {
                            System.out.println("\n--- Ejecutando semilla: " + s + " ---");
                            startTimer = System.nanoTime();

                            randomGreedy solverRandomGreedy = new randomGreedy(distanceMatrix, configurator.getSeeds().get(s));

                            endTimer = System.nanoTime();

                            timeNs = endTimer - startTimer;
                            timeMs = timeNs / 1_000_000.0;
                            
                            formatedTime = String.format(java.util.Locale.US, "%.4f", timeMs);
                        

                            System.out.println("Coste: " + solverRandomGreedy.getCost());
                            System.out.println("Tiempo: " + formatedTime + " ms");
                        }
                        break;
                }
            }
        }

    }
}
