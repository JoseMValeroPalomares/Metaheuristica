
import model.City;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Configurador {
    ArrayList<String> name;
    ArrayList<String> comment;
    ArrayList<String> type;
    ArrayList<Long> dimension;
    ArrayList<String> ewt; // edge weight type
    ArrayList<City> cities;
    double[][] distanceMatrix;

    
    public Configurador(String rute) {
        name = new ArrayList<String>();
        comment = new ArrayList<String>();
        type = new ArrayList<String>();
        dimension = new ArrayList<Long>();
        ewt = new ArrayList<String>();
        
        // Leer archivo de configuracion
        String line;
        FileReader f=null;
        try{
            f = new FileReader(rute);
            BufferedReader b = new BufferedReader(f);
            boolean readingNodes = false; // Indica si se han empezado a leer las ciudades (nodos)

            while ((line = b.readLine()) != null) {
                if (readingNodes == false) {

                    String[] split = line.split(":");
                    switch (split[0].trim()) {
                        case "NAME":
                            name.add(split[1].trim());
                            break;
                        case "COMMENT":
                            comment.add(split[1].trim());
                            break;
                        case "TYPE":
                            type.add(split[1].trim());
                            break;
                        case "DIMENSION":
                            long int_dimension = Long.parseLong(split[1].trim());
                            dimension.add(int_dimension);
                            break;
                        case "EDGE_WEIGHT_TYPE":
                            ewt.add(split[1]);
                            break;
                        case "NODE_COORD_SECTION":
                            readingNodes = true;
                            break;
                    }

                } else {
                    if (line.trim().equals("EOF")) {
                        break;
                        
                    } else {
                        String[] split = line.split("\\s+");
                        int id = Integer.parseInt(split[0]);
                        double x = Double.parseDouble(split[1]);
                        double y = Double.parseDouble(split[2]);

                        cities.add(new City(id, x, y));
                    }
                }
            }   
            b.close();

            calcDistanceMatrix();

            
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void calcDistanceMatrix() {
        int n = cities.size();
        distanceMatrix = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                City c1 = cities.get(i);
                City c2 = cities.get(j);
                distanceMatrix[i][j] = Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
            }
        }
    }

} 