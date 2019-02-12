import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
class Player {

    private HashMap<Integer,ArrayList<HMM>> standardBirds = new HashMap<Integer,ArrayList<HMM>>();
    private Round round = new Round();

    public Player() {
    }

    class Round {
        private ArrayList<HMM> birdHMM;
        private int roundNum;
        private int birdNum;
        private int timestamp;
        private int lastHit;
        private int[] timeToWait;
        private int[][] indexBuffer;

        public Round(){
            this.roundNum =-1;
        }
        public Round(int roundNum, int birdNum){
            this.roundNum = roundNum;
            this.birdNum = birdNum;
            this.birdHMM = new ArrayList<HMM>(birdNum);
            this.timestamp = 0;
            this.lastHit = -1;
            this.timeToWait =new int[birdNum];

            indexBuffer =new int [birdNum][2];
            for(int i =0; i<birdNum; i++){
                for(int j=0; j<2;j++){
                    this.indexBuffer[i][j]=0;
                }
            }
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
        if(pState.getRound()!= round.roundNum){
            System.err.println("round name " + pState.getRound());
            round = new Round(pState.getRound(),pState.getNumBirds());
//            for(int i=0; i<round.birdNum;i++){
//                round.birdHMM.add(new HMM());
//                //System.err.println("init");
//                //round.birdHMM.get(i).Print_Matrix_2D(round.birdHMM.get(i).A);
//            }
        }
        if(round.timestamp >86){
            //System.err.println(pState.getRound()+" " + pState.getRound());
          if (pState.getRound() == round.roundNum) {
              double maxProbToHit = 0.4;
              int indexToHit = -1;
              int[] bird_spec;
              bird_spec = guess(pState, pDue);
//              for (int c = 0; c < 6; c++) {
//                  if(standardBirds.containsKey(c))
//                      System.err.println("SIZE: "+standardBirds.get(c).size());
//              }
              for(int i=0; i<round.birdNum;i++){
                  Bird targetBird = pState.getBird(i);
                  int[] obsSeq = readObsSeq(targetBird);
                  if(targetBird.isAlive() && bird_spec[i] != 5){
                      if(i==round.lastHit){
                          round.timeToWait[i]=5;
                          //System.err.println("timestamp:"+targetBird.getSeqLength());
                      }

                      if(round.timeToWait[i]>0){
                          round.timeToWait[i]-=1;
                          continue;
                      }
                      HMM hmm = new HMM();
                      if(standardBirds.containsKey(round.indexBuffer[i][0]))
//                          try {
                            hmm = standardBirds.get(round.indexBuffer[i][0]).get(round.indexBuffer[i][1]).copy();
//                          }
//                          catch(Exception e ){
//                          int[][] tmp = round.indexBuffer;
//                              System.err.println("I:" +i);
//
//                              for (int c = 0; c < 6; c++) {
//                                  System.err.println(standardBirds.get(round.indexBuffer[c][0]).size());
//                              }
//                              System.exit(-1);
//                          }
                      //System.err.println(round.indexBuffer[i][0] +  " " + round.indexBuffer[i][1]);
                      //System.err.println("Obs::::" + obsSeq.length);
                      //System.err.println(targetBird.getLastObservation());
                      hmm.BaumWelchForGuess(obsSeq);
                      round.birdHMM.add(i,hmm);
                      double prob = hmm.predict_next_Obs();
                     // System.err.println(prob);

                      //hmm.Print_Matrix_2D(hmm.A);
                      if (prob>maxProbToHit){
                          maxProbToHit = prob;
                          indexToHit = i;

                      }
                  }
              }
              if(indexToHit >-1){
                  round.lastHit = indexToHit;
                  System.err.println(indexToHit +" "+ round.birdHMM.get(indexToHit).predict);
                  return new Action(indexToHit, round.birdHMM.get(indexToHit).predict);
              }
          }
        }
        round.timestamp+=1;
        // System.err.println(timestamp);
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
        for (int i = 0; i < pState.getNumBirds(); ++i){ // num of bird
            double[] maxProb;
            maxProb = new double[6];
            int maxIndex =0;
            int maxIndex_spec = 0;
            double temp_max = 0;
            int[] obserSeq = readObsSeqForGuess(pState.getBird(i));
            int[] kList = new int[6];
            for (int j =0; j<6; j++){ // num of spec
                maxProb[j] = 0;
                if(standardBirds.get(j)!=null){
                    double max_spec = 0;
                    kList[j] =0;
                    for (int k = 0; k <standardBirds.get(j).size(); k++) { // num of model in one spec
                        double prob_spec = standardBirds.get(j).get(k).evaluation(obserSeq);
                        if(prob_spec >max_spec){ // max prob of model in one spec
                            max_spec = prob_spec;
//                            System.err.println("max_spec: "+max_spec);
//                            System.err.println("k: "+k);
                            maxIndex_spec=k;
                        }
                    }
                    kList[j] = maxIndex_spec;
                    maxProb[j] = max_spec;
                    if(max_spec >temp_max){ // max prob of spec
                        maxIndex =j;
                        temp_max = max_spec;
                    }
                }
            }
//            for(int a =0;a <maxProb.length; a++){
//
//                if(maxProb[a]!=0)
//                    maxProb[a] = 1/(- Math.log(maxProb[a]));
//                //System.err.println(maxProb[a]);
//            }
            maxProb = HMM.normalize_1D(maxProb);
            round.indexBuffer[i][0]=maxIndex;
            round.indexBuffer[i][1]=kList[maxIndex];
//            System.err.println("round.indexBuffer[i][1]: "+round.indexBuffer[i][1]);
            lGuess[i] = maxIndex;

            //System.err.println(" " + maxProb[maxIndex]);
            if (maxProb[maxIndex] < 0.22) {
                lGuess[i] = 5;
            }
        }
//
//        for(int i =0;i<lGuess.length;i++){
//            System.err.print("Guess: "+lGuess[i]);
//        }
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
                    standardBirds.put(pSpecies[i],new ArrayList<HMM>());
                    //Initiate standard bird
                }
                HMM tmp=new HMM();
                int[] dataToTrain = readObsSeqForGuess(pState.getBird(i));
                if(dataToTrain.length>80){
                    tmp.BaumWelch(dataToTrain);
                    //System.err.println("Adding model..."+pSpecies[i]);
                    standardBirds.get(pSpecies[i]).add(tmp);
                    //System.err.println("Size of list..."+standardBirds.get(pSpecies[i]).size());
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


    private int[] readObsSeqForGuess(Bird b){
        int[] oldObs = readObsSeq(b);
        int i=0;
        for(i =0; i<oldObs.length;i++){
            if(oldObs[i]==-1)
                break;
        }
        return Arrays.copyOf(oldObs,i);
    }
}
