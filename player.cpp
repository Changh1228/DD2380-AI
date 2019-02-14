#include "player.hpp"
#include <cstdlib>
#include <climits>
#include <string>
#include <map>


namespace TICTACTOE3D
{
const int marks[5][5] = {
      {0,     -10, -100,  -1000, -10000},
      {10,     0,    0,    0,    0},
      {100,    0,    0,    0,    0},
      {1000,   0,    0,    0,    0},
      {10000,  0,    0,    0,    0}
};

uint8_t getTheOtherPlayer(uint8_t player){
    return (player == CELL_X) ? CELL_O : CELL_X;
}
/*std::string Converter (const GameState &pState) {
    std::string s;
    s.reserve(70);
    for (size_t i = 0; i < 64; i++) {
        s += std::to_string(pState.at(i));
    }
    return s;
}*/

int eval(const GameState &pState, uint8_t player)
{
    //uint8_t player = pState.getNextPlayer();
    uint8_t opplayer = getTheOtherPlayer(player);
    int myMark, opMark;
    int sumMark = 0;

    // Row from k perspective
    for (size_t i = 0; i < 4; i++) {
        for (size_t j = 0; j < 4; j++) {
        	myMark = 0;
        	opMark = 0;
        	for (size_t k = 0; k < 4; k++) {
	            if (pState.at(i,j,k) == player) {
	                myMark++;
	            }
	            if (pState.at(i,j,k) == opplayer) {
	                opMark++;
	            }
	        }
	        sumMark += marks[myMark][opMark];
        }

    }
    // Row from j perspective
    for (size_t i = 0; i < 4; i++) {
        for (size_t k = 0; k < 4; k++) {
        	myMark = 0;
        	opMark = 0;
        	for (size_t j = 0; j < 4; j++) {
	            if (pState.at(i,j,k) == player) {
	                myMark++;
	            }
	            if (pState.at(i,j,k) == opplayer) {
	                opMark++;
	            }
	        }
	        sumMark += marks[myMark][opMark];
        }

    }
    // Row from i perspective
    for (size_t k = 0; k < 4; k++) {
        for (size_t j = 0; j < 4; j++) {
        	myMark = 0;
        	opMark = 0;
        	for (size_t i = 0; i < 4; i++) {
	            if (pState.at(i,j,k) == player) {
	                myMark++;
	            }
	            if (pState.at(i,j,k) == opplayer) {
	                opMark++;
	            }
	        }
	        sumMark += marks[myMark][opMark];
        }

    }

    // Diagonals from i perspective
    for(size_t i = 0; i < 4; i++){
	    myMark = 0;
	    opMark = 0;
	    for (size_t m = 0; m < 4; m++) {
	        if (pState.at(i, m, 3-m) == player) {
	            myMark++;
	        }
	        if (pState.at(i, m, 3-m) == opplayer) {
	            opMark++;
	        }
	    }
	    sumMark += marks[myMark][opMark];

	    myMark = 0;
	    opMark = 0;
	    for (size_t m = 0; m < 4; m++) {
	        if (pState.at(i, m, m) == player) {
	            myMark++;
	        }
	        if (pState.at(i, m, m) == opplayer) {
	            opMark++;
	        }
	    }
	    sumMark += marks[myMark][opMark];
	}

	// Diagonals from j perspective
    for(size_t i = 0; i < 4; i++){
	    myMark = 0;
	    opMark = 0;
	    for (size_t m = 0; m < 4; m++) {
	        if (pState.at(m, i, 3-m) == player) {
	            myMark++;
	        }
	        if (pState.at(m, i, 3-m) == opplayer) {
	            opMark++;
	        }
	    }
	    sumMark += marks[myMark][opMark];

	    myMark = 0;
	    opMark = 0;
	    for (size_t m = 0; m < 4; m++) {
	        if (pState.at(m, i, m) == player) {
	            myMark++;
	        }
	        if (pState.at(m, i, m) == opplayer) {
	            opMark++;
	        }
	    }
	    sumMark += marks[myMark][opMark];
	}
	// Diagonals from k perspective
    for(size_t i = 0; i < 4; i++){
	    myMark = 0;
	    opMark = 0;
	    for (size_t m = 0; m < 4; m++) {
	        if (pState.at(m, 3-m, i) == player) {
	            myMark++;
	        }
	        if (pState.at(m, 3-m, i) == opplayer) {
	            opMark++;
	        }
	    }
	    sumMark += marks[myMark][opMark];

	    myMark = 0;
	    opMark = 0;
	    for (size_t m = 0; m < 4; m++) {
	        if (pState.at(m, m, i) == player) {
	            myMark++;
	        }
	        if (pState.at(m, m, i) == opplayer) {
	            opMark++;
	        }
	    }
	    sumMark += marks[myMark][opMark];
	}

    // The last 4 main diagonals
    myMark = 0;
    opMark = 0;
    for (size_t i = 0; i < 4; i++) {
        if (pState.at(i,i,i) == player) {
            myMark++;
        }
        if (pState.at(i,i,i) == opplayer) {
            opMark++;
        }
    }
    sumMark += marks[myMark][opMark];

    myMark = 0;
    opMark = 0;
    for (size_t i = 0; i < 4; i++) {
        if (pState.at(3-i,i,i) == player) {
            myMark++;
        }
        if (pState.at(3-i,i,i) == opplayer) {
            opMark++;
        }
    }
    sumMark += marks[myMark][opMark];

    myMark = 0;
    opMark = 0;
    for (size_t i = 0; i < 4; i++) {
        if (pState.at(i,3-i,i) == player) {
            myMark++;
        }
        if (pState.at(i,3-i,i) == opplayer) {
            opMark++;
        }
    }
    sumMark += marks[myMark][opMark];

    myMark = 0;
    opMark = 0;
    for (size_t i = 0; i < 4; i++) {
        if (pState.at(i,i,3-i) == player) {
            myMark++;
        }
        if (pState.at(i,i,3-i) == opplayer) {
            opMark++;
        }
    }
    sumMark += marks[myMark][opMark];

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

    /*
     * Here you should write your clever algorithms to get the best next move, ie the best
     * next state. This skeleton returns a random move instead.
     */
    uint8_t player = pState.getNextPlayer(); // get current player
    std::cerr << "player " << unsigned(player) << '\n';

    int depth =0;
    /*if (lNextStates.size() > 50) {
        depth = 0;
    }
    if (lNextStates.size() > 40 && lNextStates.size() <= 50) {
        depth = 1;
    }
    if (lNextStates.size() > 30 && lNextStates.size() <= 40) {
        depth = 1;
    }
    if (lNextStates.size() > 0 && lNextStates.size() <= 30) {
        depth = 1;
    }*/

    std::cerr << "depth"  << depth<< '\n';
    int alpha = INT_MIN;
    int beta = INT_MAX;
    int bestValue = INT_MIN;
    int beststateID = 0;
    int compareBuff = INT_MIN;


    for (size_t i = 0; i < lNextStates.size(); i++) {
    	if(pDue.now() >pDue - 0.1){
            std::cerr << "list length "  << lNextStates.size()<< '\n';
            std::cerr << "break at " << i << '\n';
        	break;
        }


        compareBuff = minmaxAlphaBeta(lNextStates[i], depth, alpha, beta, getTheOtherPlayer(player), player);
        if (compareBuff > bestValue) {
            bestValue = compareBuff;
            beststateID = i;
        }
    }
    std::cerr << "Sum = "<< eval(lNextStates[beststateID], player) << '\n';

    GameState move = lNextStates[beststateID];
    std::cerr << "Best Id: " << beststateID<< '\n';

    /*if (beststateID == 100) {
        move = lNextStates[rand() % lNextStates.size()];
        std::cerr << "random" << '\n';
    }*/

    return move;
}

/*namespace TICTACTOE3D*/ }
