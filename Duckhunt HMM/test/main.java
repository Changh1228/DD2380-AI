public class main {
    public static void main(String[] args) {

        int [] Obs= {7, 7, 7, 7, 7, 0, 0, 0, 0, 1, 7, 6, 1, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 8, 8, 0, 8, 6, 0, 8, 8, 0, 0, 6, 6, 0, 6, 7, 7, 7, 1, 7, 1, 6, 7, 7, 1, 7, 0, 0, 8, 2, 8, 0, 8, 6, 2, 0, 6, 6, 8 };
        int out=7;
        HMM testcase = new HMM(Obs);
        testcase.BaumWelch(Obs);
        out = testcase.predict_next_Obs();
        System.out.println(out);
    }
}
