package mnkgame;

import java.util.Random;

public class NostroPlayer implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private MNKCellState me;
	private MNKCellState opponent;
	private int TIMEOUT;
	private int M, N, K;

	private int pesi[];
	private MNKCell bestMove;
	private long timerStart;
	private int maxDepth;
	private int myMovesToWin[];
	private int yourMovesToWin[];

	public int[][] positionWeights;
	private MNKCellState[][] winSequence;
	private MNKCellState[][] sevenTrapSequence;
	private MNKCellState[][] openEndSequence;
	private MNKCellState[][] threatSequence;
	private final int[] evalWeights = { 1000000, 100, 10, 15, 20 };

	private int gameStateCounter, numMosse; // debug variables

	/**
	 * Default empty constructor
	 */
	public NostroPlayer() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		maxDepth = 1;
		// New random seed for each game
		rand = new Random(System.currentTimeMillis());
		B = new MNKBoard(M, N, K);
		myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		me = first ? MNKCellState.P1 : MNKCellState.P2;
		opponent = first ? MNKCellState.P2 : MNKCellState.P1;
		TIMEOUT = timeout_in_secs;
		this.M = M;
		this.N = N;
		this.K = K;

		pesi = new int[MNKGameState.WINP2.ordinal() + 1];
		pesi[myWin.ordinal()] = 1;
		pesi[MNKGameState.DRAW.ordinal()] = 0;
		pesi[yourWin.ordinal()] = -1;

		// debug variables
		gameStateCounter = 0;
		numMosse = 0;

		createPositionWeights();
		createWinSequence();
		createSevenTrapSequence();
		createOpenEndSequence();
		createThreatSequence();

		// for (int i = 0; i < 3; i++) {
		// for (int j = 0; j < K; j++) {
		// System.out.print(sevenTrapSequence[i][j] + " ");
		// }
		// System.out.println();
		// }

	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		timerStart = System.currentTimeMillis();

		gameStateCounter = 0;
		numMosse += 1;

		if (MC.length > 0) {
			MNKCell c = MC[MC.length - 1]; // Recover the last move from MC
			B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
		}
		// If there is just one possible move, return immediately
		if (FC.length == 1)
			return FC[0];

		bestMove = FC[rand.nextInt(FC.length)];

		// for (MNKCell c : FC) {
		// B.markCell(c.i, c.j);
		// System.out.println("[" + c.i + ", " + c.j + "]: " + eval(B));
		// B.unmarkCell();
		// }

		iterativeDeepening(B, true, 9);

		// debugMessage(false);
		B.markCell(bestMove.i, bestMove.j);
		return bestMove;
	}

	public String playerName() {
		return "TicTacToe PRO"; // TODO: scegliere un nome
	}

	private int alphabeta(MNKBoard b, int depth, int alpha, int beta, boolean isMaximazing) {
		gameStateCounter += 1;
		// if we are in a terminal state, evaluate the score
		MNKGameState result = b.gameState();
		MNKCell FC[] = b.getFreeCells();
		int bestScore;
		if (result != MNKGameState.OPEN || depth >= maxDepth) {
			int ev = eval(b);
			// System.out.println(ev + "\n");
			return ev;
		}

		if (isMaximazing) {
			bestScore = Integer.MIN_VALUE;
			for (MNKCell c : FC) {
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (99.0 / 100.0))
					return bestScore;

				B.markCell(c.i, c.j);
				int score = alphabeta(B, depth + 1, alpha, beta, false);
				B.unmarkCell();

				if (score > bestScore) {
					bestScore = score;
					if (depth == 0)
						bestMove = c;
				}
				// bestScore = Math.max(score, bestScore);
				alpha = Math.max(alpha, bestScore);
				if (alpha >= beta)
					break; // beta cutoff

			}
		} else {
			bestScore = Integer.MAX_VALUE;
			for (MNKCell c : FC) {
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (99.0 / 100.0))
					return bestScore;

				B.markCell(c.i, c.j);
				int score = alphabeta(B, depth + 1, alpha, beta, true);
				B.unmarkCell();

				if (score < bestScore) {
					bestScore = score;
					if (depth == 0)
						bestMove = c;
				}
				// bestScore = Math.min(score, bestScore);
				beta = Math.min(beta, bestScore);
				if (beta <= alpha)
					break;
			}
		}
		return bestScore;
	}

	private int iterativeDeepening(MNKBoard b, boolean isMaximazing, int depth) {
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		int eval = 0;
		for (int i = 0; i < depth; i++) {
			eval = alphabeta(b, 0, alpha, beta, isMaximazing);
			maxDepth += 1;
		}
		return eval;
	}

	private int eval(MNKBoard b) {
		MNKGameState result = b.gameState();
		MNKCell FC[] = b.getFreeCells();
		int count = 0;

		if (result != MNKGameState.OPEN)
			return pesi[result.ordinal()] * (1 + FC.length);

		// for (MNKCell cell : FC) {
		// if (isWinningCell(cell.i, cell.j, K - 1)) {
		// count += 100;
		// } else
		// count -= 50;
		// }
		return 15 * (evalPositionWeights(b, true) - evalPositionWeights(b, false));
		// TODO: forse è più efficiente fare (M*N-depth) al posto di FC.lenght?
		// verificare se sono uguali in primo luogo
	}

	private boolean isWinningCell(int i, int j, int target) {
		MNKCellState s = B.cellState(i, j);
		int n;

		// Useless pedantic check
		if (s == MNKCellState.FREE)
			return false;

		MNKCellState notS = s == MNKCellState.P1 ? MNKCellState.P2 : MNKCellState.P1;

		int freeN = 0;
		int emptyCheck = K - target;

		// Horizontal check
		n = 1;
		for (int k = 1; j - k >= 0 && freeN <= emptyCheck; k++) {
			MNKCellState p = B.cellState(i, j - k);
			if (p == MNKCellState.FREE)
				freeN += 1;
			else if (p != s)
				break;
			n++;
		} // backward check
		for (int k = 1; j + k < N && freeN <= emptyCheck; k++) {
			MNKCellState p = B.cellState(i, j - k);
			if (p == MNKCellState.FREE)
				freeN += 1;
			else if (p != s)
				break;
			n++;
		} // forward check
		if (n >= target)
			return true;

		// Vertical check
		n = 1;
		for (int k = 1; i - k >= 0 && B.cellState(i - k, j) == s; k++)
			n++; // backward check
		for (int k = 1; i + k < M && B.cellState(i + k, j) == s; k++)
			n++; // forward check
		if (n >= target)
			return true;

		// Diagonal check
		n = 1;
		for (int k = 1; i - k >= 0 && j - k >= 0 && B.cellState(i - k, j - k) == s; k++)
			n++; // backward check
		for (int k = 1; i + k < M && j + k < N && B.cellState(i + k, j + k) == s; k++)
			n++; // forward check
		if (n >= target)
			return true;

		// Anti-diagonal check
		n = 1;
		for (int k = 1; i - k >= 0 && j + k < N && B.cellState(i - k, j + k) == s; k++)
			n++; // backward check
		for (int k = 1; i + k < M && j - k >= 0 && B.cellState(i + k, j - k) == s; k++)
			n++; // backward check
		if (n >= target)
			return true;

		return false;
	}

	/**
	 * is in the bounds on the board, cost: O(1).
	 * 
	 * @param x pos x.
	 * @param y pos y.
	 * @return true if is in the bound, false if else.
	 */
	private boolean inBounds(int x, int y) {
		return ((0 <= x) && (x < M) && (0 <= y) && (y < N)) ? true : false;
	}

	private int evalPositionWeights(MNKBoard b, boolean isAi) {
		// int player = (isAi) ? aiPosition : humanPosition;
		MNKCellState player = (isAi) ? me : opponent;
		int score = 0;

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if (b.cellState(i, j) == player) {
					score += positionWeights[i][j];
				}
			}
		}

		return score;
	}

	private void createPositionWeights() {
		positionWeights = new int[M][N];

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if (inBounds(i + (K - 1), j)) {
					for (int l = 0; l < K; l++) {
						positionWeights[i + l][j]++;
					}
				}

				if (inBounds(i, j + (K - 1))) {
					for (int l = 0; l < K; l++) {
						positionWeights[i][j + l]++;
					}
				}

				if (inBounds(i + (K - 1), j + (K - 1))) {
					for (int l = 0; l < K; l++) {
						positionWeights[i + l][j + l]++;
					}
				}

				if (inBounds(i + (K - 1), j - (K - 1))) {
					for (int l = 0; l < K; l++) {
						positionWeights[i + l][j - l]++;
					}
				}
			}
		}
	}

	private int evalWins(MNKBoard state, boolean isAi) {
		MNKCellState player = (isAi) ? me : opponent;
		return countPositionForward(state, winSequence[player.ordinal()]);
	}

	private void createWinSequence() {
		winSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K];

		for (MNKCellState i : MNKCellState.values()) {
			for (int j = 0; j < K; j++) {
				winSequence[i.ordinal()][j] = i;
			}
		}
	}

	private int evalSevenTraps(MNKBoard state, boolean isAi) {
		MNKCellState player = (isAi) ? me : opponent;

		int sevenTraps = 0;

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N - 1; j++) {
				if (match(state, i, j, sevenTrapSequence[player.ordinal()], -1, 0, 1)) {
					if (match(state, i, j + 1, sevenTrapSequence[player.ordinal()], -1, -1, 1)) {
						sevenTraps++;
					}
				}

				if (match(state, i, j, sevenTrapSequence[player.ordinal()], 1, 0, 1)) {
					if (match(state, i, j + 1, sevenTrapSequence[player.ordinal()], 1, -1, 1)) {
						sevenTraps++;
					}
				}
			}
		}

		for (int i = 0; i < M; i++) {
			for (int j = 1; j < N; j++) {
				if (match(state, i, j, sevenTrapSequence[player.ordinal()], -1, 0, 1)) {
					if (match(state, i, j - 1, sevenTrapSequence[player.ordinal()], -1, 1, 1)) {
						sevenTraps++;
					}
				}

				if (match(state, i, j, sevenTrapSequence[player.ordinal()], 1, 0, 1)) {
					if (match(state, i, j - 1, sevenTrapSequence[player.ordinal()], 1, 1, 1)) {
						sevenTraps++;
					}
				}
			}
		}

		return sevenTraps;
	}

	private void createSevenTrapSequence() {
		sevenTrapSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K];

		for (MNKCellState i : MNKCellState.values()) {
			sevenTrapSequence[i.ordinal()][0] = MNKCellState.FREE;
			for (int j = 1; j < K; j++) {
				sevenTrapSequence[i.ordinal()][j] = i;
			}
		}
	}

	private int evalOpenEnds(MNKBoard state, boolean isAi) {
		MNKCellState player = (isAi) ? me : opponent;

		return (countPositionForward(state, openEndSequence[player.ordinal()])
				+ countPositionBackward(state, openEndSequence[player.ordinal()]));
	}

	private void createOpenEndSequence() {
		openEndSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K + 1];
		for (MNKCellState i : MNKCellState.values()) {
			openEndSequence[i.ordinal()][0] = MNKCellState.FREE;
			for (int j = 1; j < K - 1; j++) {
				openEndSequence[i.ordinal()][j] = i;
			}
			openEndSequence[i.ordinal()][K - 1] = MNKCellState.FREE;
			openEndSequence[i.ordinal()][K] = MNKCellState.FREE;
		}
	}

	private int evalThreats(MNKBoard state, boolean isAi) {
		MNKCellState player = (isAi) ? me : opponent;

		return (countPositionForward(state, threatSequence[player.ordinal()])
				+ countPositionBackward(state, threatSequence[player.ordinal()]));
	}

	private void createThreatSequence() {
		threatSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K];
		for (MNKCellState i : MNKCellState.values()) {
			threatSequence[i.ordinal()][0] = MNKCellState.FREE;
			for (int j = 1; j < K; j++) {
				threatSequence[i.ordinal()][j] = i;
			}
		}

	}

	private void debugMessage(boolean timeout) {
		if (timeout)
			System.out.print("time ended, ");
		System.out.println(
				"(" + playerName() + ")Stati di gioco valutati alla mossa " + numMosse + ": " + gameStateCounter);
	}

	/**
	 * match, cost: O(|sequence|) = O(K).
	 * 
	 * @param board      board.
	 * @param x          pos x.
	 * @param y          pos y.
	 * @param sequence   sequence of simbols to find.
	 * @param directionX direction x.
	 * @param directionY direction y.
	 * @param revert     sequence revertion.
	 * @return true if is thera a sequence, false if else.
	 */
	private boolean match(MNKBoard board, int x, int y, MNKCellState[] sequence, int directionX, int directionY,
			int revert) {
		int s = revert > 0 ? 0 : sequence.length - 1;

		if (this.inBounds(x + directionX * (sequence.length - 1), y + directionY * (sequence.length - 1))) {
			for (int i = 0; i < sequence.length; i++) {
				if (board.cellState(x, y) != sequence[s])
					return false;
				x += directionX;
				y += directionY;
				s += revert;
			}
			return true;
		}
		return false;
	}

	/**
	 * match position FIXED(ideas in meating 15/11/22), cost: O(4K) ~ O(K).
	 * 
	 * @param board     board.
	 * @param x         pos x.
	 * @param y         pos y.
	 * @param sequence  sequence of simbols to find.
	 * @param direction sequence direction.
	 * @return number of sequence starting from the specified position.
	 */
	private int matchPosition(MNKBoard board, int x, int y, MNKCellState[] sequence, int direction) {
		int starting = 0;
		// horiz
		if (match(board, x, y, sequence, 1, 0, direction))
			starting++;
		// vertic
		if (match(board, x, y, sequence, 0, 1, direction))
			starting++;
		// diag right
		if (match(board, x, y, sequence, 1, 1, direction))
			starting++;
		// diag left
		if (match(board, x, y, sequence, 1, -1, direction))
			starting++;
		return starting;
	}

	/**
	 * Find out how many sequence are there in the board(it scans forwards), cost:
	 * O(4MNK) ~ O(MNK).
	 * 
	 * @param board    board to check.
	 * @param sequence looking for sequence.
	 * @return number os sequence.
	 */
	private int countPositionForward(MNKBoard board, MNKCellState[] sequence) {
		int sequenceCount = 0;
		for (int i = 0; i < board.N; i++)
			for (int j = 0; j < board.M; j++)
				sequenceCount += matchPosition(board, i, j, sequence, 1);
		return sequenceCount;
	}

	/**
	 * Find out how many sequence are there in the board(it scans backwards), cost:
	 * O(4MNK) ~ O(MNK).
	 * 
	 * @param board    board to check.
	 * @param sequence looking for sequence.
	 * @return number os sequence.
	 */
	private int countPositionBackward(MNKBoard board, MNKCellState[] sequence) {
		int sequenceCount = 0;
		for (int i = 0; i < board.N; i++)
			for (int j = 0; j < board.M; j++)
				sequenceCount += matchPosition(board, i, j, sequence, -1);
		return sequenceCount;
	}
}
