package algorithms;

public class genAlg {
    
    // M = 100 (poblacion: 100 individuos) 80% aleatoriamente, 20% greedy aleatorio con una lista de candidatos = 10
    // Esquema generacional E = 1,2,3 (E: mejores individuos, con E=2 elijo 2 elites) Guardamos copia de los elites
    // Opeardor de selección: torneo binario con kBest = 2 y kBest = 3 aplicandose tantas veces como individuos tengamos en la población
    // Reemplazamiento: Los 100 hijos sustituyen a toda la población. Después comprobamos si hay un hijo con exactamente el mismo recorrido
    //                      - Si lo hay, el elite ha sobrevivido: no se hace nada
    //                      - Si no: elegir kWorst (2 y 3) hijos al azar y el peor se sustituye por el elite
    // Operador de cruce: con Pc = 70% se implementan el cruce de orden OX2 y el cruce MOC
    // Operador de mutación: co Pm = 10% por individuo será el operador 2-opt de la práctica-1
    // Condición de parada: número máximo de evaluaciones 50000 o 120 seg.

    public genAlg() {

    }
}
