
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Configurador {

    ArrayList<String> files;
    ArrayList<String> algorithms;
    ArrayList<Long> seeds;
    ArrayList<String> logAlgorithms;
    ArrayList<String> terminalAlgorithms;
    int tabuIterations = 10000;
    double tabuStagnation = 5;     // porcentaje
    int tabuTenure = 10;
    double tabuOscillation = 50;   // porcentaje

    public Configurador(String rute) {
        files = new ArrayList<>();
        algorithms = new ArrayList<>();
        seeds = new ArrayList<>();
        logAlgorithms = new ArrayList<>();
        terminalAlgorithms = new ArrayList<>();
        String line;
        FileReader f = null;
        

        try {
            f = new FileReader(rute);
            BufferedReader b = new BufferedReader(f);
            while ((line = b.readLine()) != null) {
                String[] split = line.split("=");
                switch(split[0]) {
                    case "files":
                        String[] v = split[1].split(" ");
                        for (int i = 0; i < v.length; i++) {
                            files.add(v[i]);
                        }
                        break;
                    
                    case "algorithms":
                        String[] valg = split[1].split(" ");
                        for (int i = 0; i < valg.length; i++) {
                            algorithms.add(valg[i]);
                        }
                        break;
                    
                    case "seeds":
                        String[] vseeds = split[1].split(" ");
                        for (int i = 0; i < vseeds.length; i++) {
                            seeds.add(Long.parseLong(vseeds[i]));
                        }
                        break;
                    case "log":
                        for (String a : split[1].trim().split(" ")) {
                            logAlgorithms.add(a.toLowerCase());
                        }
                        break;
                    case "terminal":
                        for (String a : split[1].trim().split(" ")) {
                            terminalAlgorithms.add(a.toLowerCase());
                        }
                        break;
                    case "tabu_iterations":
                        tabuIterations = Integer.parseInt(split[1].trim());
                        break;

                    case "tabu_stagnation":
                        tabuStagnation = Double.parseDouble(split[1].trim());
                        break;

                    case "tabu_tenure":
                        tabuTenure = Integer.parseInt(split[1].trim());
                        break;

                    case "tabu_oscillation":
                        tabuOscillation = Double.parseDouble(split[1].trim());
    break;
                }
            }

            b.close();

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public ArrayList<String> getFiles() {
        return files;
    }


    public ArrayList<String> getAlgorithms() {
        return algorithms;
    }

    public ArrayList<Long> getSeeds() {
        return seeds;
    }

    public boolean toLog(String alg) {
    return logAlgorithms.contains("all") || logAlgorithms.contains(alg.toLowerCase());
    }

    public boolean toTerminal(String alg) {
        return terminalAlgorithms.contains("all") || terminalAlgorithms.contains(alg.toLowerCase());
    }

    public int getTabuIterations() { return tabuIterations; }
    public double getTabuStagnation() { return tabuStagnation; }
    public int getTabuTenure() { return tabuTenure; }
    public double getTabuOscillation() { return tabuOscillation; }


} 