#ifndef _CHECKERS_PLAYER_HPP_
#define _CHECKERS_PLAYER_HPP_

#include <vector>
#include "constants.hpp"
#include "deadline.hpp"
#include "gamestate.hpp"
#include "move.hpp"

namespace checkers {
const int DEPTH = 8;
//std::unordered_map<uint_fast64_t, int> redMap, whiteMap; // hashmap for eval of different player
// maybe use negative of red as eval of white
class Player {
   public:
    /// perform a move
    ///\param pState the current state of the board
    ///\param pDue time before which we must have returned
    ///\return the next state the board is in after our move
    GameState play(const GameState &pState, const Deadline &pDue);
};

/*namespace checkers*/  // namespace checkers
}

#endif
