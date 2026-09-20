
public class Main {
    public static void main(String[] args) {
        Configurador configurator = new Configurador("Practica-1/config-files/config.txt");

        filesReader file = new filesReader(configurator.getFiles().get(0));
        file.printMatrix();

        
    }
}
