/*
 * @Author: Hongsheng Chang
 * @Date: 2019-01-25 08:12:54
 * @Last Modified by:   Hongsheng Chang
 * @Last Modified time: 2019-01-25 08:12:54
 */

public class HMM {

    private int N = 6;
    private int K = 9;
    private int T;
    private double[] c;
    private int[] Obs;
    private double[][] Alpha, Beta, Gamma;
    private double[][][] DiGamma;

    public int predict = 0;
    public double[] pi;
    public double[][] A, B;

    public HMM(int[] Obs){
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
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < K; j++) {
                A[i][j] = (1 / A[i].length + rand);
                rand = -rand;
            }
        }

        B = new double[N][K];
        rand = 0.1*Math.random();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < K; j++) {
                B[i][j] = (1 / B[i].length + rand);
                rand = -rand;
                if (j == 7) {
                    rand = 0;
                }
            }
        }
        Alpha = new double[T][N];
        Beta = new double[T][N];
        Gamma = new double[T][N];
        DiGamma = new double[T][N][N];
        c = new double[T];
    }

    private void Alpha_pass(){
        int k = Obs[0]; // first step t=1
        c[0] = 0;
        for (int i = 0; i < N; i++) {
            Alpha[0][i] = B[i][k] * pi[i];
            c[0] += Alpha[0][i];
        }
        c[0] = 1 / c[0];
        for (int i = 0; i < N; i++) { // scale Alpha 0
            A[0][i] *= c[0];
        }

        for (int t = 1; t < T; t++) { // t timestep
            c[t] = 0;
            k = Obs[t];
            for (int i = 0; i < N; i++) {
                Alpha[t][i] = 0;
                for (int j = 0; j < N; j++) {
                    Alpha[t][i] += ( Alpha[t-1][j] * A[j][i] * B[i][k] );
                }
                c[t] += Alpha[t][i];
            }
            c[t] = 1 / c[t];
            for (int i = 0; i < N; i++) {
                A[t][i] *= c[t];
            }
        }
    }

    private void Beta_pass(){
        for (int i = 0; i < N; i++) { // T timestep
            Beta[T-1][i] = 1 / c[T-1];
        }

        for (int t = T-2; t >= 0; t--) { // t timestep
            int k = Obs[t+1];
            for (int i = 0; i < N; i++) {
                Beta[t][i] = 0;
                for (int j = 0; j < N; j++) {
                    Beta[t][i] += ( A[i][j] * B[j][k] * Beta[t+1][j]);
                }
                Beta[t][i] *= c[t];
            }
        }
    }

    private void Cal_Gammas(){
        for (int t = 0; t < T-1; t++) { // Gammma t
            double denom = 0;
            int k = Obs[t+1];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    denom += ( Alpha[t][i] * A[i][j] * B[j][k] * Beta[t+1][j] );
                }
            }
            for (int i = 0; i < N; i++) {
                Gamma[t][i] = 0;
                for (int j = 0; j < N; j++) {
                    DiGamma[t][i][j] = (Alpha[t][i] * A[i][j] * B[j][k] *
                                        Beta[t+1][j] ) / denom;
                    Gamma[t][i] += DiGamma[t][i][j];
                }
            }
        }

        double denom = 0; // special case for Gamma T-1
        for (int i = 0; i < N; i++) {
            denom += Alpha[T-1][i];
        }
        for (int i = 0; i < N; i++) {
            Gamma[T-1][i] = Alpha[T-1][i] / denom;
        }
    }

    private void Cal_para(){
        for (int i = 0; i < N; i++) { // estimate pi
            pi[i] = Gamma[0][i];
        }
        for (int i = 0; i < N; i++) { // estimate A
            for (int j = 0; j < N; j++) {
                double number = 0;
                double denom = 0;
                for (int t = 0; t < T-1; t++) {
                    number += DiGamma[t][i][j];
                    denom += Gamma[t][i];
                }
                A[i][j] = number / denom;
            }
        }
        for (int i = 0; i < N; i++) { //estimate B
            for (int j = 0; j < K; j++) {
                double number = 0;
                double denom = 0;
                for (int t = 0; t < T-1; t++){
                    if (Obs[t] == j) {
                        number += Gamma[t][i];
                    }
                    denom += Gamma[t][i];
                }
                B[i][j] = number/denom;
            }
        }
    }

    public void BaumWelch(int[] Obs){ // run prediction
        //Init(double[] Obs);
        double oldlogProb = Integer.MIN_VALUE;
        double logProb = Integer.MIN_VALUE + 1;

        for (int it = 0; it < 99999 && logProb > oldlogProb; it++) {
            Alpha_pass();
            Beta_pass();
            Cal_Gammas();
            Cal_para();

            oldlogProb = logProb; // cal logProb
            logProb = 0;
            for (int i = 0; i < T; i++) {
                logProb += Math.log(c[i]);
            }
            logProb = -logProb;
        }
    }
    public int predict_next_Obs(){
        // predict next action
        double[] buff = pi;
        double[] pi_t = new double[N];
        for (int t = 0; t < T; t++) {
            for (int i = 0; i < N; i++) { // Cal hidden state
                pi_t[i] = 0;
                for (int j = 0; j < N; j++) {
                    pi_t[i] += ( buff[j] * A[j][i] );
                }
            }
            buff = pi_t;
        }
        double[] Obs_t = new double[K];
        double max = 0;
        for (int i = 0; i < K; i++) { // Cal final Obs prob
            Obs_t[i] = 0;
            for (int j = 0; j < N; j++) {
                Obs_t[i] += ( pi_t[j] * B[j][i] );
            }
            if (Obs_t[i] > max) { // find max index of Obs_t
                max = Obs_t[i];
                predict = i;
            }
        }
        if (max < 0.5) {
            predict = 10; // not sure don't shoot
        }
        return predict;
    }
}
