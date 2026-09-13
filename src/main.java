

public class main {
    public static void main(String[] args) {
        Configurador configurador = new Configurador("Practica 1/a280.tsp");

        System.out.println("Nombre: " + configurador.name);
        System.out.println("Dimension: " + configurador.dimension);
        System.out.println("Tipo de peso: " + configurador.ewt);
        System.out.println("Numero de ciudades cargados: " + configurador.dimension);
        System.out.println("Numero de ciudades cargadas: " + configurador.cities.size());

        System.out.println("--- Coordenadas ---");

        for (City ciudad : configurador.cities) {
            System.out.println(ciudad);
        }
    }
}
