
package algorithms;

import java.util.ArrayList;

public class localSearch {
    protected int[] mask;
    protected int n;
    protected boolean global_improvement;
    protected ArrayList<Integer> solution;
    protected double[][] matrix;

    public localSearch(ArrayList<Integer> solution, double[][] matrix) {
        this.n = solution.size();
        this.mask = new int[n]; 
        this.global_improvement = true;
        this.solution = solution;
        this.matrix = matrix;


        search();

    }


    public void search() {
        double currentCost = calculateCost(solution);
        int index = 0;

        while (global_improvement) {
            global_improvement = false;
            int unsuccessful_attempts = 0;

            while (unsuccessful_attempts < n) {
                if (mask[index] == 0) {
                    boolean improved_here = false;

                    for (int offset = 2; offset < n; offset++) {
                        int j = (index + offset) % n;

                        ArrayList<Integer> neighbor = apply2opt(solution, index, j);
                        double neighborCost = calculateCost(neighbor);

                        if (neighborCost < currentCost) {
                            solution = neighbor;
                            currentCost = neighborCost;
                            global_improvement = true;
                            improved_here = true;

                            
                            break; 
                        }
                    }

                    if (!improved_here) {
                        mask[index] = 1;
                        unsuccessful_attempts++;
                    } else {
                        unsuccessful_attempts = 0;
                    }
                } else {
                    unsuccessful_attempts++;
                }

                index = (index + 1) % n;

                if (global_improvement) {
                    break;
                }
            }
        }
    }

    private ArrayList<Integer> apply2opt(ArrayList<Integer> sol, int i, int j) {
        ArrayList<Integer> newSol = new ArrayList<>();
        
        for (int c = 0; c <= i; c++) {
            newSol.add(sol.get(c));
        }
        
        for (int c = j; c >= i + 1; c--) {
            newSol.add(sol.get(c));
        }
        
        for (int c = j + 1; c < sol.size(); c++) {
            newSol.add(sol.get(c));
        }
        
        return newSol;
    }


    private double calculateCost(ArrayList<Integer> sol) {
        double cost = 0;
        for (int i = 0; i < sol.size(); i++) {
            int actualCity = sol.get(i);
            int nextCity = sol.get((i + 1) % sol.size());
            cost += matrix[actualCity][nextCity];
        }
        return cost; 
    }


    public void printMask() {
        System.out.print("Mascara DLB -> [ ");
        for (int i = 0; i < n; i++) {
            System.out.print(mask[i]);
        }
        System.out.println("]");
    }
    

    public ArrayList<Integer> getSolution() {
        return solution;
    }
}