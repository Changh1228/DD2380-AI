#include "player.hpp"
#include <cstdlib>
#include <climits>
#include <random>
#include <unordered_map>

namespace checkers
{
const int side[14] = {0,1,2,3,11,19,27,31,30,29,28,20,12,4};
const int lsRow[9] = {5,6,7,13,14,15,21,22,23};
const int rsRow[9] = {8,9,10,16,17,18,24,25,26};
std::unordered_map<uint_fast64_t, int> redMap, whiteMap;

struct Zobrist {
    // hash 值： 格子上红or白；格子上红王or白王；next player是谁
    uint_fast64_t whitePiece[32];
    uint_fast64_t redPiece[32];
    uint_fast64_t whiteKing[32];
    uint_fast64_t redKing[32];
    uint_fast64_t nextRed;
    uint_fast64_t nextWhite;
    //uint_fast64_t initState;
} Zobrist;

void Zobrist_init() {
    std::random_device rd;
    std::mt19937_64 mt(rd());

    for (size_t i = 0; i < 32; i++) {
        Zobrist.whitePiece[i] = mt();
    }
    for (size_t i = 0; i < 32; i++) {
        Zobrist.redPiece[i] = mt();
    }
    for (size_t i = 0; i < 32; i++) {
        Zobrist.whiteKing[i] = mt();
    }
    for (size_t i = 0; i < 32; i++) {
        Zobrist.redKing[i] = mt();
    }
    Zobrist.nextRed = mt();
    Zobrist.nextWhite = mt();
    /*Zobrist.initState = Zobrist.nextRed; // first player is red
    for(size_t i = 0; i < 12; i++) { // red piece is in case 0-11
        Zobrist.initState = Zobrist.initState ^ Zobrist.redPiece[i];
    }
    for(size_t i = 20; i < 32; i++) { // white piece is in case 20-31
        Zobrist.initState = Zobrist.initState ^ Zobrist.whitePiece[i];
    }*/
}


uint8_t getOtherPlayer(uint8_t player){
    return (player == CELL_RED) ? CELL_WHITE : CELL_RED;
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
        for(unsigned i=1; i<pMove.length(); ++i) {
            std::cerr << unsigned(pMove[i]) << " ";
        }
        std::cerr << " " << '\n';
    }
    else ; // no jump
    return num;
}

int markEnd(const GameState &pState, uint8_t myPlayer) {
    if (pState.isRedWin()) {
        if (myPlayer == CELL_RED)
            return 2000; // I(red) win
        else
            return -2000; // I(red) lose
    }
    else if (pState.isWhiteWin()) {
        if (myPlayer == CELL_WHITE)
            return 2000; // I(white) win
        else
            return -2000; // I(white) lose
    }
    else if (pState.isDraw()) {
        return -1000;
    }
    else
        return 0;
}

int markSide(const GameState &pState, uint8_t myPlayer) {
    uint8_t opPlayer = getOtherPlayer(myPlayer);
    int sum = 0;
    for (auto i : side) {
        if (pState.at(i) & myPlayer) {
            ++sum;
        }
        else if (pState.at(i) & opPlayer) {
            --sum;
        }
    }
    return sum;
}

int markProtect(const GameState &pState, uint8_t myPlayer) {
    int redUnPro = 0;
    int whiteUnPro = 0;
    for (auto i : lsRow) {
        if (pState.at(i) & CELL_RED) {
            if ((pState.at(i-4) & CELL_RED) || (pState.at(i-5) & CELL_RED)) {
                ++redUnPro;
            }
        }
        else if (pState.at(i) & CELL_WHITE) {
            if ((pState.at(i+4) & CELL_WHITE) || (pState.at(i+3) & CELL_WHITE)) {
                ++whiteUnPro;
            }
        }
    }
    for (auto i :rsRow) {
        if (pState.at(i) & CELL_RED) {
            if ((pState.at(i-4) & CELL_RED) || (pState.at(i-3) & CELL_RED)) {
                ++redUnPro;
            }
        }
        else if (pState.at(i) & CELL_WHITE) {
            if ((pState.at(i+4) & CELL_WHITE) || (pState.at(i+5) & CELL_WHITE)) {
                ++whiteUnPro;
            }
        }
    }
    if (myPlayer == CELL_RED)
        return (whiteUnPro - redUnPro);
    else
        return (redUnPro - whiteUnPro);
}

uint_fast64_t calHashKey(const GameState &pState) {
    // my player is the player we cal eval func for
    uint8_t player = pState.getNextPlayer();
    uint_fast64_t key;
    if (player & CELL_RED)
        key = Zobrist.nextRed;
    else
        key = Zobrist.nextWhite;

    for(size_t i = 0; i < pState.cSquares; i++) {
        if (pState.at(i) & CELL_RED) {
            key ^= Zobrist.redPiece[i];
            if (pState.at(i) & CELL_KING) {
                key ^= Zobrist.redKing[i];
            }
        }
        else if (pState.at(i) & CELL_WHITE) {
            key ^= Zobrist.whitePiece[i];
            if (pState.at(i) & CELL_KING) {
                key ^= Zobrist.whiteKing[i];
            }
        }
    }
    return key;
}

int eval(const GameState &pState, uint8_t myPlayer) {
    uint_fast64_t key = calHashKey(pState); // check repeat state
    if ((myPlayer & CELL_RED) && (redMap.find(key) != redMap.end())) {
        //std::cerr << "cheked repeat red " << redMap[key]<< " size " << redMap.size()<<'\n';
        return redMap[key];
    }
    else if  ((myPlayer & CELL_WHITE) && (whiteMap.find(key) != whiteMap.end())) {
        //std::cerr << "cheked repeat white " << whiteMap[key]<<" size " << whiteMap.size()<< '\n';
        return whiteMap[key];
    }

    uint8_t opPlayer = getOtherPlayer(myPlayer);
    int sumMark = 0;
    int myPiece = 0;
    int opPiece = 0;
    int myKing = 0;
    int opKing = 0;
    //std::cerr << "mark " ;

    for (int i = 0; i < pState.cSquares; ++i) {
		if (pState.at(i) & myPlayer) {
            ++ myPiece;
            if (pState.at(i) & CELL_KING) {
                ++ myKing;
            }
        }
        else if (pState.at(i) & opPlayer) {
            ++ opPiece;
            if (pState.at(i) & CELL_KING) {
                ++ opKing;
            }
        }
	} // cal num of pieces and king
    /*std::cerr << "myPiece "<< myPiece << '\n';
    std::cerr << "opPiece "<< opPiece << '\n';
    std::cerr << "myKing "<< myKing << '\n';
    std::cerr << "opKing "<< opKing << '\n'; //*/

    // mark for dif of piece and king number katti ++
    sumMark = (myPiece - opPiece) + 100 * ( - opKing);

    // penalty if opPlayer get king kattis +++
    //sumMark -= 100 * opKing;

    // mark for jump: no improve in kattis
    //sumMark += markJump(pState, myPlayer);
    /*std::cerr << "mark from jump " << buff << '\n'; //*/

    // mark for end game kattis +++
    sumMark += markEnd(pState, myPlayer);

    // mark for accupy side for defence kattis +
    //sumMark += markSide(pState, myPlayer);

    // mark for unprotected pieces kattis +
    //sumMark += markProtect(pState, myPlayer);
    if (myPlayer & CELL_RED) // add value to the map
        redMap[key] = sumMark;
    else
        whiteMap[key] = sumMark;

    //std::cerr << sumMark << '\n';
    return sumMark;
}

int minmaxAlphaBeta(const GameState &pState,int depth, int alpha, int beta, uint8_t player, uint8_t myStand) {
    std::vector<GameState> lNextStates;
    pState.findPossibleMoves(lNextStates);
    int v = 0;
    if (depth == 0 || lNextStates.size() == 0)
        v = eval(pState, myStand);
    else{
        //std::cerr << "layer "<< DEPTH - depth +2<<" child num "<< lNextStates.size() << '\n';
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
    /*std::cerr << "player " << unsigned(player) << '\n';
    std::cerr << "layer 1 child num "<< lNextStates.size() << '\n'; //*/

    //std::cerr << "depth"  << depth<< '\n';
    int alpha = INT_MIN;
    int beta = INT_MAX;
    int bestValue = INT_MIN;
    int beststateID = 0;
    int compareBuff = INT_MIN;
    Zobrist_init(); // init hash key

    for (size_t i = 0; i < lNextStates.size(); i++) {
    	if(pDue.now() >pDue - 0.1){
            std::cerr << "list length "  << lNextStates.size()<< '\n';
            std::cerr << "break at " << i << '\n'; //*/
        	break;
        }

        compareBuff = minmaxAlphaBeta(lNextStates[i], DEPTH, alpha, beta, getOtherPlayer(player), player);
        if (compareBuff > bestValue) {
            bestValue = compareBuff;
            beststateID = i;
        }
    }
    std::cerr << "bestValue = "<< bestValue << '\n'; //*/

    GameState move = lNextStates[beststateID];
    std::cerr << "Best Id: " << beststateID<< '\n'; //*/

    /*if (beststateID == 100) {
        move = lNextStates[rand() % lNextStates.size()];
        std::cerr << "random" << '\n';
    }*/
    return move;
}

/*namespace checkers*/ }
