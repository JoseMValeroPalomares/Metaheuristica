package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

import models.Individuo;

public class genAlg {
    
    // M = 100 (poblacion: 100 individuos) 80% aleatoriamente, 20% greedy aleatorio con una LRC = 10
    // Esquema generacional E = 1,2,3 (E: mejores individuos, con E=2 elijo 2 elites) Guardamos copia de los elites
    // Opeardor de selección: torneo binario con kBest = 2 y kBest = 3 aplicandose tantas veces como individuos tengamos en la población
    // Operador de cruce: con Pc = 70% se implementan el cruce de orden OX2 y el cruce MOC
    // Operador de mutación: co Pm = 10% por individuo será el operador 2-opt de la práctica-1
    // Reemplazamiento: Los 100 hijos sustituyen a toda la población. Después comprobamos si hay un hijo con exactamente el mismo recorrido
    //                      - Si lo hay, el elite ha sobrevivido: no se hace nada
    //                      - Si no: elegir kWorst (2 y 3) hijos al azar y el peor se sustituye por el elite
    // Condición de parada: número máximo de evaluaciones 50000 o 120 seg.

    private int M = 100;
    private double PORC_ALEA = 0.8;
    private int TAM_LRC = 10;
    private double PC = 0.7;
    private double PM = 0.1;
    private int MAX_EVA = 50000;
    private double MAX_SEG = 120.0;

    private int E, kBest,  kWorst;
    private String tipo_cruce;
    private long seed;
    private Random rand;

    private double[][] m_dist;
    private int n;

    private int n_evaluaciones = 0;
    private int generacion = 0;
    private List<Individuo> poblacion;
    private long inicio;


    public genAlg(double[][] m_dist, int E, int kBest, int kWorst, String tipo_cruce, long seed) {

        this.m_dist = m_dist;
        this.n = m_dist.length;
        this.E = E;
        this.kBest = kBest;
        this.kWorst = kWorst;
        this.tipo_cruce = tipo_cruce;
        this.seed = seed;
        this.poblacion = new ArrayList<>(M);
        this.rand = new Random(this.seed);
    }

    public void ejecutar() {

        inicio = System.nanoTime();

        inicializar();

        for (int i = 0; i < M; i++) {
            evaluar(poblacion.get(i));
        }

        while (!parar()) {
            generacion++;
            List<Individuo> elites = calcularElite();
            


        }
        
    }

    private void inicializar() {
        int nAleatorios = (int) Math.round(M * PORC_ALEA);

        for (int i = 0; i < nAleatorios; i++) {
            ArrayList<Integer> sol = new ArrayList<>();
            for (int j = 0; j < n; j++) sol.add(j);
            Collections.shuffle(sol, rand);

            Individuo ind = new Individuo(sol);
            poblacion.add(ind);
        }

        for (int i = nAleatorios; i < M; i++) {
            randomGreedy rg = new randomGreedy(m_dist, rand, 10);
            Individuo ind = new Individuo(rg.getRute());
            poblacion.add(ind);
        }
        
    }

    private void evaluar(Individuo ind) {
        if(!ind.evaluado) {
            ind.coste = calculateCost(ind.recorrido);
            ind.evaluado = true;
            n_evaluaciones++;
        }
        
    }

    private boolean parar() {
        double segundos = (System.nanoTime() - inicio) / 1e9;
        return n_evaluaciones >= MAX_EVA || segundos >= MAX_SEG;
    }

    private List<Individuo> calcularElite() {
        List<Individuo> ordenada = new ArrayList<>(poblacion); // copiamos la poblacion para no desordenarla
        ordenada.sort((a, b) -> Double.compare(a.coste, b.coste));

        List<Individuo> elite = new ArrayList<>();
        for (int i = 0; i < E; i++) {
            elite.add(new Individuo(ordenada.get(i)));
        }

        return elite;
    }

    private double calculateCost(ArrayList<Integer> sol) {
        double cost = 0;
        for (int i = 0; i < sol.size(); i++) {
            cost += m_dist[sol.get(i)][sol.get((i + 1) % sol.size())];
        }
        return cost;
    }

    public int get_tam_poblacion() {
        return poblacion.size();
    }

    public void mostrarPoblacion() {
        System.out.println("Cruce=" + tipo_cruce + 
        " M=" + poblacion.size() +
        " E=" + E +
        " kBest=" + kBest +
        " kWorst=" + kWorst
        );
    }
}
