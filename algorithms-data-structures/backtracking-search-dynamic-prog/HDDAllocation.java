package assignment2;

import java.util.Arrays;

public class HDDAllocation {
    private final int[] hdds;
    private final int[] files;

    public HDDAllocation(int[] hdds, int[] files)
    {
        this.hdds = hdds;
        this.files = files;
    }

//    public int[] generate_allocation() {
//        int[] p = new int[files.length];
//        Arrays.fill(p, -1);
//        int c = 0;
//        return generate_allocation(p, c);
//    }

//    public int[] generate_allocation(int[] p, int c) {
//        if (c == p.length) {
//            return p;
//        } else {
//            for (int s = 0; s < hdds.length; s++) {
//                int spaceUsed = 0;
//                for (int i = 0; i < p.length; i++) {
//                    if (p[i] != -1 && p[i] == s) {
//                        spaceUsed += files[i];
//                    }
//                }
//                if (spaceUsed + files[c] <= hdds[s] ) {
//                    p[c] = s;
//                    int[] nd = generate_allocation(p, c + 1);
//                    if (nd != null) {
//                        return nd;
//                    } else {
//                        p[c] = -1;
//                    }
//                }
//            }
//        }
//        return null;
//    }

    public int[] generate_allocation() {
        int[] p = new int[files.length];
        Arrays.fill(p, -1);
        int c = 0;
        int[] spaceUsed = new int[hdds.length];
        return generate_allocation(p, c, spaceUsed);
    }

    public int[] generate_allocation(int[] p, int c, int[] spaceUsed) {
        if (c == p.length) {
            return p;
        }
        for (int s = 0; s < hdds.length; s++) {
            if (spaceUsed[s] + files[c] <= hdds[s]) {
                p[c] = s;
                spaceUsed[s] += files[c];
                int[] nd = generate_allocation(p, c + 1, spaceUsed);
                if (nd != null) {
                    return nd;
                } else {
                    p[c] = -1;
                    spaceUsed[s] -= files[c];
                }
            }
        }
        return null;
    }
    
    public static void main(String[] args) {
        int[] hdds = {1000, 1000, 2000};
        int[] files = {300, 200, 300, 1200, 400, 700, 700 };
        int[] allocation = new HDDAllocation(hdds, files).generate_allocation();
        for (int i=0; i<allocation.length; i++) {
            System.out.println("File "+i+" has size " + files[i] + "MB and goes on HDD"+allocation[i] + ".");
        }
        for (int j=0; j<hdds.length; j++)
        {
            int space_used = 0;
            for (int i=0; i<allocation.length; i++) {
                if (allocation[i]==j) {
                    space_used += files[i];
                }
            }
            System.out.println("HDD"+ j + " space used " + space_used + "MB / " + hdds[j] + "MB.");
        }
    }
}
