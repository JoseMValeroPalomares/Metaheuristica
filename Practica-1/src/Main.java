import algorithms.greedy;

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
                        
                    // poner greedy aleatorio
                        
                    default:
                        System.out.println("Algoritmo desconocido: " + actualAlgorithm);
                        break;
                }
            }
        }

    }
}
