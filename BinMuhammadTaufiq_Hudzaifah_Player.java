class BinMuhammadTaufiq_Hudzaifah_Player extends Player {

	/**
	 * Strategy: Adaptive 4-Rule Agent
	 *
	 * Rule 0 (overarching): Be trustworthy AND unpredictable.
	 *   Execute intended action 99% of the time, opposite 1% of the time.
	 *
	 * Rule 1: Protect myself.
	 *   After n > 100, if BOTH opponents' cooperation probability is below
	 *   STRICT_THRESHOLD (0.750), defect. Only fires late so early noise
	 *   does not trigger premature punishment.
	 *
	 * Rule 2: Cooperate in a cooperative environment.
	 *   If BOTH opponents cooperated last round AND both have cooperation
	 *   probability above LENIENT_THRESHOLD (0.705), cooperate.
	 *   This is the primary path for the majority of rounds.
	 *
	 * Rule 3: Sore Loser fallback.
	 *   When Rules 1 and 2 do not apply, use cumulative score as signal.
	 *   If winning or tied: cooperate (no need to act).
	 *   If losing: defect (stop the gap from widening).
	 */

	// Previous round index
	int r;

	// History references — updated each round for use in helper methods
	int[] myHist, opp1Hist, opp2Hist;

	// Cumulative scores — tracked manually using payoff matrix each round
	int myScore = 0, opp1Score = 0, opp2Score = 0;

	// Running cooperation counts for each opponent (full history, not rolling)
	int opp1Coop = 0;
	int opp2Coop = 0;

	// Lenient threshold — used in Rule 2 to reward broadly cooperative opponents
	final double LENIENT_THRESHOLD = 0.705;

	// Strict threshold — used in Rule 1 only after n > 100 for reliable stats
	final double STRICT_THRESHOLD = 0.750;

	// Local copy of payoff matrix for score tracking in helper methods
	int[][][] payoff = {
		{{6, 3}, {3, 0}},
		{{8, 5}, {5, 2}}
	};

	int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

		// Always cooperate on the first round — establish goodwill
		if (n == 0) return 0;

		// Update round index and history references for helper methods
		this.r        = n - 1;
		this.myHist   = myHistory;
		this.opp1Hist = oppHistory1;
		this.opp2Hist = oppHistory2;

		// Capture last actions for all three players
		int myLA   = myHistory[r];
		int opp1LA = oppHistory1[r];
		int opp2LA = oppHistory2[r];

		// Update cumulative scores using payoff matrix
		this.myScore   += payoff[myLA][opp1LA][opp2LA];
		this.opp1Score += payoff[opp1LA][opp2LA][myLA];
		this.opp2Score += payoff[opp2LA][opp1LA][myLA];

		// Update cooperation counters (getOppAction returns 1 if they cooperated)
		opp1Coop += getOppAction(opp1Hist[r]);
		opp2Coop += getOppAction(opp2Hist[r]);

		// Calculate cooperation probabilities over full history
		double opp1CoopProb = (double) opp1Coop / opp1Hist.length;
		double opp2CoopProb = (double) opp2Coop / opp2Hist.length;

		// ----------------------------------------------------------------
		// EARLY NASTY DETECTION
		// After 10 rounds, if neither opponent has cooperated even once,
		// they are pure defectors — no need to wait until round 100.
		// ----------------------------------------------------------------
		if (n > 10 && opp1Coop == 0 && opp2Coop == 0) {
			return 1;
		}

		// ----------------------------------------------------------------
		// RULE 1 — PROTECT MYSELF
		// After 100 rounds the cooperation probability is statistically
		// reliable. If both opponents cooperate less than 75% of the time,
		// they are confirmed as predominantly nasty — defect to cut losses.
		// ----------------------------------------------------------------
		if ((n > 100)
				&& (opp1CoopProb < STRICT_THRESHOLD)
				&& (opp2CoopProb < STRICT_THRESHOLD)) {
			return applyNoise(1, 99);
		}

		// ----------------------------------------------------------------
		// ASYMMETRIC OPPONENT HANDLING
		// One opponent is confirmed nasty, the other is cooperative.
		// Cooperate to preserve the good relationship — the nasty opponent
		// hurts us regardless of our action, so don't sacrifice the good one.
		// ----------------------------------------------------------------
		boolean opp1Nasty = (n > 100) && (opp1CoopProb < STRICT_THRESHOLD);
		boolean opp2Nasty = (n > 100) && (opp2CoopProb < STRICT_THRESHOLD);
		if ((opp1Nasty && opp2CoopProb > LENIENT_THRESHOLD)
				|| (opp2Nasty && opp1CoopProb > LENIENT_THRESHOLD)) {
			return applyNoise(0, 99);
		}

		// ----------------------------------------------------------------
		// RULE 2 — COOPERATE IN A COOPERATIVE ENVIRONMENT
		// Both cooperated last round AND both have cooperative histories.
		// Sustain mutual cooperation (6 pts/round is the optimal equilibrium).
		// ----------------------------------------------------------------
		if ((opp1LA + opp2LA == 0)
				&& (opp1CoopProb > LENIENT_THRESHOLD)
				&& (opp2CoopProb > LENIENT_THRESHOLD)) {
			return applyNoise(0, 99);
		}

		// ----------------------------------------------------------------
		// RULE 3 — SORE LOSER FALLBACK (proportional)
		// Use the size of the score gap, not just win/lose:
		//   Gap > 50  → defect (hemorrhaging, must act)
		//   Gap < 10  → cooperate (trivial gap, not worth breaking trust)
		//   Otherwise → defect (meaningful deficit, stop the bleeding)
		// ----------------------------------------------------------------
		return handleAmbiguousCase();
	}

	/**
	 * Rule 0: Introduces 1% noise so the agent is unpredictable.
	 * Executes intendedAction (pct)% of the time, opposite (1-pct)%.
	 */
	private int applyNoise(int intendedAction, int pct) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>() {{
			put(intendedAction, pct);
			put(getOppAction(intendedAction), 1 - pct);
		}};
		LinkedList<Integer> list = new LinkedList<>();
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				list.add(entry.getKey());
			}
		}
		Collections.shuffle(list);
		return list.pop();
	}

	/**
	 * Rule 3 (proportional): Cooperate if winning or gap is trivial,
	 * defect only when the deficit is meaningful.
	 */
	private int handleAmbiguousCase() {
		int maxOpponentScore = Math.max(opp1Score, opp2Score);
		int gap = maxOpponentScore - myScore;
		if (gap < 10) return 0;  // winning or close — cooperate
		if (gap > 50) return 1;  // far behind — defect hard
		return 1;                // meaningful deficit — defect
	}

	/**
	 * Returns the opposite action (0→1, 1→0).
	 * Also used to convert a cooperation move into a +1 cooperation count.
	 */
	private int getOppAction(int action) {
		if (action == 1) return 0;
		return 1;
	}
}
