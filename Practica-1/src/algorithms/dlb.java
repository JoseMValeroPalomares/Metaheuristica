package algorithms;

import java.util.ArrayList;

public class dlb {
    protected boolean[] mask;
    protected int n;

    public dlb(ArrayList<Integer> solution) {
        this.n = solution.size();
        this.mask = new boolean[n]; 
    }


    public void activate(int i) {
        mask[i] = false;
    }


    public void deactivate(int i) {
        mask[i] = true;
    }


    public boolean isIgnored(int i) {
        return mask[i];
    }

    public void reset() {
        for (int i = 0; i < n; i++) {
            mask[i] = false;
        }
    }
}