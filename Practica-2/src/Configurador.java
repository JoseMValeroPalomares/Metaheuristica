
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
    ArrayList<String> cruces;
    ArrayList<Integer> Es;
    ArrayList<Integer> kBests;
    ArrayList<Integer> kWorsts;

    public Configurador(String rute) {
        files = new ArrayList<>();
        algorithms = new ArrayList<>();
        seeds = new ArrayList<>();
        logAlgorithms = new ArrayList<>();
        terminalAlgorithms = new ArrayList<>();
        cruces = new ArrayList<>();
        Es = new ArrayList<>();
        kBests = new ArrayList<>();
        kWorsts = new ArrayList<>();
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
                    case "tipo_cruce":
                        for (String a : split[1].trim().split(" ")) {
                            cruces.add(a.toLowerCase());
                        }
                        break;
                    case "E":
                        String[] vEs = split[1].split(" ");
                        for (int i = 0; i < vEs.length; i++) {
                            Es.add(Integer.parseInt(vEs[i]));
                        }
                        break;
                    case "kBest":
                        String[] vkBests = split[1].split(" ");
                        for (int i = 0; i < vkBests.length; i++) {
                            kBests.add(Integer.parseInt(vkBests[i]));
                        }
                        break;
                    
                    case "kWorst":
                        String[] vkWorsts = split[1].split(" ");
                        for (int i = 0; i < vkWorsts.length; i++) {
                            kWorsts.add(Integer.parseInt(vkWorsts[i]));
                        }
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

    public ArrayList<String> get_cruces() {
        return cruces;
    }

    public ArrayList<Integer> get_Es() {
        return Es;
    }

    public ArrayList<Integer> get_kBests() {
        return kBests;
    }

    public ArrayList<Integer> get_kWorsts() {
        return kWorsts;
    }

    public boolean toLog(String alg) {
    return logAlgorithms.contains("all") || logAlgorithms.contains(alg.toLowerCase());
    }

    public boolean toTerminal(String alg) {
        return terminalAlgorithms.contains("all") || terminalAlgorithms.contains(alg.toLowerCase());
    }


} 