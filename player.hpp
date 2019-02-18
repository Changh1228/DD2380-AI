#ifndef _CHECKERS_PLAYER_HPP_
#define _CHECKERS_PLAYER_HPP_

#include <vector>
#include "constants.hpp"
#include "deadline.hpp"
#include "gamestate.hpp"
#include "move.hpp"

namespace checkers {
const int DEPTH = 7;
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
