package models;

import java.util.ArrayList;


public class Individuo {

    public ArrayList<Integer> recorrido; // orden en que se visitan las ciudades
    public double coste;
    public boolean evaluado; // true si 'coste' corresponde al recorrido actual. Sirve para no gastar evaluaciones innecesarias. Si hay copias (hijo sin cambios) su coste ya se conoce


    public Individuo(ArrayList<Integer> recorrido) {
        this.recorrido = recorrido;
        this.evaluado = false;
    }

    public Individuo(Individuo otro) {
        this.recorrido = new ArrayList<>(otro.recorrido);
        this.coste = otro.coste;
        this.evaluado = otro.evaluado;
    }

    @Override
    public String toString() {
        return "Coste: " + coste + " | Recorrido: " + recorrido;
    }
}