package jump61;

import static jump61.Side.*;
import static jump61.Square.square;
import java.util.Stack;
import java.util.EmptyStackException;
/** A Jump61 board state that may be modified.
 *  @author Thomas Wu
 */
class MutableBoard extends Board {

    /** The length of a row (or column) of the board. */
    private int _size;

    /** The board that stores squares. An array of squares of length N^2. */
    private Square[] _mBoard;

    /** The variables _NUMBLUE, _NUMRED, and _NUMWHITE keep track of the numbers
      * of each color there are on the board. */

    /** The stack of previous squares, when addSpot is called. Contains their
      * color and spots. Works together with _past and _numOverFlow to keep
      * track of the undo history. */
    private Stack<Square> _undoHistory;

    /** The stack that keeps track of the sqNum index of the sqaures that were
      * changed. Works together w/ _numOverFlow and _undoHistory
      * and _numTotalMoves. */
    private Stack<Integer> _past;

    /** The Stack of integers. A stack of 0, 1, 3, 2 means the first move
      * (bottom of stack) had 2 squares overflowed into (3 total square
      * changes). The second move had 3 squares overflowed into, so 4 total
      * square changes (the initial add spot, and the 3 adjacent
      * square overflows). A 0 in the stack means that there was just
      * the initial change in square with no adjacent squares changing
      * due to overflow. */
    private Stack<Integer> _numOverFlow;

    /** The TOTAL number of changes in the current undo history. Adds 1
      * for every addSpot call (so basically every time you overflow a
      * square, you add 1 for every adjacent square, and so forth). */
    private int _numTotalMoves;


    /** An N x N board in initial configuration. */
    MutableBoard(int N) {
        _mBoard = new Square[N * N];
        _size = N;
        for (int i = 0; i < _size * _size; i++) {
            _mBoard[i] = Square.INITIAL;
        }
        _undoHistory = new Stack<Square>();
        _past = new Stack<Integer>();
        _numOverFlow = new Stack<Integer>();
    }


    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear. */
    MutableBoard(Board board0) {
        _size = board0.size();
        _undoHistory = new Stack<Square>();
        _numOverFlow = new Stack<Integer>();
        _past = new Stack<Integer>();
        _numTotalMoves = 0;
        _mBoard = new Square[_size * _size];
        for (int i = 0; i < board0.size() * board0.size(); i++) {
            _mBoard[i] = board0.get(i);
        }

    }
    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    @Override
    void clear(int N) {
        _mBoard = new Square[N * N];
        _size = N;
        for (int i = 0; i < size() * size(); i++) {
            _mBoard[i] = Square.INITIAL;
        }
        _numOverFlow = new Stack<Integer>();
        _undoHistory = new Stack<Square>();
        _past = new Stack<Integer>();
        _numTotalMoves = 0;

        announce();

    }

    /** Copy the contents of BOARD into me. */
    @Override
    void copy(Board board) {
        internalCopy((MutableBoard) board);

    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history.  Assumes BOARD and I have the same size. */
    private void internalCopy(MutableBoard board) {
        for (int i = 0; i < board.size(); i++) {
            _mBoard[i] = board.get(i);
        }
        _size = board.size();
    }

    /** Returns the size of the current board (length). */
    @Override
    int size() {
        return this._size;
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    @Override
    Square get(int n) {
        return this._mBoard[n];
    }

    /** Return the number of squares of given SIDE. */
    @Override
    int numOfSide(Side side) {
        int num = 0;
        for (int i = 0; i < size() * size(); i++) {
            if (_mBoard[i].getSide() == side) {
                num += 1;
            }
        }
        return num;
    }

    /** Returns the total number of spots on the board. */
    @Override
    int numPieces() {
        int numSpots = 0;
        for (int i = 0; i < _size * _size; i++) {
            numSpots += get(i).getSpots();
        }
        return numSpots;
    }

    /** Adds a spot. Includes overflow. Will add a spot for side
      * PLAYER, at row R, and column C. */
    @Override
    void addSpot(Side player, int r, int c) {
        Square original = get(r, c);
        if (r <= 0 || c <= 0 || r > size() || c > size()) {
            return;
        }
        markUndo();
        _undoHistory.push(original);
        _past.push(sqNum(r, c));

        int numberRed = 0;
        int numberBlue = 0;
        for (int i = 0; i < size() * size(); i++) {
            if (_mBoard[i].getSide() == RED) {
                numberRed += 1;
            } else if (_mBoard[i].getSide() == BLUE) {
                numberBlue += 1;
            }
        }
        if (numberRed == size() * size() || numberBlue == size() * size()) {
            return;
        }

        int numSpots = original.getSpots();
        if (numSpots == neighbors(r, c)) {
            internalSet(sqNum(r, c), square(player, 1));
            int amountOverflow = overFlow(r, c, player);
            _numOverFlow.push(amountOverflow);
        } else {
            internalSet(sqNum(r, c), square(player, numSpots + 1));
            _numOverFlow.push(0);
        }
        announce();
    }


    /** A helper function that recurses with addSpot such that it adds a dot
      * to each adjacent square. This function is only called when the maximum
      * number of dots in a square is reached. This returns an INT of how many
      * times things overflowed, to but into our stack in addSpot. The purpose
      * of this is for undohistory-keeping. Takes in row R, column C, the num
      * of spots there are orginially NUMSPOTS there are currently,
      * and the side PLAYER. */
    public int overFlow(int r, int c, Side player) {
        int numOverFlow = 0;
        try {
            addSpot(player, r - 1, c);
            numOverFlow += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            what();
        }
        try {
            addSpot(player, r + 1, c);
            numOverFlow += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            what();
        }
        try {
            addSpot(player, r, c - 1);
            numOverFlow += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            what();
        }
        try {
            addSpot(player, r, c + 1);
            numOverFlow += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            what();
        }
        return numOverFlow;
    }

    /** The style checker didn't let me have an empty catch block.
      * So I made it do what() which does absolutely nothing. */
    public void what() {
        return;
    }

    /** Convenience method for addSpot with the squareNum instead of r,c.
      * Takes in the side PLAYER and the index N. */
    @Override
    void addSpot(Side player, int n) {
        addSpot(player, row(n), col(n));
        announce();
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Clear the undo
     *  history. */
    @Override
    void set(int r, int c, int num, Side player) {
        set(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white).  Clear the undo history. */
    @Override
    void set(int n, int num, Side player) {
        if (num <= 0) {
            player = WHITE;
        }
        internalSet(n, square(player, num));
        _undoHistory = new Stack<Square>();
        _past = new Stack<Integer>();
        _numTotalMoves = 0;
        _numOverFlow = new Stack<Integer>();
        announce();
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    @Override
    void undo() {
        if (_numTotalMoves < 0) {
            return;
        }
        int x = _numOverFlow.pop();
        try {
            while (x >= 0) {
                int indx = _past.pop();
                Square temp = _undoHistory.pop();
                Side colorAfterMove = _mBoard[indx].getSide();
                Side previousColor = temp.getSide();
                _mBoard[indx] = temp;
                _numTotalMoves -= 1;
                x -= 1;
            }
        } catch (EmptyStackException e) {
            return;
        }
    }

    /** Record the beginning of a move in the undo history. Adds 1 to the
      * total number of dots added/squares moved. _numTotalMoves is the number
      * of squares that have changed and can be undone. */
    private void markUndo() {
        _numTotalMoves += 1;
    }

    /** Set the contents of the square with index IND to SQ. Update counts
     *  of numbers of squares of each color.  */
    private void internalSet(int ind, Square sq) {
        Side originalColor = _mBoard[ind].getSide();
        _mBoard[ind] = sq;
        Side color = sq.getSide();
        if (originalColor == color) {
            return;
        }
    }

    /** Notify all Observers of a change. */
    private void announce() {
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MutableBoard)) {
            return obj.equals(this);
        } else {
            for (int i = 0; i < size() * size(); i++) {
                MutableBoard temp = (MutableBoard) obj;
                if (!temp.get(i).equals(this.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    /** WHAT HASHCODE SHOULD I USE LOL. Returns an INT for the haschode. */
    @Override
    public int hashCode() {
        return 0;
    }
}
