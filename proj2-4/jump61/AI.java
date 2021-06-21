package jump61;

import java.util.ArrayList;

/** An automated Player.
 *  @author Thomas Wu
 */
class AI extends Player {

    /** Time allotted to all but final search d (milliseconds). */
    private static final long TIME_LIMIT = 15000;

    /** Number of calls to minmax between checks of elapsed time. */
    private static final long TIME_CHECK_INTERVAL = 10000;

    /** Number of milliseconds in one second. */
    private static final double MILLIS = 1000.0;

    /** A new player of GAME initially playing COLOR that chooses
     *  moves automatically.
     */
    public AI(Game game, Side color) {
        super(game, color);
    }

    @Override
    void makeMove() {
        Game gme = getGame();
        MutableBoard board = new MutableBoard(getBoard());

        ArrayList<Integer> moves = new ArrayList<Integer>();
        Side whoMoves = getSide();
        for (int i = 0; i < board.size() * board.size(); i++) {
            if (board.isLegal(whoMoves, i)) {
                moves.add(i);
            }
        }
        int move;
        move = minmax(whoMoves, board, 3, Integer.MIN_VALUE, moves);
        System.out.println("move is " + move);
        gme.makeMove(move);
        gme.message("%s moves %d %d.%n", whoMoves.toCapitalizedString(),
                board.row(move), board.col(move));
    }


    /** Return the minimum of CUTOFF and the minmax value of board B
      *  (which must be mutable) for player P to a search depth of D
      *  (where D == 0 denotes statically evaluating just the next move).
      *  If MOVES is not null and CUTOFF is not exceeded, set MOVES to
      *  a list of all highest-scoring moves for P; clear it if
      *  non-null and CUTOFF is exceeded. the contents of B are
      *  invariant over this call. */
    private int minmax(Side p, Board b, int d, int cutoff,
                       ArrayList<Integer> moves) {

        int bestSoFar = cutoff;
        int indexOfBest = 0;
        ArrayList<Integer> validMoves = new ArrayList<Integer>();

        if (d == 0) {
            for (int i = 0; i < moves.size(); i++) {
                if (moves.get(i) != null) {
                    b.addSpot(p, moves.get(i));
                    int evalValue = staticEval(p, b);
                    b.undo();
                    if (evalValue > bestSoFar) {
                        bestSoFar = evalValue;
                        indexOfBest = moves.get(i);
                        if (evalValue >= cutoff) {
                            break;
                        }
                    }
                }
            }

        } else {
            for (int j = 0; j < moves.size(); j++) {
                if (moves.get(j) == null) {
                    continue;
                } else  {
                    b.addSpot(p, moves.get(j));
                    for (int k = 0; k < moves.size(); k++) {
                        if (b.isLegal(p, moves.get(k))) {
                            validMoves.add(moves.get(k));
                        }
                    }
                    int evalValue = staticEval(p, b);
                    int response = minmax(p.opposite(), b, d - 1,
                            bestSoFar, validMoves);
                    if (evalValue > bestSoFar) {
                        bestSoFar = evalValue;
                        indexOfBest = moves.get(j);
                        if (evalValue >= cutoff) {
                            break;
                        }
                    }
                    b.undo();
                }
            }
        }
        return indexOfBest;
    }

    /** Returns heuristic value of board B for player P.
     *  Higher is better for P. */
    private int staticEval(Side p, Board b) {
        int numOfP = b.numOfSide(p);
        int numOppP = b.numOfSide(p.opposite());
        return numOfP - numOppP;
    }

}
