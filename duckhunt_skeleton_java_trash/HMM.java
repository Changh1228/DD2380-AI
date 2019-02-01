import java.util.ArrayList;



public class HMM {
	public double[][] pxMatrix, aMatrix, bMatrix;
	private double[] c;
	private final double UNDERFLOW_PROTECTION = 0.00000000000000000000000000001;
	public HMM (double[][] pxMatrix, double[][] aMatrix, double[][] bMatrix)
	{
		this.pxMatrix = pxMatrix;
		this.aMatrix = aMatrix;
		this.bMatrix = bMatrix;
	}

	public HMM(HMM h)
	{
		int N = 6;
		int T = Constants.COUNT_MOVE;
		pxMatrix = new double[1][N];
		aMatrix = new double[N][N];
		bMatrix = new double[N][T];

		System.arraycopy(h.pxMatrix[0], 0, pxMatrix[0], 0, h.pxMatrix[0].length);

		for(int i= 0; i < aMatrix.length; i++)
			System.arraycopy(h.aMatrix[i], 0, aMatrix[i], 0, aMatrix[0].length);

		for(int i = 0; i < bMatrix.length; i++)
			System.arraycopy(h.bMatrix[i], 0, bMatrix[i], 0, bMatrix[0].length);
	}
	public HMM()
	{
		int N = 6;
		int T = Constants.COUNT_MOVE;
		pxMatrix = new double[1][N];
		aMatrix = new double[N][N];
		bMatrix = new double[N][T];

		for(int i = 0; i < N; i++){
			pxMatrix[0][i] = (1.00/pxMatrix[0].length) + Math.random() * (0.50/pxMatrix[0].length);
		}


		for(int i = 0; i < N;i++){
			for(int j = 0; j < N; j++){
				if (i == j)
					aMatrix[i][j] = (1.00/aMatrix[0].length) + Math.random() * (0.50/aMatrix[0].length);
				else
					aMatrix[i][j] = (0.50/aMatrix[0].length) + Math.random() * (0.50/aMatrix[0].length);
			}
		}



		for(int i = 0; i < N;i++){
			for(int j = 0; j < bMatrix[0].length; j++){
				bMatrix[i][j] = (1.00/bMatrix[0].length) + Math.random() * (0.50/bMatrix[0].length);
			}
		}


	}

	public static void printMatrix(int[][] matrix)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < matrix.length; i++)
		{
			for(int j = 0; j < matrix[0].length; j++)
			{
				sb.append(matrix[i][j] + " ");
			}
			sb.append("\n");
		}
		System.err.println(matrix.length+ " " +matrix[0].length + "\n" + sb.toString());
	}
	public static void printMatrix(double[][] matrix)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < matrix.length; i++)
		{
			for(int j = 0; j < matrix[0].length; j++)
			{
				sb.append(matrix[i][j] + " ");
			}
			sb.append("\n");
		}
		System.err.println(matrix.length+ " " +matrix[0].length + "\n" + sb.toString());
	}

	public static void printForKattis(double[][] matrix)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < matrix.length; i++)
		{
			for(int j = 0; j < matrix[0].length; j++)
			{
				sb.append(matrix[i][j] + " ");
			}
		}
		System.out.println(matrix.length+ " " +matrix[0].length + " " + sb.toString());
	}




	public int[] viterbi(int[] o)
	{
		final int numStates = pxMatrix[0].length;
		final int numObservations = o.length;
		double[][] viterbi = new double[numStates][numObservations];
		int[][] backtrack = new int[numStates][numObservations];
		for(int i = 0; i < numStates; i++)
		{	
			viterbi[i][0] = pxMatrix[0][i] * bMatrix[i][o[0]];
		}

		double maxProb, tmp;
		for(int t = 1; t < numObservations; t++)
		{
			for(int s = 0; s < numStates; s++)
			{
				maxProb = 0;
				for(int i = 0; i < numStates; i++)
				{
					tmp = viterbi[i][t-1] * aMatrix[i][s] * bMatrix[s][o[t]];
					if (tmp >= maxProb)
					{
						maxProb = tmp;
						viterbi[s][t] = tmp;
						backtrack[s][t] = i;
					}
				}
			}
		}

		double maxValue = 0;
		int maxRow = 0;
		for(int i = 0 ; i < numStates; i++)
		{
			if (viterbi[i][numObservations - 1] > maxValue)
			{
				maxValue =viterbi[i][numObservations - 1];
				maxRow = i;
			}
		}
		int[] answer = new int[numObservations];
		int row = maxRow;
		for(int i = numObservations - 1; i >= 0 ; i--)
		{
			answer[i] = row;
			row = backtrack[row][i];
		}
		return answer;
	}
	public double[] totalNextEmissionProbability(int[] o)
	{

		double[][] alpha = forward(o);

		int T = o.length;
		double sumAlpha = 0;
		for(int i = 0 ; i < alpha.length; i++)
		{
			sumAlpha += alpha[i][T-1];
		}
		double[] pred = new double[bMatrix[0].length];
		double tmp;
		for(int i = 0; i < aMatrix.length; i++)
		{
			tmp = 0.0;
			for(int j = 0; j < aMatrix.length; j++)
			{
				tmp += aMatrix[j][i]*alpha[j][T - 1];
			}
			for(int obs = 0; obs < bMatrix[0].length; obs++)
			{
				for(int s = 0; s < aMatrix.length; s++)
				{
					pred[obs] = (bMatrix[s][obs] * tmp)/sumAlpha;
				}
			}

		}
		return pred;
	}
	public double sequenceProbability(int[] o )
	{




		int numStates = pxMatrix[0].length;
		int numObservations = bMatrix[0].length;
		double[][] emissionMatrix = new double[numStates][o.length];
		// initiate
		for(int i = 0; i < numStates; i++)
		{
			emissionMatrix[i][0] = pxMatrix[0][i] * bMatrix[i][o[0]];
		}


		for(int emission = 0; emission <= o.length - 2; emission++)
		{
			for(int toState = 0; toState < numStates; toState++)
			{
				emissionMatrix[toState][emission + 1] = 0.0;
				for(int fromState = 0; fromState < numStates; fromState++)
				{
					//System.err.println("From State " + fromState + " to State " + toState + " "+emissionMatrix[fromState][emission - 1] +" * " + aMatrix[fromState][toState] +" * " + bMatrix[toState][emissionSequence[emission]] + " = " + emissionMatrix[fromState][emission - 1] * aMatrix[fromState][toState] * bMatrix[toState][emissionSequence[emission]]);
					emissionMatrix[toState][emission + 1] += emissionMatrix[fromState][emission] * aMatrix[fromState][toState] * bMatrix[toState][o[emission + 1]]; 
				}

			}

		}

		double probability = 0;
		for(int i = 0; i < emissionMatrix.length; i++)
		{
			probability += emissionMatrix[i][emissionMatrix[0].length -1];
		}

		//System.err.println("Total probability= " + probability);
		return probability;

	}
	public void estimateMatrices(int iterations, int[] o)
	{
		int i = 0;
		double convergence = 0.0000001; 
		double logProb = Integer.MAX_VALUE;
		double avg,relErr = Integer.MAX_VALUE, oldLogProb = -Integer.MAX_VALUE;
		while(i < iterations && relErr > 0.00001){
			logProb = estimateMatrices(o);
			// Calculate relative error
			avg = (Math.abs(oldLogProb) + Math.abs(logProb))/2;
			relErr = Math.abs(logProb - oldLogProb)/avg;
			i++;
		}
	}
	private double mult(double f1, double f2)
	{
		double ret = f1 * f2;
		if (Double.isNaN(ret))
			return Double.MIN_VALUE;
		else return ret;
	}
	private double div(double numerator, double denominator)
	{
		double ret;
		ret=  (numerator/denominator);
		if (Double.isNaN(ret))
			return Double.MIN_VALUE;
		else return ret;
	}
	private double estimateMatrices(int[] o)
	{
		final int N = pxMatrix[0].length;
		final int T = o.length;
		double[][] alpha = forward(o);
		double[][] beta = backward(o);
		double[][] gamma = new double[N][T];
		double[][][] digamma = new double[N][N][T];
		double denom;
		for(int t = 0; t < T - 1; t++)
		{
			denom = 0;
			for (int i = 0 ; i < N; i++)
			{
				for(int j = 0; j < N; j++)
					denom += alpha[i][t] * aMatrix[i][j]*bMatrix[j][o[t+1]]*beta[j][t+1] + UNDERFLOW_PROTECTION;

			}
			for(int i = 0 ; i < N; i++)
			{
				gamma[i][t] = 0;
				for (int j = 0; j < N; j++)
				{
					digamma[i][j][t] = ((alpha[i][t] * aMatrix[i][j] * bMatrix[j][o[t+1]] * beta[j][t+1]) / denom) + UNDERFLOW_PROTECTION;
					gamma[i][t] += digamma[i][j][t]; 
				}
			}
		}
		for(int i = 0; i < N; i++)
			pxMatrix[0][i] = gamma[i][0];

		double numer;
		for(int i = 0; i < N; i++)
		{
			for(int j = 0; j < N; j++)
			{
				numer = 0;
				denom = 0;
				for (int t = 0; t < T - 1; t++)
				{
					numer +=  digamma[i][j][t];
					denom += gamma[i][t];
				}
				aMatrix[i][j] = (numer/denom) + UNDERFLOW_PROTECTION;

			}
		}

		for(int i = 0 ; i < N ; i++)
		{
			for(int j = 0; j < bMatrix[0].length; j++)
			{
				numer = 0;
				denom = 0;
				for(int t = 0; t < T-1; t++)
				{
					if (o[t] == j)
						numer += gamma[i][t];
					denom += gamma[i][t];
				}
				bMatrix[i][j] = (numer/denom) + UNDERFLOW_PROTECTION;
			}
		}

		double logProb = 0;
		for (int i = 0; i < T; i++)
			logProb = logProb + Math.log10(c[i]);
		logProb = - logProb;
		return logProb;

	}
	private double[][] forward(int[] o)
	{
		final int N = pxMatrix[0].length;
		final int T = o.length;
		double[][] alpha = new double[N][T];
		c = new double[o.length];
		c[0] = 0;
		//Initial values
		for(int i = 0 ; i < N; i++)
		{
			alpha[i][0] = pxMatrix[0][i] * bMatrix[i][o[0]] + UNDERFLOW_PROTECTION;
			c[0]  += alpha[i][0];
		}
		//Scale initial values
		c[0] = 1/c[0];
		for(int i = 0 ; i < N; i++)
			alpha[i][0] *= c[0];

		//Compute alpha
		for (int t = 1; t < T; t++)
		{
			c[t] = 0;
			for(int i = 0; i < N; i++)
			{
				alpha[i][t] = 0;
				for(int j= 0; j < N; j++)
					alpha[i][t] += alpha[j][t-1] * aMatrix[j][i];

				alpha[i][t] *= bMatrix[i][o[t]] + UNDERFLOW_PROTECTION;
				c[t] += alpha[i][t];
			}
			c[t] = 1/c[t];
			for(int i = 0; i < N; i++)
				alpha[i][t] *= c[t]; 
		}
		return alpha;
	}

	private double[][] backward(int[] o)
	{
		final int N = pxMatrix[0].length;
		final int T = o.length;
		double[][] beta = new double[N][T];

		for(int i = 0; i < N; i++)
			beta[i][T - 1] = c[T-1];

		for(int t = T - 2; t >= 0; t--)
		{
			for(int i = 0; i < N; i++)
			{
				beta[i][t] = 0;
				for(int j = 0; j < N; j++)
					beta[i][t] += aMatrix[i][j] * bMatrix[j][o[t+1]] * beta[j][t+1] + UNDERFLOW_PROTECTION;
				beta[i][t] *= c[t];
			}
		}


		return beta;
	}


	public double[] nextEmissionDistribution()
	{
		double[] emissionProb = new double[bMatrix[0].length];

		for(int px = 0 ; px < pxMatrix[0].length; px++)
		{
			for(int aCol = 0; aCol < aMatrix[0].length; aCol++ )
			{
				for(int bCol = 0; bCol < bMatrix[0].length; bCol++)
					emissionProb[bCol] +=bMatrix[aCol][bCol] * aMatrix[px][aCol] * pxMatrix[0][px];

			}

		}


		return emissionProb;
	}

}