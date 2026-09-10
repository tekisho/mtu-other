package assignment2;

import java.util.LinkedList;

public class RodCutting {
    private final int[] prices;

    public RodCutting(int[] prices)
    {
        this.prices = prices;
    }

    public LinkedList<Integer> best_cuts()
    {
        int n = prices.length;
        int[] s = new int[n + 1];
        int[] r = new int[n + 1];

        r[0] = 0;
        int q;
        for (int j = 1; j <= n; j++) {
            q = -1;
            for (int i = 1; i <= j; i++) {
                if (q <= prices[i - 1] + r[j - i]) {
                    q = prices[i - 1] + r[j - i];
                    s[j] = i;
                }
            }
            r[j] = q;
        }

        LinkedList<Integer> cuts = new LinkedList<>();
        while (n > 0) {
            cuts.add(s[n]);
            n -= s[n];
        }
        return cuts;
    }

    public static void main(String[] args) {
        int[] prices = {  1, 5, 8, 9, 12, 14, 17, 19, 20, 21 };
        LinkedList<Integer> cuts = new RodCutting(prices).best_cuts();
        System.out.println("The best cuts for a rod of length " + prices.length + "m are");
        int total_price=0;
        for (Integer cut : cuts)
        {
            System.out.println(" - " + cut + "m selling at €"+prices[cut-1]);
            total_price += prices[cut-1];
        }
        System.out.println("The overall price is €"+total_price+".");
    }
}