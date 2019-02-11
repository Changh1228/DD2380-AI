/*
 * @Author: Hongsheng Chang
 * @Date: 2019-01-25 08:12:54
 * @Last Modified by:   Hongsheng Chang
 * @Last Modified time: 2019-01-25 08:12:54
 */
import java.util.*;
public class HMM {

    private int N = 10;
    private int K = 9;
    private int T;
    private double[] c;
    private double[][] Alpha, Beta, Gamma;
    private double[][][] DiGamma;

    public int predict = 0;
    public double[] pi ;//= {0.241896, 0.266086, 0.249153, 0.242864};
    public double[][] A;// = {{0.4, 0.2, 0.2, 0.2},{0.2, 0.4, 0.2, 0.2}, {0.2, 0.2, 0.4, 0.2}, {0.2, 0.2, 0.2, 0.4}};
    public double[][] B;// = {{0.4, 0.2, 0.2, 0.2}, {0.2, 0.4, 0.2, 0.2}, {0.2, 0.2, 0.4, 0.2}, {0.2, 0.2, 0.2, 0.4}};

    public HMM(int[] Obs){
        T = Obs.length;
        pi = new double[N];
        double rand = 0.01*Math.random();
        for (int i = 0; i < N; ++i) {
            pi[i] = (1.0/ N +rand);
            rand = -rand;
        }

        A = new double[N][N];
        rand = 0.01*Math.random();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A[i][j] = (1.0 / N + rand);
                rand = -rand;
            }
        }

        B = new double[N][K];
        for (int i = 0; i < N; i++) {
            rand = 0.01*Math.random();
            for (int j = 0; j < K; j++) {
                B[i][j] = (1.0 / K + rand);
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

    private void Print_Matrix_2D(double[][]Matrix){
        int X = Matrix.length;
        int Y = Matrix[0].length;

        for (int i = 0; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                System.out.print(Matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    private void Alpha_pass(int[] Obs){
        int k = Obs[0]; // first step t=1
        c[0] = 0;
        for (int i = 0; i < N; i++) {
            Alpha[0][i] = B[i][k] * pi[i];
            c[0] += Alpha[0][i];
        }
        c[0] = 1.0 / c[0];
        System.out.println("c ");
        System.out.print(c[0] + " ");
        for (int i = 0; i < N; i++) { // scale Alpha 0
            Alpha[0][i] *= c[0];
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
            c[t] = 1.0 / c[t];
            System.out.print(c[t] + " ");
            for (int i = 0; i < N; i++) {
                Alpha[t][i] *= c[t];
            }

        }
        System.out.println();
        System.out.println("Alpha");
        Print_Matrix_2D(Alpha);
        System.out.println();
    }

    private void Beta_pass(int[] Obs){
        for (int i = 0; i < N; i++) { // T timestep
            Beta[T-1][i] = 1.0 / c[T-1];
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
        System.out.println("Beta");
        Print_Matrix_2D(Beta);
        System.out.println();
    }

    private void Cal_Gammas(int[] Obs){
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
        System.out.println("Gamma");
        Print_Matrix_2D(Gamma);
        System.out.println();

        System.out.println("DiGamma");
        for (int i= 0; i < 2 ; i++) {
            System.out.println("t="+i);
            Print_Matrix_2D(DiGamma[i]);
        }
    }

    private void Cal_para(int[] Obs){
        System.out.println("pi");
        for (int i = 0; i < N; i++) { // estimate pi
            pi[i] = Gamma[0][i];
            System.out.print(pi[i]+ " ");
        }
        System.out.println();
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
        System.out.println("A");
        Print_Matrix_2D(A);
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
        System.out.println("B");
        Print_Matrix_2D(B);
    }

    public void BaumWelch(int[] Obs){ // run prediction
        //Init(double[] Obs);
        double oldlogProb = Integer.MIN_VALUE;
        double logProb = Integer.MIN_VALUE + 1;
        for (int it = 0; it < 999999 && logProb > oldlogProb+0.001; it++) {
            System.out.println(" ");
            System.out.println("it=" +it);
            System.out.println("oldlogProb" + oldlogProb +" "+ logProb);
            Alpha_pass(Obs);
            Beta_pass(Obs);
            Cal_Gammas(Obs);
            Cal_para(Obs);

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
            for (int i=0; i<N; i++) {
                pi_t[i]=0;// init pit
            }
            for (int i = 0; i < N; i++) { // Cal hidden state
                for (int j = 0; j < N; j++) {
                    pi_t[i] += ( buff[j] * A[j][i] );
                }
            }
            buff = Arrays.copyOf(pi_t,N);

        }
        double[] Obs_t = new double[K];
        double max = 0;
        System.out.println();
        for (int i = 0; i < K; i++) { // Cal final Obs prob
            Obs_t[i] = 0;
            for (int j = 0; j < N; j++) {
                Obs_t[i] += ( pi_t[j] * B[j][i] );
            }
            System.out.println(Obs_t[i]);
            if (Obs_t[i] > max) { // find max index of Obs_t
                max = Obs_t[i];

                predict = i;
            }
        }
        if (max < 0.3 ) {
            predict = 10; // not sure don't shoot
        }

        return predict;


    }
    public double evaluation (int[] obserSeq){
        //this.Obs =obserSeq;
        Alpha_pass(obserSeq);
        double sum = 0.0;
        for(int i=0; i<N;i++){
            sum += Alpha[T][i];
        }
        return sum;
    }
}
