#include <iostream>
#include <cmath>
#include <limits.h>
using namespace std;

struct Out
{
    double **A; // [0~N-1]*[0~N-1] transision matrix
    double **B; // [0~N-1]*[0~K-1] observation matrix
    double **pi; // [0]*[0~N-1] initial state distribution
    double **Obs; // [0]*[0~T-1] emissions (observation)sequence
    /* input matrix ^ */

    double **Alpha; // [0~T-1]*[0~N-1] prob of state(t-1) come to state(t)
    double **Beta; // [0~T-1]*[0~N-1] prob of furture observation  [0~T-1]*[0~N-1]
    double **c; // [0]*[0~T-1] scale factor
    double ***diGamma; // [0~T-1][0~N-1][0~N-1] prob of going form i(t) to j(t+1)
    double **Gamma; // [0~T-1][0~N-1]   prob f state(t)=i
    /* temporary matrix ^ */

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

void open_temporaty_matrix()
{
    Matrix.Alpha = new double *[Matrix.T]; // init Alpha martix
    for (int t = 0; t < Matrix.T; ++t)
    {
        Matrix.Alpha[t] = new double[Matrix.N];
    }

    Matrix.Beta = new double *[Matrix.T]; // init Beta martix
    for (int t = 0; t < Matrix.T; ++t)
    {
        Matrix.Beta[t] = new double[Matrix.N];
    }

    Matrix.c = new double *[1];
    Matrix.c[0] = new double[Matrix.T];

    Matrix.diGamma = new double **[Matrix.T];
    for (int t = 0; t < Matrix.T; ++t)
    {
        Matrix.diGamma[t] = new double *[Matrix.N];
        for (int i = 0;i < Matrix.T; ++i)
        {
            Matrix.diGamma[t][i] = new double [Matrix.N];
        }
    }

    Matrix.Gamma = new double *[Matrix.T]; // init Beta martix
    for (int t = 0; t < Matrix.T; ++t)
    {
        Matrix.Gamma[t] = new double[Matrix.N];
    }

}

void Delete_input_Matrix()
{
    for (int i = 0; i < Matrix.N; ++i)
    {
        delete[] Matrix.A[i];
    }
    delete[] Matrix.A;

    for (int i = 0; i < Matrix.N; ++i)
    {
        delete[] Matrix.B[i];
    }
    delete[] Matrix.B;

    for (int i = 0; i < 1; ++i)
    {
        delete[] Matrix.pi[i];
    }
    delete[] Matrix.pi;

    for (int i = 0; i < 1; ++i)
    {
        delete[] Matrix.Obs[i];
    }
    delete[] Matrix.Obs;
}

void Delete_temporary_Matrix()
{
    for (int t = 0; t < Matrix.T; ++t)
    {
        delete[] Matrix.Alpha[t];
    }
    delete[] Matrix.Alpha;

    for (int t = 0; t < Matrix.T; ++t)
    {
        delete[] Matrix.Beta[t];
    }
    delete[] Matrix.Beta;

    delete[] Matrix.c[0];
    delete[] Matrix.c;

    /*for (int t = 0; t < Matrix.T; ++t)
    {
        for (int i = 0; i < Matrix.N; ++i)
        {
            delete[] Matrix.diGamma[t][i];
        }
        delete[] Matrix.diGamma[t];
    }
    delete[] Matrix.diGamma;*/
    // don't know how to delete this 3D Matrix

    for (int t = 0; t < Matrix.T; ++t)
    {
        delete[] Matrix.Gamma[t];
    }
    delete[] Matrix.Gamma;
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
    std::cout << '\n';
}
void Alpha_pass()
{
    // first step t=1
    int k = Matrix.Obs[0][0]; // index of observation
    Matrix.c[0][0] = 0;
    for (int i = 0; i < Matrix.N; ++i)
    {
        Matrix.Alpha[0][i] = Matrix.B[i][k] * Matrix.pi[0][i];
        Matrix.c[0][0] += Matrix.Alpha[0][i];
    }

    Matrix.c[0][0] = 1 / Matrix.c[0][0];
    for(int i = 0 ; i < Matrix.N; i++) // scale alpha 0
    {
        Matrix.Alpha[0][i] *= Matrix.c[0][0];
    }

    // t timestep
    for (int t=1; t < Matrix.T; ++t)
    {
        Matrix.c[0][t] = 0;
        k = Matrix.Obs[0][t]; // index of observation
        for (int i = 0; i < Matrix.N; i++)
        {
            Matrix.Alpha[t][i] = 0;
            for(int j = 0; j < Matrix.N ; j++)
            {
                Matrix.Alpha[t][i] += (Matrix.Alpha[t-1][j] * Matrix.A[j][i] * Matrix.B[i][k]);
            }
            Matrix.c[0][t] += Matrix.Alpha[t][i];
        }
        Matrix.c[0][t] = 1 / Matrix.c[0][t];
        for(int i = 0 ; i < Matrix.N; i++)
        {
            Matrix.Alpha[t][i] *= Matrix.c[0][t];
        }
    }
}

void Beta_pass()
{
    for(int i=0;i<Matrix.N;i++) // T timestep
    // let beta = 1, scale c[T-1]
    {
        Matrix.Beta[Matrix.T-1][i] = 1 / Matrix.c[0][Matrix.T-1];
    }

    for (int t = Matrix.T-2; t >=0; t--) // t timestep
    {
        int k = Matrix.Obs[0][t+1]; // index of observation(t+1)
        for (int i = 0; i < Matrix.N; i++)
        {
            Matrix.Beta[t][i] = 0;
            for (int j = 0; j < Matrix.N; j++)
            {
                Matrix.Beta[t][i] += Matrix.A[i][j] * Matrix.B[j][k] * Matrix.Beta[t+1][j];
            }
            Matrix.Beta[t][i] *= Matrix.c[0][t]; // scale beta(t) with same factor as alpha(t)
        }
    }
}

void Cal_Gamma()
{
    for (int t = 0; t < Matrix.T-1; t++) {
        double denom = 0;
        int k = Matrix.Obs[0][t+1];
        for (int i = 0; i < Matrix.N; i++)
        {
            for (int j = 0; j < Matrix.N; j++)
            {
                denom += Matrix.Alpha[t][i] * Matrix.A[i][j] * Matrix.B[j][k] * Matrix.Beta[t+1][j];
            }
        }
        for (int i = 0; i < Matrix.N; i++)
        {
            Matrix.Gamma[t][i] = 0;
            for (int j = 0; j < Matrix.N; j++)
            {
                Matrix.diGamma[t][i][j] = (Matrix.Alpha[t][i] * Matrix.A[i][j] * Matrix.B[j][k] * Matrix.Beta[t+1][j]) / denom;
                Matrix.Gamma[t][i] += Matrix.diGamma[t][i][j];
            }
        }
    }

    // special case for gamma T-1
    double denom = 0;
    for (int i = 0; i < Matrix.N; i++)
    {
        denom += Matrix.Alpha[Matrix.T-1][i];
    }
    for (int i = 0; i < Matrix.N; i++)
    {
        Matrix.Gamma[Matrix.T-1][i] = Matrix.Alpha[Matrix.T-1][i] / denom;
    }
}

void Cal_para()
{
    // estimate pi
    for (int i = 0; i < Matrix.N; i++)
    {
        Matrix.pi[0][i] = Matrix.Gamma[0][i];
    }

    // estimate A
    for (int i = 0; i <Matrix.N; i++)
    {
        for (int j = 0; j < Matrix.N; j++)
        {
            double number = 0;
            double denom = 0;
            for (int t = 0; t < Matrix.T-1; t++)
            {
                number += Matrix.diGamma[t][i][j];
                denom += Matrix.Gamma[t][i];
            }
            Matrix.A[i][j] = number / denom;
        }
    }

    //estimate B
    for (int i = 0; i < Matrix.N; i++)
    {
        for (int j = 0; j < Matrix.K; j++)
        {
            double number = 0;
            double denom = 0;
            for (int t = 0; t < Matrix.T-1; t++)
            {
                if (Matrix.Obs[0][t] == j)
                {
                    number += Matrix.Gamma[t][i];
                }
                denom += Matrix.Gamma[t][i];
            }
            Matrix.B[i][j] = number/denom;
        }
    }
}
int main()
{
    double oldlogProb = INT_MIN; //minus inf
    double logProb = INT_MIN + 1;
    read_input(); // read data from infut file
    open_temporaty_matrix();
    for (int it = 0; it < 99999 && logProb > oldlogProb; it++)
    {
        Alpha_pass();
        Beta_pass();
        Cal_Gamma();
        Cal_para();

        oldlogProb = logProb;
        // cal logProb
        logProb = 0;
        for (int i = 0; i < Matrix.T; i++)
        {
            logProb += log(Matrix.c[0][i]);
        }
        logProb = -logProb;
    }

    std::cout << Matrix.N << " " << Matrix.N << " "; // print A
    for (int i = 0; i < Matrix.N; i++)
    {
        for (int j = 0; j < Matrix.N; j++)
        {
            std::cout << Matrix.A[i][j] << " ";
        }
    }
    std::cout << '\n';

    std::cout << Matrix.N << " " << Matrix.K << " "; // print B
    for (int i = 0; i < Matrix.N; i++)
    {
        for (int j = 0; j < Matrix.K; j++)
        {
            std::cout << Matrix.B[i][j] << " ";
        }
    }
    std::cout << '\n' ;

    Delete_input_Matrix();
    Delete_temporary_Matrix();

    return 0;
}
