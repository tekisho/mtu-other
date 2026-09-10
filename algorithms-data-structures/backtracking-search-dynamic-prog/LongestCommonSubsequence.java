package assignment2;

public class LongestCommonSubsequence {
    private final String X;
    private final String Y;

    public LongestCommonSubsequence(String X, String Y)
    {
        this.X = X;
        this.Y = Y;
    }

    public String compare() {
        char[] Xc = this.X.toCharArray();
        char[] Yc = this.Y.toCharArray();

        int m = X.length(), n = Y.length();
        int[][] c = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            c[i][0] = 0;
        }
        for (int j = 0; j <= n; j++) {
            c[0][j] = 0;
        }
        for (int i = 1; i <= m ; i++) {
            for (int j = 1; j <= n; j++) {
                if (Xc[i - 1] == Yc[j - 1]) {
                    c[i][j] = c[i - 1][j - 1] + 1;
                } else if (c[i - 1][j] >= c[i][j - 1]) {
                    c[i][j] = c[i - 1][j];
                } else {
                    c[i][j] = c[i][j - 1];
                }
            }
        }

        int length = c[m][n];
        char[] Z = new char[length];
        build_lcs(Z, length - 1, c, Xc, Yc, m, n);
        return new String(Z);
    }
    private void build_lcs(char[] Z, int k, int[][] c, char[] X, char[] Y, int i, int j) {
        if (i == 0 || j == 0)
            return;
        if (X[i - 1] == Y[j - 1]) {
            Z[k--] = X[i - 1];
            build_lcs(Z, k, c, X, Y, i - 1, j - 1);
        } else if (c[i - 1][j] >= c[i][j - 1]) {
            build_lcs(Z, k, c, X, Y, i - 1, j);
        } else {
            build_lcs(Z, k, c, X, Y, i, j - 1);
        }
    }

    public static void main(String[] args) {
        String X = "ABCBDAB";
        String Y = "BDCABA";
        String Z = new LongestCommonSubsequence(X,Y).compare();
        System.out.println("The longest common subsequence of '" +X + "' and '" + Y + "' is '" + Z + "'.");
    }
}
