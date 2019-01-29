#include <iostream>
using namespace std;

struct Out
{
    double **A;
    double **B;
    double **pi;
    int N; // number of state, observation and rowHeight if pi
    int K;
    int M;
};

Out Matrix;

void read_input()
{

    std::cin>>Matrix.N>>Matrix.N; // transision matrix A
    Matrix.A = new double *[Matrix.N];
    for (int i = 0; i < Matrix.N; i++)
    {
        Matrix.A[i] = new double[Matrix.N];
        for (int j = 0; j < Matrix.N; j++)
        {
            std::cin >> Matrix.A[i][j];
        }
    }

    std::cin>>Matrix.N>>Matrix.K; // emission matrix B
    Matrix.B = new double *[Matrix.N];
    for (int i = 0; i < Matrix.N; i++)
    {
        Matrix.B[i] = new double[Matrix.K];
        for (int j = 0; j < Matrix.K; j++)
        {
            std::cin >> Matrix.B[i][j];
        }
    }

  std::cin>>Matrix.M>>Matrix.N; // initial state probability distribution pi
  Matrix.pi = new double *[Matrix.M];
  for (int i = 0; i < Matrix.M; i++)
  {
      Matrix.pi[i] = new double[Matrix.N];
      for (int j = 0; j < Matrix.N; j++)
      {
          std::cin >> Matrix.pi[i][j];
      }
  }

}

double **MatrixMult (double **A, double **B, int X, int Y, int Z)
{
    int i,j,k;
    double **c = new double *[X];

    for (i = 0;i < X; i++)
    {
        c[i] = new double[Z];
        for (j = 0;j < Z; j++)
        {
            c[i][j] = 0;
            for (k = 0; k < Y; k++)
            {
                c[i][j] = c[i][j] + (A[i][k] * B[k][j]);
            }
        }
    }
    return c;
}

int main()
{
    read_input(); // read data from infut file

    double **statet = MatrixMult(Matrix.pi, Matrix.A, Matrix.M, Matrix.N, Matrix.N); // get new state vector
    double **obst   = MatrixMult(statet, Matrix.B, Matrix.M, Matrix.N, Matrix.K); // get new observation vector

    std::cout << Matrix.M << " " << Matrix.K << " ";
    for (int i = 0; i < Matrix.M; i++)
    {
        for(int j = 0; j < Matrix.K; j++)
        {
            cout << obst[i][j] << " ";
        }
    }
    std::cout << '\n';
    return 0;
}
