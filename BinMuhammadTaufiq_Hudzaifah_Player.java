import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class BinMuhammadTaufiq_Hudzaifah_Player extends Player {

	/**
	 * Strategy: Adaptive 4-Layer Agent
	 *
	 * Layer 1: Reward sustained cooperation.
	 *   If both opponents have cooperated >= 85% of the time AND both
	 *   cooperated last round, cooperate to sustain the relationship.
	 *
	 * Layer 2: Punish confirmed nasty opponents.
	 *   If either opponent has defected >= 75% of the time AND defected
	 *   last round, defect to protect myself.
	 *
	 * Layer 3: React to recent history (last 2 rounds).
	 *   If either opponent defected in either of the last 2 rounds, defect.
	 *   Otherwise cooperate.
	 *
	 * Layer 4: SoreLoser fallback.
	 *   Cooperate if winning or tied, defect if losing.
	 */

	int r;
	int[] myHist, opp1Hist, opp2Hist;
	int myScore = 0, opp1Score = 0, opp2Score = 0;
	double opp1Def = 0, opp2Def = 0;

	final double FRIENDLY_THRESHOLD  = 0.850;
	final double DEFENSIVE_THRESHOLD = 0.750;

	int[][][] payoff = {
		{{6, 3}, {3, 0}},
		{{8, 5}, {5, 2}}
	};

	int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

		if (n == 0) return 0;

		this.r        = n - 1;
		this.myHist   = myHistory;
		this.opp1Hist = oppHistory1;
		this.opp2Hist = oppHistory2;

		int myLA   = myHistory[r];
		int opp1LA = oppHistory1[r];
		int opp2LA = oppHistory2[r];

		// Update cumulative scores
		this.myScore   += payoff[myLA][opp1LA][opp2LA];
		this.opp1Score += payoff[opp1LA][opp2LA][myLA];
		this.opp2Score += payoff[opp2LA][opp1LA][myLA];

		// Update defection counts
		opp1Def += oppHistory1[r];
		opp2Def += oppHistory2[r];

		// Calculate cooperation and defection probabilities
		double opp1DefProb  = opp1Def / oppHistory1.length;
		double opp2DefProb  = opp2Def / oppHistory2.length;
		double opp1CoopProb = 1.0 - opp1DefProb;
		double opp2CoopProb = 1.0 - opp2DefProb;

		// ----------------------------------------------------------------
		// LAYER 1 — REWARD SUSTAINED COOPERATION
		// Both opponents have been cooperative >= 85% AND cooperated
		// last round — sustain the mutual cooperation equilibrium.
		// ----------------------------------------------------------------
		if (opp1CoopProb >= FRIENDLY_THRESHOLD
				&& opp2CoopProb >= FRIENDLY_THRESHOLD
				&& opp1LA == 0
				&& opp2LA == 0) {
			return applyNoise(0, 99);
		}

		// ----------------------------------------------------------------
		// LAYER 2 — PUNISH CONFIRMED NASTY OPPONENTS
		// Either opponent has defected >= 75% of the time AND defected
		// last round — defect to protect myself.
		// ----------------------------------------------------------------
		if ((opp1DefProb >= DEFENSIVE_THRESHOLD || opp2DefProb >= DEFENSIVE_THRESHOLD)
				&& (opp1LA == 1 || opp2LA == 1)) {
			return applyNoise(1, 99);
		}

		// ----------------------------------------------------------------
		// LAYER 3 — REACT TO RECENT HISTORY (last 2 rounds)
		// If either opponent defected in the last 2 rounds, defect.
		// Otherwise cooperate — recent behaviour is most predictive.
		// ----------------------------------------------------------------
		if (n >= 2) {
			if (opp1LA == 1 || opp2LA == 1
					|| oppHistory1[n-2] == 1 || oppHistory2[n-2] == 1) {
				return 1;
			} else {
				return applyNoise(0, 99);
			}
		}

		// ----------------------------------------------------------------
		// LAYER 4 — SORE LOSER FALLBACK
		// Cooperate if winning or tied, defect if losing.
		// ----------------------------------------------------------------
		if (myScore >= opp1Score && myScore >= opp2Score) return 0;
		return 1;
	}

	private int applyNoise(int intendedAction, int pct) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>() {{
			put(intendedAction, pct);
			put(getOppAction(intendedAction), 1 - pct);
		}};
		LinkedList<Integer> list = new LinkedList<>();
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) list.add(entry.getKey());
		}
		Collections.shuffle(list);
		return list.pop();
	}

	private int getOppAction(int action) {
		if (action == 1) return 0;
		return 1;
	}
}
