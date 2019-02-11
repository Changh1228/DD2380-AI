#include <iostream>
using namespace std;

struct Out
{
    double **A; // transision matrix    [0~N-1]*[0~N-1]
    double **B; // observation matrix   [0~N-1]*[0~K-1]
    double **pi; // initial state distribution   [0]*[0~N-1]
    double **Obs; // emissions (observation)sequence     [0]*[0~T-1]
    double **Alpha; // joint prob of obs sequence(1~t) and stete(t)    [0]*[0~N-1]
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

double **MatrixMult (double **A, double **B, int X, int Y, int Z) // A[X*Y] B[Y*Z]
{
    int i,j,k;
    double **c = new double *[X];
    for (i = 0;i < X; i++)
    {
        c[i] = new double[Z];
        for (j = 0; j < Z; j++)
        {
            c[i][j] = .0;
            for (k = 0; k < Y; k++)
            {
                c[i][j] = c[i][j] + (A[i][k] * B[k][j]);
            }
        }
    }
    return c;
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
void Cal_Alpha()
{
    // first step t=1
    int k = Matrix.Obs[0][0]; // index of observation
    Matrix.Alpha = new double *[1];
    Matrix.Alpha[0] = new double[Matrix.N];
    for (int i = 0; i < Matrix.N; ++i)
    {
        Matrix.Alpha[0][i] = Matrix.B[i][k] * Matrix.pi[0][i];
    }
    //printMatrix(Matrix.Alpha, 1, Matrix.N);

    // t timestep
    for (int t=1; t < Matrix.T; ++t)
    {
        k = Matrix.Obs[0][t]; // index of observation
        Matrix.Alpha = MatrixMult(Matrix.Alpha, Matrix.A, 1, Matrix.N, Matrix.N);
        for (int i = 0; i < Matrix.N; ++i)
        {
            Matrix.Alpha[0][i] = Matrix.B[i][k] * Matrix.Alpha[0][i];
        }
        //printMatrix(Matrix.Alpha, 1, Matrix.N);
    }
}

int main()
{
    double Output = .0;
    read_input(); // read data from infut file
    Cal_Alpha(); // calculate Alpha in the last timestep

    for (int i = 0; i < Matrix.N; ++i)
    {
        Output = Output + Matrix.Alpha[0][i];
    }
    std::cout << Output <<'\n';

    return 0;
}
