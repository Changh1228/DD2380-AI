import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


class Player_Reference {
	// /constructor

	// /There is no data in the beginning, so not much should be done here.
	//private HMM[] speciesArr;
	int[] myGuesses;
	private static final int estimations = 150;
	private int timeStep;
	private int hitBirds;
	private int shotsFired;
	private int correctGuesses;
	private int failedGuesses;
	private int lastShot;
	private LinkedList<HMM>[] birdsListArr;
	public Player_Reference() {
		timeStep = 0;
		hitBirds = 0;
		shotsFired = 0;
		correctGuesses = 0;
		failedGuesses = 0;
		lastShot = -1;
		birdsListArr = new LinkedList[Constants.COUNT_SPECIES];
		for(int i = 0; i < birdsListArr.length; i++)
			birdsListArr[i] = new LinkedList<HMM>();
	}

	//TODO: Haven't implemented
	static class PredictedMove {
		static int UNKNOWN_MOVE=-1;
		int i;
		int move;
		double max;
		double chance;
		int birdId;
		
		public PredictedMove(int i, int move, double max) {
			this.i = i;
			this.move = move;
			this.max = max;
		}
	}
	/**
	 * Shoot!
	 * 
	 * This is the function where you start your work.
	 * 
	 * You will receive a variable pState, which contains information about all
	 * birds, both dead and alive. Each birds contains all past actions.
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

		// This line choose not to shoot
		//return cDontShoot;

		// This line would predict that bird 0 will move right and shoot at it
		// return Action(0, MOVE_RIGHT);

		final int numBirds = pState.getNumBirds();

		if(timeStep == 0)
		{

			System.err.println("Round " + pState.getRound());
		}
		else if (pState.getRound() > 0 && timeStep >= 60)
		{

			Bird b;
			int[] os;
			int species;
			Iterator<HMM> it;
			double  max;
			int move;
			double[] predOs;
			LinkedList<PredictedMove> moves = new LinkedList<PredictedMove>();
			boolean unanimous;
			ArrayList<Integer> bMoves;
			for(int i = 0 ; i < numBirds; i++)
			{
				b = pState.getBird(i);
				if (b.isAlive())
				{
					os = getBirdObservations(b);
					species = idBird(os);
					max = 0;
					move = PredictedMove.UNKNOWN_MOVE;
					if (species != Constants.SPECIES_UNKNOWN && species != Constants.SPECIES_BLACK_STORK)
					{
						it = birdsListArr[species].iterator();
						bMoves = new ArrayList<Integer>();
						while(it.hasNext())
						{
							predOs = it.next().totalNextEmissionProbability(os);
							for(int j = 0 ; j < predOs.length; j++)
							{
								if(predOs[j] > max)
								{
									max = predOs[j];
									move = j;
								}
							}
							bMoves.add(move);
						}
						unanimous = true;
						for (int j = 0 ; j < bMoves.size() - 1; j++)
						{
							if(bMoves.get(j) != bMoves.get(j + 1))
							{
								unanimous = false;
								break;
							}
						}
						if(unanimous && move != PredictedMove.UNKNOWN_MOVE)
							moves.add(new PredictedMove(i,move,max));
					}
				}

				Iterator<PredictedMove> iter = moves.iterator();
				max = 0;
				move = PredictedMove.UNKNOWN_MOVE;
				PredictedMove tmp;
				int maxBird = -1;
				while(iter.hasNext())
				{
					tmp = iter.next();
					if(tmp.chance > max)
					{
						max = tmp.chance;
						move = tmp.move;
						maxBird = tmp.birdId;
					}
				}

				if(max > 0.8 && move != PredictedMove.UNKNOWN_MOVE && maxBird != -1)
				{
					System.err.println("Round " +pState.getRound()+" TimeStep " + timeStep + " SHOOOT bird " + maxBird + " chance " + max  *100 + " move " + move);
					shotsFired++;
					timeStep++;
					return new Action(maxBird,move);
				}
			}
		}
		timeStep++;
		return cDontShoot;
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
		timeStep = 0;
		int[] lGuess = new int[pState.getNumBirds()];
		if(pState.getRound() == 0)
		{
			Random r = new Random();
			System.err.println("First guess");
			for(int i = 0; i < pState.getNumBirds(); i++)
				lGuess[i] = r.nextInt(Constants.COUNT_SPECIES - 1);

		}
		else
		{

			System.err.println("Regular guess");
			Bird bird;
			int guess;
			for (int i = 0; i < pState.getNumBirds(); ++i)
			{
				bird = pState.getBird(i);
				int[] currBirdObs = getBirdObservations(bird);
				guess = idBird(currBirdObs);

				if (guess != Constants.SPECIES_UNKNOWN )
					lGuess[i] = guess;
				else
				{
					lGuess[i] = Constants.SPECIES_UNKNOWN;
					System.err.println("Failed to id bird, guessing randomly on id " + i + " guess " + lGuess[i]);
				}
			}

		}

		myGuesses = lGuess;
		return lGuess;

	}
	// If bird is alive still get all observations else get all observations before bird was killed
	private int[] getBirdObservations(Bird bird)
	{
		int[] birdObs;
		if (bird.isAlive())
		{
			birdObs = new int[bird.getSeqLength()];
			for (int j = 0; j < bird.getSeqLength(); j++)
				birdObs[j] = bird.getObservation(j);
		} else
		{
			LinkedList<Integer> aliveObservations = new LinkedList<Integer>();
			for(int i = 0 ; i < bird.getSeqLength();i++)
			{
				if(bird.wasAlive(i))
					aliveObservations.add(bird.getObservation(i));
			}

			birdObs = new int[aliveObservations.size()];
			for(int i = 0 ; i < aliveObservations.size(); i++)
				birdObs[i] = aliveObservations.pollFirst();
		}
		return birdObs;

	}
	// Returns species of bird or -1 if it couldnt id bird
	private int idBird(int[] observations)
	{
		double maxProb = 0.0, tmp;
		int bestGuess = Constants.SPECIES_UNKNOWN;
		Iterator<HMM> it;
		for(int k = 0; k < birdsListArr.length; k++)
		{
			it = birdsListArr[k].iterator();
			while(it.hasNext())
			{
				tmp = it.next().sequenceProbability(observations);
				if(tmp > maxProb)
				{
					maxProb = tmp;
					bestGuess = k;
				}
			}
		}
		return bestGuess;
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
		System.err.println("Bird " + pBird);
		hitBirds++;
		lastShot = -1;


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
		System.err.println("revealed");
		timeStep = 0;


		for  (int i = 0; i < pSpecies.length; i++)
		{
			if(pSpecies[i] == myGuesses[i])
				correctGuesses++;
			else
			{
				failedGuesses++;
			}
		}

		if (pState.getRound() == 0)
		{
			System.err.print("Getting results for species: ");


			int[] os;
			for(int i = 0; i < pSpecies.length; i++)
			{
				birdsListArr[pSpecies[i]].addLast(new HMM());
				os = getBirdObservations(pState.getBird(i));
				birdsListArr[pSpecies[i]].getLast().estimateMatrices(estimations, os);
			}




		} 
		else
		{
			int[] os;
			int random;
			for(int i = 0; i < pSpecies.length; i++)
			{
				if(pSpecies[i] !=  -1 ) //&& pState.getBird(i).isAlive())
				{
					os = getBirdObservations(pState.getBird(i));
					birdsListArr[pSpecies[i]].addLast(new HMM());
					birdsListArr[pSpecies[i]].getLast().estimateMatrices(estimations, os);
				}
			}
		}

		System.err.println("Guess statistics, correct guesses " + correctGuesses + " failed guesses " + failedGuesses + " guess rate " + (new Double(correctGuesses)/(failedGuesses + correctGuesses)));
		System.err.println("Shooting statistics, shots fired: " + shotsFired + " birds killed " + hitBirds + " hit rate " + (new Double(hitBirds))/(shotsFired));

	}
	private void train(HMM[] hmmArr,Bird bird, int birdType)
	{
		int[] curOs;
		curOs = getBirdObservations(bird);
		if (hmmArr[birdType] == null)
		{
			System.err.println("No HMM for bird" + birdType + " creating new one");
			hmmArr[birdType] = new HMM();
		}
		hmmArr[birdType].estimateMatrices(estimations, curOs);


	}

	public static final Action cDontShoot = new Action(-1, -1);
}