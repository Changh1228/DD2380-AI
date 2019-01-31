impot java.util.ArrayList

public class HMM {
    private int N = 6;
    private int K = 9;
    private int T;
    private double[][] Alpha, Beta, DiGamma, Gamma;
    private double[] c, Obs;

    public double[][] A, B;
    public double[] pi;
    public int predict = 0;

    private Init(double[][] Obs){
        this.Obs = Obs;
        T = Obs.length;

        pi = new double[N];
        double rand = 0.1*Math.random();
        for (int i = 0; i < N; ++i) {
            pi[i] = (1 / pi.length +rand);
            rand = -rand;
        }

        A = new double[N][N];
        rand = 0.1*Math.random();
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < K; ++j) {
                A[i][j] = (1 / A[i].length + rand);
                rand = -rand;
            }
        }

        B = new double[N][K];
        rand = 0.1*Math.random();
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < K; ++j) {
                B[i][j] = (1 / B[i].length + rand);
                rand = -rand;
                if (j == 7) {
                    rand = 0;
                }
            }
        }

        c = new double[T];
        Alpha = new double[T][N];
        Beta = new double[T][N];


    }

    }
}
