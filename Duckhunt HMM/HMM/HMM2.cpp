#include <iostream>
using namespace std;

struct Out
{
    double **A; // transision matrix    [0~N-1]*[0~N-1]
    double **B; // observation matrix   [0~N-1]*[0~K-1]
    double **pi; // initial state distribution   [0]*[0~N-1]
    double **Obs; // emissions (observation)sequence     [0]*[0~T-1]
    double **theta; // prob of different state(t-1) come to state now (t)    [T-1]*[0~N-1]
    int **phi; // ID of max prob of theta    [T-1]*[0~N-1]
    int *Xstar; // mose possible state sequence
    int N; // number of state
    int K; // number of observation
    int M; // number of rowHeight of pi
    int T; // number of timestep
};

Out Matrix; // Define structure Matrix for input

void read_input()
{

    std::cin>>Matrix.N>>Matrix.N; // transision matrix A
    Matrix.A = new double *[Matrix.N];
    for (int i = 0; i < Matrix.N; ++i)
    {
        Matrix.A[i] = new double[Matrix.N];
        for (int j = 0; j < Matrix.N; ++j)
        {
            std::cin >> Matrix.A[i][j];
        }
    }

    std::cin>>Matrix.N>>Matrix.K; // emission matrix B
    Matrix.B = new double *[Matrix.N];
    for (int i = 0; i < Matrix.N; ++i)
    {
        Matrix.B[i] = new double[Matrix.K];
        for (int j = 0; j < Matrix.K; ++j)
        {
            std::cin >> Matrix.B[i][j];
        }
    }

    std::cin>>Matrix.M>>Matrix.N; // initial state probability distribution pi
    Matrix.pi = new double *[Matrix.M];
    for (int i = 0; i < Matrix.M; ++i)
    {
        Matrix.pi[i] = new double[Matrix.N];
        for (int j = 0; j < Matrix.N; ++j)
        {
            std::cin >> Matrix.pi[i][j];
        }
    }

    std::cin>>Matrix.T; // emissions (observation)sequence
    Matrix.Obs = new double *[1];
    Matrix.Obs[0] = new double[Matrix.T];
    for (int j = 0; j < Matrix.T; ++j)
    {
        std::cin >> Matrix.Obs[0][j];
    }
}

void Cal_theta()
{
    Matrix.theta = new double *[Matrix.T]; // init theta martix
    for (int i = 0; i < Matrix.T; ++i)
    {
        Matrix.theta[i] = new double[Matrix.N];
    }

    Matrix.phi = new int *[Matrix.T]; // init phi martix
    for (int i = 0; i < Matrix.T; ++i)
    {
        Matrix.phi[i] = new int[Matrix.N];
    }

    // first step t=1
    int k = Matrix.Obs[0][0]; // index of observation
    for (int i = 0; i < Matrix.N; ++i)
    {
        Matrix.theta[0][i] = Matrix.B[i][k] * Matrix.pi[0][i];
    }

    // t timestep
    for (int t=1; t < Matrix.T; ++t)
    {
        k = Matrix.Obs[0][t];
        for (int i = 0; i < Matrix.N; i++)
        {
            for (int j = 0; j < Matrix.N; j++)
            {
                double buff = Matrix.A[j][i] * Matrix.theta[t-1][j] * Matrix.B[i][k];
                if (buff > Matrix.theta[t][i])
                {
                    Matrix.theta[t][i] = buff;
                    Matrix.phi[t][i] = j;
                }
            }
        }
    }

}
void printMatrix(double **M, int X, int Y)
{
    for (int i = 0; i < X; i++)
    {
        for(int j = 0; j < Y; j++)
        {
            cout << M[i][j] << " ";
        }
        std::cout << '\n';
    }
}

void Cal_Xstar()
{
    Matrix.Xstar = new int[Matrix.T]; // initial

    // T timestep
    double buff = 0;
    for (int j = 0; j < Matrix.N; j++)
    {

        if (Matrix.theta[Matrix.T - 1][j] > buff)
        {
            buff = Matrix.theta[Matrix.T - 1][j];
            Matrix.Xstar[Matrix.T - 1] = j;
        }
    }


    // t timestep
    for (int t = (Matrix.T-2); t >= 0 ; t--)
    {
        Matrix.Xstar[t] = Matrix.phi[t+1][Matrix.Xstar[t+1]];
    }
}

int main()
{
    read_input(); // read data from infut file
    Cal_theta(); // calculate theta in the last timestep
    //printMatrix(Matrix.theta, Matrix.T, Matrix.N);
    Cal_Xstar();

    for(int t=0;t<Matrix.T;t++){
      std::cout<<Matrix.Xstar[t]<<" ";
    }
    return 0;
}
