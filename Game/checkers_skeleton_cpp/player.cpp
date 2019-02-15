#include "player.hpp"
#include <cstdlib>
#include <climits>

namespace checkers
{

uint8_t getOtherPlayer(uint8_t player){
    return (player == CELL_RED) ? CELL_WHITE : CELL_RED;
}

int markPiece(const GameState &pState, uint8_t myPlayer) {
    uint8_t opPlayer = getOtherPlayer(myPlayer);
    int num = 0;
    for (int i = 0; i < pState.cSquares; ++i) {
		if (pState.at(i) & myPlayer)
			++num;
        if (pState.at(i) & opPlayer)
    		--num;
	}
    return num;
}

int markKing(const GameState &pState, uint8_t myPlayer) {
    uint8_t opPlayer = getOtherPlayer(myPlayer);
    int num = 0;
    for (int i = 0; i < pState.cSquares; ++i) {
        if (pState.at(i) == (myPlayer|CELL_KING))
            ++num;
        if (pState.at(i) == (opPlayer|CELL_KING))
            --num;
    }
    return num;
}

int markJump(const GameState &pState, uint8_t myPlayer) {
    Move pMove;
    int num = 0;
    pMove = pState.getMove();
    if (pMove.isJump()) {
        std::cerr << "jump num " << pMove.length()-1<< '\n';
        // pmove [pos before jump, pos after jump, pos after double jump]
        // after double jump, the blank of first jump pos is empty
        if (pState.at(pMove[pMove.length()-1]) & myPlayer) {
            num += pMove.length()-1; // player make a jump
            std::cerr << "I jump " ;
        }
        else{
            num -= pMove.length()-1; // player make a jump
            std::cerr << "op jump ";
        }
        for(unsigned i=1;i<pMove.length();++i) {
            std::cerr << unsigned(pMove[i]) << " ";
        }
        std::cerr << " " << '\n';
    }
    else ; // no jump
    return num;
}

int eval(const GameState &pState, uint8_t myPlayer) {
    //uint8_t opPlayer = getOtherPlayer(myPlayer);
    int sumMark = 0;
    //int myMark, opMark;
    std::cerr << "possible move" << '\n';

    // cal mark for king
    sumMark += markKing(pState, myPlayer);
    std::cerr << "mark from king "<< sumMark << '\n';

    // cal mark for dif of piece number
    int buff = markPiece(pState, myPlayer);
    sumMark += buff;
    std::cerr << "mark from piece " << buff << '\n';

    // cal mark for jump
    buff = markJump(pState, myPlayer);
    sumMark += buff;
    std::cerr << "mark from jump " << buff << '\n';
    std::cerr << " " << '\n';

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
        		v = std::max(v,minmaxAlphaBeta(lNextStates[i], depth-1, alpha, beta, getOtherPlayer(player), myStand));
        		alpha = std::max(alpha,v);
        		if (beta <= alpha)
        			break;
        	}
        }
        else if(player != myStand){
        	v = INT_MAX;
        	for (size_t i=0; i<lNextStates.size(); i++){
        		v = std::min(v,minmaxAlphaBeta(lNextStates[i], depth-1, alpha, beta, getOtherPlayer(player), myStand));
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
    //std::cerr << "Processing " << pState.toMessage() << std::endl;

    std::vector<GameState> lNextStates;
    pState.findPossibleMoves(lNextStates);

    if (lNextStates.size() == 0) return GameState(pState, Move());

    uint8_t player = pState.getNextPlayer(); // get current player
    std::cerr << "player " << unsigned(player) << '\n';
    std::cerr << "child num "<< lNextStates.size() << '\n';

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

    //std::cerr << "depth"  << depth<< '\n';
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

        compareBuff = minmaxAlphaBeta(lNextStates[i], DEPTH, alpha, beta, getOtherPlayer(player), player);
        if (compareBuff > bestValue) {
            bestValue = compareBuff;
            beststateID = i;
        }
    }
    std::cerr << "bestValue = "<< bestValue << '\n';

    GameState move = lNextStates[beststateID];
    std::cerr << "Best Id: " << beststateID<< '\n';

    /*if (beststateID == 100) {
        move = lNextStates[rand() % lNextStates.size()];
        std::cerr << "random" << '\n';
    }*/
    return move;
}

/*namespace checkers*/ }
