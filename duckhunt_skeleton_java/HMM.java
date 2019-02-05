/*
 * @Author: Hongsheng Chang
 * @Date: 2019-01-25 08:12:54
 * @Last Modified by:   Hongsheng Chang
 * @Last Modified time: 2019-01-25 08:12:54
 */
import java.util.*;
public class HMM {

    private int N = 1;
    private int K = 9;
    private int T;
    private double[] c;
    private double[][] Alpha, Beta, Gamma;
    private double[][][] DiGamma;

    public int predict = 10;
    public double[] pi ;//= {0.241896, 0.266086, 0.249153, 0.242864};
    public double[][] A;// = {{0.4, 0.2, 0.2, 0.2},{0.2, 0.4, 0.2, 0.2}, {0.2, 0.2, 0.4, 0.2}, {0.2, 0.2, 0.2, 0.4}};
    public double[][] B;// = {{0.4, 0.2, 0.2, 0.2}, {0.2, 0.4, 0.2, 0.2}, {0.2, 0.2, 0.4, 0.2}, {0.2, 0.2, 0.2, 0.4}};

    public HMM(){
        pi = new double[N];
        for (int i = 0; i < N; i++) {
            pi[i] = 1000 + Math.random()*500;
        }
        this.pi = normalize_1D(pi);


        A = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A[i][j] = 1000 + Math.random()*500;
                if (i == j) {
                    A[i][j] = A[i][j] + 5000;
                }
            }
        }
        this.A = normalize_2D(A);


        B = new double[N][K];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < K; j++) {
                B[i][j] = 1000 + Math.random()*500;
                if (i == j) {
                    B[i][j] = B[i][j] + 5000;
                }
            }
        }
        this.B = normalize_2D(B);
    }

    public void init (int[] Obs){
      this.T = Obs.length;
     // System.err.println(T);
//     System.err.println("Obs");
//     for (int i = 0; i< T; i++) {
//         System.err.print(Obs[i] + " ");
//     }
//     System.err.println(" ");
      Alpha = new double[T][N];
      Beta = new double[T][N];
      Gamma = new double[T][N];
      DiGamma = new double[T][N][N];
      c = new double[T];
    }

    public void Print_Matrix_2D(double[][]Matrix){
        int X = Matrix.length;
        int Y = Matrix[0].length;

        for (int i = 0; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                System.err.print(Matrix[i][j] + " ");
            }
            System.err.println();
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
        //System.out.println("c ");
        //System.out.print(c[0] + " ");
        for (int i = 0; i < N; i++) { // scale Alpha 0
            Alpha[0][i] *= c[0];
        }

        int min = Obs.length > T? T:Obs.length;
        for (int t = 1; t <min ; t++) { // t timestep
            c[t] = 0;
            k = Obs[t];
            for (int i = 0; i < N; i++) {
                Alpha[t][i] = 0;
                for (int j = 0; j < N; j++) {
                    //try{
                    Alpha[t][i] += ( Alpha[t-1][j] * A[j][i] * B[i][k]);
                //}
                /*catch(Exception e){
                    System.err.println(i +" "+ t +" "+ j +" " + k);
                }*/
                }
                c[t] += Alpha[t][i];
            }
            c[t] = 1.0 / c[t];
            //System.out.print(c[t] + " ");

            for (int i = 0; i < N; i++) {
                Alpha[t][i] *= c[t];
            }

        }
        /*System.out.println();
        System.out.println("Alpha");
        Print_Matrix_2D(Alpha);
        System.out.println();*/
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
        /*System.out.println("Beta");
        Print_Matrix_2D(Beta);
        System.out.println();*/
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
        /*System.out.println("Gamma");
        Print_Matrix_2D(Gamma);
        System.out.println();
        System.out.println("DiGamma");
        for (int i= 0; i < 4 ; i++) {
            System.out.println("t="+i);
            Print_Matrix_2D(DiGamma[i]);
        }*/
    }

    private void Cal_para(int[] Obs){
        //System.out.println("pi");
        for (int i = 0; i < N; i++) { // estimate pi
            pi[i] = Gamma[0][i];
            //System.out.print(pi[i]+ " ");
        }
        //System.out.println();
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
        /*System.out.println("A");
        Print_Matrix_2D(A);*/
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
        /*System.out.println("B");
        Print_Matrix_2D(B);*/
    }

    public void BaumWelch(int[] Obs){ // run prediction
        //Init(double[] Obs);
        init(Obs);
        double oldlogProb = Integer.MIN_VALUE;
        double logProb = Integer.MIN_VALUE + 1;
        for (int it = 0; it < 200 && logProb > oldlogProb+0.001; it++) {
            //System.out.println("oldlogProb" + oldlogProb +" "+ logProb);
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

            avoidZero(A);
            avoidZero(B);
        }
        if(Double.isNaN(A[0][0])){
            System.err.println("!!!!!!!");
        }
    }

    public void BaumWelchForGuess(int[] Obs){ // run prediction
        //Init(double[] Obs);
        init(Obs);
        double oldlogProb = Integer.MIN_VALUE;
        double logProb = Integer.MIN_VALUE + 1;
        for (int it = 0; it < 200 && logProb > oldlogProb+0.001; it++) {
            //System.out.println("oldlogProb" + oldlogProb +" "+ logProb);
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
        avoidZero(A);
        avoidZero(B);
    }
    public double predict_next_Obs(){
        // predict next action
       double[] buff = pi;
       double[] pi_t = new double[N];
       for (int t = 0; t < T+1; t++) {
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
//        if(max==0){
//            System.err.println("pi"+pi_t[0]);
//            //Print_Matrix_2D(DiGamma[0]);
//            System.err.println("C"+ c[0]);
//
//        }
//        else
//        System.err.println("max: "+max);
       return max;
        /*int min = Obs.length > T? T:Obs.length;
        Alpha_pass(Obs); // get state right now
        double[] Prob_next_obs = new double[K];
        for (int i = 0; i< N; i++) {
            for (int j = 0; j < N; j++) {
                for (int k = 0; k < K; k++) {
                    Prob_next_obs[k] = Alpha[min-1][j] * A[j][i] * B[i][k];
                    //System.err.println("Alpha " + j + " "+Alpha[min-1][j]+ " ");
                }
            }
        }
        double[] Prob_next_obs_norm = new double[K];
        Prob_next_obs_norm = normalize_1D(Prob_next_obs);
        double max = 0;
        for (int i = 0; i < K; i++) {
            if (Prob_next_obs_norm[i] > max) {
                max = Prob_next_obs_norm[i];
                predict = i;
            }
        }
        return max;*/
    }

    public void avoidZero(double[][] matrix){
        for(int i =0; i<matrix.length;i++){
            for(int j =0; j<matrix[0].length;j++){
                if (matrix[i][j]<0.00001){
                    matrix[i][j]=0.00001;
                }
            }
        }
        normalize_2D(matrix);
    }

    public static double[] normalize_1D(double m[]) {
        double[] m2 = new double[m.length];
        double sum = sum(m);
        for (int i = 0; i < m.length; i++) {
            if (sum != 0){
                m2[i] = m[i] / sum;
            }
        }
        return m2;
    }

    public static double[][] normalize_2D(double m[][]) {
        double[][] m2 = new double[m.length][m[0].length];
        for (int row = 0; row < m.length; row++) {
            double sum = sum(m[row]);
            if (sum != 0)
                for (int col = 0; col < m[row].length; col++) {
                    m2[row][col] = m[row][col] / sum;
                }
        }
        return m2;
    }

    public static double sum(double[] prob) {
        double sum = 0;
        for (double d : prob)
            sum += d;
        return sum;
    }
    public double evaluation (int[] obserSeq){
        init(obserSeq);

        int k = obserSeq[0]; // first step t=1

        for (int i = 0; i < N; i++) {
            Alpha[0][i] = B[i][k] * pi[i];
        }
        for (int t = 1; t < T ; t++) { // t timestep
            k = obserSeq[t];
            for (int i = 0; i < N; i++) {
                Alpha[t][i] = 0;
                for (int j = 0; j < N; j++) {
                    Alpha[t][i] += ( Alpha[t-1][j] * A[j][i] * B[i][k]);
                }
            }
        }
        double sum = 0.0;
        for(int i=0; i<N;i++){
            sum += Alpha[T-1][i];
        }
        return sum;
}
}
