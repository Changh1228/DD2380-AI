#include "player.hpp"
#include <climits>
#include <cstdlib>
#include <algorithm>

namespace TICTACTOE
{
uint8_t getTheOtherPlayer(uint8_t player){
    return (player == CELL_X) ? CELL_O : CELL_X;
}
int eval(const GameState &pState, uint8_t player)
{
    //uint8_t player = pState.getNextPlayer();
    uint8_t opplayer = getTheOtherPlayer(player);
    int myMark, opMark;
    int sumMark = 0;

    // Row
    for (size_t i = 0; i < 4; i++) {
        myMark = 0;
        opMark = 0;
        for (size_t j = 0; j < 4; j++) {
            if (pState.at(i,j) == player) {
                myMark++;
            }
            if (pState.at(i,j) == opplayer) {
                opMark--;
            }
        }
        if (myMark != 0 && opMark != 0) {
            continue;
        }
        else{
            sumMark += (myMark + opMark);
        }
    }
    // Column
    for (size_t i = 0; i < 4; i++) {
        myMark = 0;
        opMark = 0;
        for (size_t j = 0; j < 4; j++) {
            if (pState.at(j,i) == player) {
                myMark++;
            }
            if (pState.at(j,i) == opplayer) {
                opMark--;
            }
        }
        if (myMark != 0 && opMark != 0) {
            continue;
        }
        else{
            sumMark += (myMark + opMark);
        }
    }
    // Diagonal1
    myMark = 0;
    opMark = 0;
    for (size_t i = 0; i < 4; i++) {
        if (pState.at(i,3-i) == player) {
            myMark++;
        }
        if (pState.at(i,3-i) == opplayer) {
            opMark--;
        }
    }
    if (myMark != 0 && opMark != 0);
    else{
        sumMark += (myMark + opMark);
    }
    // Diagonal2
    myMark = 0;
    opMark = 0;
    for (size_t i = 0; i < 4; i++) {
        if (pState.at(i,i) == player) {
            myMark++;
        }
        if (pState.at(i,i) == opplayer) {
            opMark--;
        }
    }
    if (myMark != 0 && opMark != 0);
    else{
        sumMark += (myMark + opMark);
    }
    return sumMark;
}

int minmaxAlphaBeta(const GameState &pState,int depth, int alpha, int beta, uint8_t player, uint8_t myStand)
{
    std::vector<GameState> lNextStates;
    pState.findPossibleMoves(lNextStates);
    int v = 0;
    if (depth == 0 || lNextStates.size() == 0)
        v = eval(pState, myStand);
    else{
        if (player == myStand){
        	v = INT_MIN;
        	for (size_t i=0; i<lNextStates.size(); i++){
        		v = std::max(v,minmaxAlphaBeta(lNextStates[i], depth-1, alpha, beta, getTheOtherPlayer(player), myStand));
        		alpha = std::max(alpha,v);
        		if (beta <= alpha)
        			break;
        	}
        }
        else if(player != myStand){
        	v = INT_MAX;
        	for (size_t i=0; i<lNextStates.size(); i++){
        		v = std::min(v,minmaxAlphaBeta(lNextStates[i], depth-1, alpha, beta, getTheOtherPlayer(player), myStand));
        		beta = std::min(beta,v);
        		if (beta <= alpha)
        			break;
        	}
        }
    }

    return v;
}


GameState Player::play(const GameState &pState,const Deadline &pDue)
{
    std::vector<GameState> lNextStates;
    pState.findPossibleMoves(lNextStates);
    //Returns a list of all valid moves for \p pWho

    if (lNextStates.size() == 0)
        return GameState(pState, Move());

    for (size_t i = 0; i < 16; i++) {
        std::cerr << unsigned(lNextStates[0].at(i)) ;
    }
    std::cerr << " " << '\n';
    /*
     * Here you should write your clever algorithms to get the best next move, ie the best
     * next state. This skeleton returns a random move instead.
     */
    uint8_t player = pState.getNextPlayer(); // get current player
    //std::cerr << "player " << unsigned(player) << '\n';

    int depth = 3;
    int alpha = INT_MIN;
    int beta = INT_MAX;
    int bestValue = INT_MIN;
    int beststateID = 20;
    int compareBuff = INT_MIN;


    for (size_t i = 0; i < lNextStates.size(); i++) {
        compareBuff = minmaxAlphaBeta(lNextStates[i], depth, alpha, beta, getTheOtherPlayer(player), player);
        //std::cerr << "Bestv" << compareBuff<< '\n';
        if (compareBuff > bestValue) {
            bestValue = compareBuff;
            beststateID = i;
        }
    }
    std::cerr << "Sum = "<< eval(lNextStates[beststateID], player) << '\n';
    std::cerr << "State" << '\n';

    GameState move = lNextStates[beststateID];

    if (beststateID == 20) {
        move = lNextStates[rand() % lNextStates.size()];
        std::cerr << "random" << '\n';
    }

    return move;
}
/*namespace TICTACTOE*/ }
