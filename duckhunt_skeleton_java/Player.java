import java.lang.reflect.Array;
import java.util.ArrayList;

class Player {

    private final double threshold = 0.8;
    private ArrayList<HMM> standardBirds = new ArrayList<HMM>(6);
    private Round round = new Round();
    public Player() {

    }

    class Round {
        private ArrayList<HMM> birdHMM;
        private int roundNum;
        private int birdNum;
        public Round(){
            this.roundNum =-1;
        }
        public Round(int roundNum, int birdNum){
            this.roundNum = roundNum;
            this.birdNum = birdNum;
            this.birdHMM= new ArrayList<HMM>(birdNum);
        }
    }
    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */

//        System.err.println("pState.getNumBirds()"+pState.getNumBirds());
//        System.err.println("pState.getNumNewTurns()"+pState.getNumNewTurns());
//        System.err.println("pState.getNumNewTurns()"+pState.getNumNewTurns());
//        System.err.println("pState.getRound()"+pState.getRound());
//        System.err.println("pState.getScore(0)"+pState.getScore(0));
        if (pState.getRound()!= round.roundNum){
            round = new Round(pState.getRound(),pState.getNumBirds());
            for(int i=0; i<round.birdNum;i++){
                Bird targetBird = pState.getBird(i);
                int[] obsSeq = readObsSeq(targetBird);
                round.birdHMM.add(i,new HMM(obsSeq));
            }
        }
        else{
            for(int i=0; i<round.birdNum;i++){
                Bird targetBird = pState.getBird(i);
                if(targetBird.isAlive()){
                    int[] obsSeq = readObsSeq(targetBird);
                    HMM hmm = round.birdHMM.get(i);
                    hmm.BaumWelch(obsSeq);
                    hmm.predict_next_Obs();
                    if (hmm.predict!=10) {
                        return new Action(i,hmm.predict);
                    }
                    else{
                        return cDontShoot;
                    }
                }
            }
        }

        // This line chooses not to shoot.
        return cDontShoot;

        // This line would predict that bird 0 will move right and shoot at it.
        // return Action(0, MOVE_RIGHT);
    }

    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to guess the species of
         * each bird. This skeleton makes no guesses, better safe than sorry!
         */
        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i)
            lGuess[i] = Constants.SPECIES_UNKNOWN;

        if(round.roundNum == 0){ //No Guess
            return lGuess;
        }

        for (int i = 0; i < pState.getNumBirds(); ++i){
            double maxProb = 0.0;
            int maxIndex =0;
            int[] obserSeq = readObsSeq(pState.getBird(i));
            for (int j =0; j<6; j++){
                double prob = standardBirds.evaluate(obserSeq);
                if(prob >maxProb){
                    maxProb = prob;
                    maxIndex =j;
                }
            }

            if(pDue.remainingMs() < 100 && maxProb > 0.3 ){
                lGuess[i] = maxIndex;
            }
        }

        return lGuess;

    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {

        System.err.println("HIT BIRD!!!");
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
        for(int i = 0;i<round.birdNum;i++){
            if(pSpecies[i]>=0 && pSpecies[i] <6){
                if(standardBirds.get(pSpecies[i]) == null){
                    //Initiate standard bird
                    standardBirds.add(pSpecies[i],new HMM(readObsSeq(pState.getBird(i))));
                }
                else{
                    //Train the HMM
                    standardBirds.get(pSpecies[i]).BaumWelch(readObsSeq(pState.getBird(i)));
                }
            }
        }
    }

    public static final Action cDontShoot = new Action(-1, -1);

    private int[] readObsSeq(Bird b){
        int seqLength = b.getSeqLength();
        int[] observationSeq = new int[seqLength];
        for (int k=0; k< seqLength;k++){
            observationSeq[k]= b.getObservation(k);
        }
        return observationSeq;
    }
}
