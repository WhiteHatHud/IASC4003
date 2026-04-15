package com.cz4046;

class BinMuhammadTaufiq_Hudzaifah_Player extends Player {

	final String NAME = "BinMuhammadTaufiq_Hudzaifah";
	final String MATRIC_NO = "U2320600F";

	int[][][] payoff = {
		{{6, 3}, {3, 0}},
		{{8, 5}, {5, 2}}
	};

	int myScore = 0, opp1Score = 0, opp2Score = 0;
	int opp1Def = 0, opp2Def = 0;

	int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
		if (n == 0) return 0;

		int r = n - 1;
		int opp1LA = oppHistory1[r];
		int opp2LA = oppHistory2[r];

		myScore   += payoff[myHistory[r]][opp1LA][opp2LA];
		opp1Score += payoff[opp1LA][opp2LA][myHistory[r]];
		opp2Score += payoff[opp2LA][myHistory[r]][opp1LA];

		opp1Def += opp1LA;
		opp2Def += opp2LA;

		double opp1DefRate = (double) opp1Def / n;
		double opp2DefRate = (double) opp2Def / n;

		// Rule 1: Punish confirmed defectors (both defecting >50% overall)
		if (opp1DefRate > 0.5 && opp2DefRate > 0.5) return 1;

		// Rule 2: Retaliate only if BOTH defected last round
		if (opp1LA == 1 && opp2LA == 1) return 1;

		// Rule 3: Otherwise cooperate
		return 0;
	}

	private int oppAction(int action) {
		if (action == 1) return 0;
		return 1;
	}
}
