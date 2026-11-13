package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

import models.Individuo;
import models.ExecutionLogger;

public class genAlg {
    
    // M = 100 (poblacion: 100 individuos) 80% aleatoriamente, 20% greedy aleatorio con una LRC = 10
    // Esquema generacional E = 1,2,3 (E: mejores individuos, con E=2 elijo 2 elites) Guardamos copia de los elites
    // Opeardor de selección: torneo binario con kBest = 2 y kBest = 3 aplicandose tantas veces como individuos tengamos en la población
    // Operador de cruce: con Pc = 70% se implementan el cruce de orden OX2 y el cruce MOC
    // Operador de mutación: co Pm = 10% por individuo será el operador 2-opt de la práctica-1 (un swap de dos ciudades)
    // Reemplazamiento: Los 100 hijos sustituyen a toda la población. Después comprobamos si hay un hijo con el mismo coste
    //                      - Si lo hay, el elite ha sobrevivido: no se hace nada
    //                      - Si no: elegir kWorst (2 y 3) hijos al azar y el peor se sustituye por el elite
    // Condición de parada: número máximo de evaluaciones 50000 o 120 seg.

    private int M;
    private double PORC_ALEA;
    private int TAM_LRC;
    private double PC;
    private double PM;
    private int MAX_EVA;
    private double MAX_SEG;

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

    private ExecutionLogger log;

    /**
     * Crea un algoritmo genético generacional con élite para el TSP.
     *
     * @param m_dist     matriz de distancias entre ciudades (n x n)
     * @param E          número de individuos élite que se conservan entre generaciones
     * @param kBest      tamaño del torneo de selección de padres (a mayor k, mayor presión selectiva)
     * @param kWorst     tamaño del torneo de perdedores usado para decidir qué hijo sustituye el élite
     * @param tipo_cruce operador de cruce a usar: "OX2" o "MOC"
     * @param seed       semilla del generador aleatorio (permite repetir ejecuciones)
     * @param M          tamaño de la población (debe ser par, el cruce trabaja por parejas)
     * @param PORC_ALEA  proporción de la población inicial generada aleatoriamente (el resto con greedy aleatorio)
     * @param TAM_LRC    tamaño de la lista restringida de candidatos del greedy aleatorio
     * @param PC         probabilidad de cruce por pareja de padres
     * @param PM         probabilidad de mutación por individuo
     * @param MAX_EVA    número máximo de evaluaciones de la función objetivo
     * @param MAX_SEG    tiempo máximo de ejecución en segundos
     */
    public genAlg(double[][] m_dist, int E, int kBest, int kWorst, String tipo_cruce, long seed, int M, double PORC_ALEA, int TAM_LRC, double PC, double PM, int MAX_EVA, double MAX_SEG) {
        this.m_dist = m_dist;
        this.n = m_dist.length;
        this.E = E;
        this.kBest = kBest;
        this.kWorst = kWorst;
        this.tipo_cruce = tipo_cruce;
        this.seed = seed;
        this.poblacion = new ArrayList<>(M);
        this.rand = new Random(this.seed);
        this.M = M;
        this.PORC_ALEA = PORC_ALEA;
        this.TAM_LRC = TAM_LRC;
        this.PC = PC;
        this.PM = PM;
        this.MAX_EVA = MAX_EVA;
        this.MAX_SEG = MAX_SEG;
    }


    /**
     * Bucle principal del esquema generacional con élite.
     * Inicializa y evalúa P(0). Mientras no se cumpla la condición de parada:
     * calcula el élite de P(t), selecciona los padres P'(t), aplica cruce y mutación
     * para obtener los hijos Q(t), los evalúa y reemplaza P(t) por Q(t) reinsertando el élite si no ha sobrevivido.
     */
    public void ejecutar() {

        inicio = System.nanoTime();

        inicializar();

        for (int i = 0; i < M; i++) {
            evaluar(poblacion.get(i));
        }

        logPoblacionInicial(); 

        while (!parar()) {

            List<Individuo> elite = calcularElite();
            
            List<Individuo> padres = seleccion();

            List<Individuo> hijos = cruce(padres);
            
            mutacion(hijos);

            for (int i = 0; i < M; i++) {
                evaluar(hijos.get(i));
            }
            
            reemplazar(hijos, elite);

            generacion++;

            logElite();

        }

        logResumen();
        
    }


    /**
     * Genera la población inicial P(0) de M individuos.
     * Una proporción PORC_ALEA son permutaciones aleatorias de las ciudades
     * y el resto se construye con un greedy aleatorio con LRC de tamaño TAM_LRC.
     * Los individuos se crean sin evaluar.
     */
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
            randomGreedy rg = new randomGreedy(m_dist, rand, TAM_LRC);
            Individuo ind = new Individuo(rg.getRute());
            poblacion.add(ind);
        }
        
    }


    /**
     * Calcula el fitness (coste del recorrido) de un individuo si aún no lo tiene.
     * Solo cuenta como evaluación cuando realmente se calcula: los individuos
     * copiados sin cambios (padres que no se cruzan ni mutan) conservan su coste
     * y no consumen evaluaciones.
     *
     * @param ind individuo a evaluar
     */
    private void evaluar(Individuo ind) {
        if(!ind.evaluado) {
            ind.coste = calculateCost(ind.recorrido);
            ind.evaluado = true;
            n_evaluaciones++;
        }
    }


    /**
     * Condición de parada: se alcanza el máximo de evaluaciones (MAX_EVA)
     * o el tiempo máximo de ejecución (MAX_SEG), lo que ocurra antes.
     *
     * @return true si el algoritmo debe detenerse
     */
    private boolean parar() {
        double segundos = (System.nanoTime() - inicio) / 1e9;
        return n_evaluaciones >= MAX_EVA || segundos >= MAX_SEG;
    }


    /**
     * Obtiene los E mejores individuos (menor coste) de la población actual P(t).
     * Se devuelven copias para que no les afecten los cambios posteriores de la
     * población (cruce, mutación y reemplazo).
     *
     * @return lista con copias de los E individuos élite, ordenados de mejor a peor
     */
    private List<Individuo> calcularElite() {
        List<Individuo> ordenada = new ArrayList<>(poblacion); // copiamos la poblacion para no desordenarla
        ordenada.sort((a, b) -> Double.compare(a.coste, b.coste));

        List<Individuo> elite = new ArrayList<>();
        for (int i = 0; i < E; i++) {
            elite.add(new Individuo(ordenada.get(i)));
        }

        return elite;
    }


    /**
     * Selección por torneo: escoge aleatoriamente kBest individuos de la población
     * (con reemplazamiento, puede repetirse alguno) y se queda con el de mejor
     * fitness, es decir, el de menor coste.
     *
     * @return copia del individuo ganador del torneo
     */
    private Individuo torneo() {
        List<Individuo> participantes = new ArrayList<>();

        for (int i = 0; i < kBest; i++) {
            int indice = rand.nextInt(poblacion.size());
            participantes.add(poblacion.get(indice));
        }

        Individuo mejor = participantes.get(0);

        for (Individuo p : participantes) {
            if (p.coste < mejor.coste) {
                mejor = p;
            }
        }
 
        return new Individuo(mejor); // Devolvemos una copia
    }


    /**
     * Construye la población de padres P'(t) repitiendo el torneo M veces.
     *
     * @return lista de M padres (copias de individuos de P(t))
     */
    private List<Individuo> seleccion() {
        List<Individuo> padres = new ArrayList<>();

        for (int i = 0; i < M; i++) {
            padres.add(torneo());
        }

        return padres;
    }

    
    /**
     * Operación común a OX2 y MOC: copia el recorrido base y, en las posiciones
     * donde aparece alguna de las ciudades seleccionadas, las vuelve a colocar
     * en el orden en que vienen en la lista de seleccionadas. El resto de
     * ciudades se queda en su posición, por lo que el hijo siempre es una
     * permutación válida.
     *
     * @param base         recorrido del que se copian las posiciones fijas
     * @param seleccionadas ciudades a recolocar, en el orden que deben aparecer
     * @return nuevo recorrido hijo
     */
    private ArrayList<Integer> reordenar(ArrayList<Integer> base, List<Integer> seleccionadas) {
        boolean[] marcada = new boolean[n];
        for (int c : seleccionadas) marcada[c] = true;

        ArrayList<Integer> hijo = new ArrayList<>(base);
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (marcada[hijo.get(i)]) {
                hijo.set(i, seleccionadas.get(k));
                k++;
            }
        }
        return hijo;
    }


    /**
     * Genera un hijo con el cruce OX2: se eligen al azar (50%) posiciones de p1,
     * se localizan esas ciudades en p2 y se rellenan esos huecos con el orden
     * que tienen en p1. El resto de ciudades conserva la posición de p2.
     *
     * @param p1 padre del que se toma el orden de las ciudades seleccionadas
     * @param p2 padre que aporta la estructura del hijo
     * @return recorrido del hijo
     */
    private ArrayList<Integer> hijoOX2(ArrayList<Integer> p1, ArrayList<Integer> p2) {
        List<Integer> seleccionadas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (rand.nextDouble() < 0.5) { // cada posición se elige con probabilidad del 50%
                seleccionadas.add(p1.get(i));
            }
        }
        return reordenar(p2, seleccionadas);
    }


    /**
     * Cruce OX2 (order-based crossover): produce dos hijos intercambiando
     * el papel de cada padre.
     *
     * @param p1 primer padre
     * @param p2 segundo padre
     * @return array con los dos hijos (sin evaluar)
     */
    private Individuo[] OX2(Individuo p1, Individuo p2) {
        Individuo h1 = new Individuo(hijoOX2(p1.recorrido, p2.recorrido));
        Individuo h2 = new Individuo(hijoOX2(p2.recorrido, p1.recorrido));

        return new Individuo[] {h1, h2};
    }


    /**
     * Cruce MOC (Modified Order Crossover): se elige un punto de corte aleatorio.
     * El hijo 1 mantiene la estructura de p1 pero las ciudades que están a la
     * derecha del corte en p2 se recolocan en p1 con el orden de p2. El hijo 2
     * se obtiene igual cambiando los papeles de los padres.
     *
     * @param p1 primer padre
     * @param p2 segundo padre
     * @return array con los dos hijos (sin evaluar)
     */
    private Individuo[] MOC(Individuo p1, Individuo p2) {
        int corte = 1 + rand.nextInt(n - 1); // entre 1 y n-1: ninguna parte queda vacía

        // Parte derecha de cada padre (desde 'corte' hasta el final)
        List<Integer> derechaP2 = new ArrayList<>(p2.recorrido.subList(corte, n));
        List<Integer> derechaP1 = new ArrayList<>(p1.recorrido.subList(corte, n));

        Individuo h1 = new Individuo(reordenar(p1.recorrido, derechaP2));
        Individuo h2 = new Individuo(reordenar(p2.recorrido, derechaP1));
        return new Individuo[]{ h1, h2 };
    }


    /**
     * Recombina P'(t) para obtener P''(t). Los padres se emparejan de forma
     * consecutiva (0-1, 2-3, ...) y cada pareja se cruza con probabilidad PC
     * usando el operador indicado en tipo_cruce. Si no se cruzan, pasan
     * los padres tal cual (conservando su coste ya evaluado).
     *
     * @param padres población de padres P'(t), de tamaño M (par)
     * @return lista de M hijos
     */
    private List<Individuo> cruce(List<Individuo> padres) {
        List<Individuo> hijos = new ArrayList<>(M);

        for (int i = 0; i < M; i += 2) {
            Individuo p1 = padres.get(i);
            Individuo p2 = padres.get(i + 1);

            if (rand.nextDouble() < PC) {
                Individuo[] h = tipo_cruce.equalsIgnoreCase("OX2") ? OX2(p1, p2) : MOC(p1, p2);
                hijos.add(h[0]);
                hijos.add(h[1]);
            } else {
                // No se cruzan, pasan las copias de los padres (ya son copias del torneo)
                hijos.add(p1);
                hijos.add(p2);
            }
        }
        return hijos;
    }
    

    /**
     * Operador de mutación: intercambia dos ciudades distintas elegidas al azar
     * y marca el individuo como no evaluado, ya que su coste ha cambiado.
     *
     * @param h individuo a mutar (se modifica directamente)
     */
    private void mutar(Individuo h) {
        int i = rand.nextInt(n);
        int j;
        do {
            j = rand.nextInt(n);
        } while (j == i);

        Collections.swap(h.recorrido, i, j);
        h.evaluado = false;
    }


    /**
     * Aplica la mutación a cada hijo con probabilidad PM, obteniendo Q(t).
     *
     * @param hijos hijos tras el cruce, P''(t) (se modifican directamente)
     */
    private void mutacion(List<Individuo> hijos) {
        for (Individuo h : hijos) {
            if (rand.nextDouble() < PM) {
                mutar(h);
            }
        }
    }


    /**
     * Comprueba si un élite ha sobrevivido en la nueva población comparando su
     * fitness (con tolerancia 1e-6) en lugar del recorrido completo: cuesta O(M)
     * en vez de O(M·n) y además detecta recorridos rotados o invertidos, que en
     * el TSP representan la misma solución.
     *
     * @param e individuo élite a buscar
     * @return true si algún individuo de la población tiene el mismo coste
     */
    private boolean estaEnPoblacion(Individuo e) {
        for (int i = 0; i < M; i++) {
            if (Math.abs(poblacion.get(i).coste - e.coste) < 1e-6) {
                return true;
            }
        }

        return false;
    }


    /**
     * Elige al azar una posición de la población que no esté protegida, es decir,
     * donde todavía no se haya colocado un élite en esta generación. Así se evita
     * que un élite recién insertado sea sustituido por otro. Siempre termina
     * porque hay como mucho E posiciones protegidas y E es menor que M.
     *
     * @param protegido posiciones donde ya se ha insertado un élite
     * @return índice de una posición no protegida
     */
    private int posicionNoProtegida(boolean[] protegido) {
        int pos;
        do {
            pos = rand.nextInt(M);
        } while (protegido[pos]);
        return pos;
    }


    /**
     * Torneo inverso para el reemplazo: elige kWorst posiciones al azar entre las
     * no protegidas y devuelve la del individuo de peor fitness (mayor coste),
     * que será sustituido por un élite.
     *
     * @param protegido posiciones donde ya se ha insertado un élite y no pueden elegirse
     * @return posición en la población del individuo perdedor
     */
    private int torneoPerdedores(boolean[] protegido) {
        // La lista guarda posiciones y no individuos porque, después del torneo, reemplazar necesita saber dónde colocar el élite, y eso lo da la posición
        List<Integer> participantes = new ArrayList<>();
        for (int i = 0; i < kWorst; i++) {
            participantes.add(posicionNoProtegida(protegido));
        }
    
        int peor = participantes.get(0);

        for (int pos : participantes) {
            if (poblacion.get(pos).coste > poblacion.get(peor).coste) {
                peor = pos;
            }
        }

        return peor;
    }


    /**
     * Reemplazamiento generacional con élite: los hijos Q(t) sustituyen a toda
     * la población y forman P(t+1). Después, cada élite de P(t) que no esté
     * en Q(t) se reinserta en el lugar del perdedor de un torneo de kWorst
     * individuos, para no perder la mejor solución encontrada.
     *
     * @param hijos nueva generación Q(t), ya evaluada
     * @param elite copias de los E mejores individuos de P(t)
     */
    private void reemplazar(List<Individuo> hijos, List<Individuo> elite) {
        poblacion = hijos;
        boolean[] protegido = new boolean[M];

        for (int i = 0; i < E; i++) {
            if (!estaEnPoblacion(elite.get(i))) {
                int indice = torneoPerdedores(protegido);
                poblacion.set(indice, new Individuo(elite.get(i)));
                protegido[indice] = true;
            }
        }
    }


    /**
     * Función objetivo (fitness) del TSP: longitud total del ciclo, sumando
     * la distancia entre ciudades consecutivas más la vuelta de la última a
     * la primera. Se busca minimizarla.
     *
     * @param sol recorrido como permutación de las ciudades
     * @return coste total del recorrido
     */
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
        Individuo mejor = calcularElite().get(0);
        System.out.println("Cruce=" + tipo_cruce + 
        " M=" + poblacion.size() +
        " E=" + E +
        " kBest=" + kBest +
        " kWorst=" + kWorst +
        " seed=" + seed +
        " | Generaciones=" + generacion +
        " Evaluaciones=" + n_evaluaciones +
        " Mejor-coste=" + String.format("%.2f", mejor.coste) + 
        " Tiempo=" + String.format("%.3f", segundos()) + "s"
        );
    }


    // ---- MÉTODOS PARA EL LOG ----

    
    public void setLogger(ExecutionLogger log) {
        this.log = log;
    }


    private double segundos() {
        return (System.nanoTime() - inicio) / 1e9;
    }


    /** Generación 0: toda la población. */
    private void logPoblacionInicial() {
        if (log == null) return;
        int nAleatorios = (int) Math.round(M * PORC_ALEA);

        log.section("POBLACION INICIAL (generacion 0)");
        for (int i = 0; i < poblacion.size(); i++) {
            Individuo ind = poblacion.get(i);
            String tipo = (i < nAleatorios) ? "aleatorio" : "greedy";
            log.line(String.format("[%3d] %-9s coste=%.2f recorrido=%s",
                    i, tipo, ind.coste, ind.recorrido));
        }
        log.line("");
    }


    /** Resto de generaciones: solo el élite. */
    private void logElite() {
        if (log == null) return;
        List<Individuo> elite = calcularElite();

        log.line(String.format("Gen %d | evals %d | t=%.3fs",
                generacion, n_evaluaciones, segundos()));
        for (int i = 0; i < elite.size(); i++) {
            Individuo e = elite.get(i);
            log.line(String.format("   elite %d: coste=%.2f",
                    i + 1, e.coste));
        }
    }


    /** Resumen final. */
    private void logResumen() {
        if (log == null) return;
        Individuo mejor = calcularElite().get(0);

        log.section("RESULTADO FINAL");
        log.kv("Generaciones", generacion);
        log.kv("Evaluaciones", n_evaluaciones);
        log.kv("Tiempo (s)", String.format("%.3f", segundos()));
        log.kv("Mejor coste", String.format("%.2f", mejor.coste));
        log.kv("Mejor recorrido", mejor.recorrido);
    }
}
