import models.City;

public class Main {
    public static void main(String[] args) {
        Configurador configurador = new Configurador("Practica-1/config-files/ch130.tsp");

        System.out.println("Nombre: " + configurador.name);
        System.out.println("Dimension: " + configurador.dimension);
        System.out.println("Tipo de peso: " + configurador.ewt);
        System.out.println("Numero de ciudades cargados: " + configurador.dimension);
        System.out.println("Numero de ciudades cargadas: " + configurador.cities.size());

        System.out.println("--- Coordenadas ---");

        for (City ciudad : configurador.cities) {
            System.out.println(ciudad);
        }

        System.out.println("--- Matriz de distancias (primeras 5x5) ---");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.printf("%.2f ", configurador.distanceMatrix[i][j]);
            }
            System.out.println();
        }
        
    }
}

// Main mayus :)
