package bTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An Island in a Linear Graph with a set of Hypotheses
 * 
 * @author Andrea Gesmundo
 */
public class Island
{

	public TagSample sen;

	public int lastPosi; // position of the last action

	public int leftBoundPosi; // position of the first word in the span

	public int rightBoundPosi; // position of the last word in the span

	public Island islandFromLeft;

	public Island islandFromRight;

	// pair of hypotheses as interface of hypotheses
	public List<Socket> leftBoundSocket;

	public List<Socket> rightBoundSocket;

	public Hypothesis[][][] hypo; // leftSktID * rightSktID * hypID ->

	// Hypotheses

	// Hypothesis with the highest lableScore
	public Hypothesis topOpHypo;

	public int topLeftBoundSktID;

	public int topRightBoundSktID;

	public int topHypoID;

	// Gold standard hypothesis; for training only
	public Hypothesis goldHypo;

	public int goldLeftBoundSktID;

	public int goldRightBoundSktID;

	/**
	 * The Tagger that generated this Island, the reference is needed to access
	 * his variables like the FeatLib
	 */
	public TagLearn tagger;

	// ///////////////////////////////////////////////////////////////////
	// CONSTRUCTORS

	/**
	 * For stand alone island at the beginning.
	 * 
	 * @param s
	 *            The sentence used to initialize the spans.
	 * @param posi
	 *            Position of the word which the span refer to.
	 * @param training
	 *            true if we are executing a training.
	 */
	public Island(TagLearn tagLearn, TagSample s, int posi, boolean training) {
		tagger = tagLearn;
		sen = s;
		lastPosi = posi;
		leftBoundPosi = posi;
		rightBoundPosi = posi;

		Set<Socket> leftSktSet = new HashSet<Socket>(); // all possible left
		// BOUND sockets
		Set<Socket> rightSktSet = new HashSet<Socket>(); // all possible right
		// BOUND sockets
		Map<List<Socket>, List<Hypothesis>> skt2hypos = new Hashtable<List<Socket>, List<Hypothesis>>();

		// generate all possible sockets and hypotheses depending on them for
		// all possible labels
		List<Label> target = tagger.labels
		.getCandidateLabelsFromLexicon(s.words[posi]);
		for (int j = 0; j < target.size(); j++) {
			Label label = target.get(j);
			genSktHypo(label, null, null, -1, 0, null, null, -1, 0, leftSktSet,
					rightSktSet, skt2hypos, training, true, 0);
		}

		// maintain k-best
		keepKBest(leftSktSet, rightSktSet, skt2hypos, training);

		// set the hypothesis with the highest labelScore
		setTopHypo();

		// gold standard for training
		if (training) {
			for (int j = 0; j < leftBoundSocket.size(); j++) {
				if (leftBoundSocket.get(j).sktLabel.get(0) == sen.tags[posi]) {
					goldLeftBoundSktID = j;
					break;
				}
			}
			for (int j = 0; j < rightBoundSocket.size(); j++) {
				if (rightBoundSocket.get(j).sktLabel.get(0) == sen.tags[posi]) {
					goldRightBoundSktID = j;
					break;
				}
			}
		}
	}

	/**
	 * Create an island in the left and right context
	 */
	public Island(TagLearn tagLearn, TagSample s, int posi, Island leftIsland,
			Island rightIsland, boolean training) {
		tagger = tagLearn;
		sen = s;
		lastPosi = posi;
		if (leftIsland == null) {
			leftBoundPosi = posi;
		} else {
			leftBoundPosi = leftIsland.leftBoundPosi;
		}
		if (rightIsland == null) {
			rightBoundPosi = posi;
		} else {
			rightBoundPosi = rightIsland.rightBoundPosi;
		}

		// DEBUG
		// if (posi < leftBoundPosi || posi > rightBoundPosi){
		// System.err.println("Error in creating BLinIsland");
		// }

		islandFromLeft = leftIsland;
		islandFromRight = rightIsland;

		// generate all possible sockets and hypotheses depending on them in all
		// contexts
		Set<Socket> leftSktSet = new HashSet<Socket>(); // all possible left
		// BOUND sockets
		Set<Socket> rightSktSet = new HashSet<Socket>(); // all possible right
		// BOUND sockets
		Map<List<Socket>, List<Hypothesis>> skt2hypos = new Hashtable<List<Socket>, List<Hypothesis>>();

		// for all possible labels
		List<Label> target = tagger.labels
		.getCandidateLabelsFromLexicon(s.words[posi]);
		for (int k = 0; k < target.size(); k++) {
			Label label = target.get(k);
			boolean contextgold = true;

			if (islandFromLeft == null && islandFromRight == null) {
				genSktHypo(label, null, null, -1, 0, null, null, -1, 0,
						leftSktSet, rightSktSet, skt2hypos, training, true, 0);
			}

			if (islandFromLeft != null && islandFromRight == null) {
				for (int i = 0; i < islandFromLeft.rightBoundSocket.size(); i++) {
					Socket leftContextSkt = islandFromLeft.rightBoundSocket
					.get(i);
					for (int m = 0; m < islandFromLeft.leftBoundSocket.size(); m++) {
						Socket leftBoundSkt = islandFromLeft.leftBoundSocket
						.get(m);
						Hypothesis leftCellTop = islandFromLeft.hypo[m][i][0];
						if (leftCellTop == null)
							continue; // no hypo that matchs the left and right
						// bound sockets
						double leftCellTopScore = leftCellTop.getHypoScore();
						contextgold = contextgold && leftCellTop.isgold;
						genSktHypo(label, leftBoundSkt, leftContextSkt, i,
								leftCellTopScore, null, null, -1, 0,
								leftSktSet, rightSktSet, skt2hypos, training,
								contextgold, leftCellTop.mistake);
					}
				}
			}

			if (islandFromLeft == null && islandFromRight != null) {
				for (int i = 0; i < islandFromRight.leftBoundSocket.size(); i++) {
					Socket rightContextSkt = islandFromRight.leftBoundSocket
					.get(i);
					for (int m = 0; m < islandFromRight.rightBoundSocket.size(); m++) {
						Socket rightBoundSkt = islandFromRight.rightBoundSocket
						.get(m);
						Hypothesis rightCellTop = islandFromRight.hypo[i][m][0];
						if (rightCellTop == null)
							continue; // no hypo that matchs the left and right
						// bound sockets
						double rightCellTopScore = rightCellTop.getHypoScore();
						contextgold = contextgold && rightCellTop.isgold;
						genSktHypo(label, null, null, -1, 0, rightBoundSkt,
								rightContextSkt, i, rightCellTopScore,
								leftSktSet, rightSktSet, skt2hypos, training,
								contextgold, rightCellTop.mistake);
					}
				}
			}

			if (islandFromLeft != null && islandFromRight != null) {
				for (int i = 0; i < islandFromLeft.rightBoundSocket.size(); i++) {
					Socket leftContextSkt = islandFromLeft.rightBoundSocket
					.get(i);
					for (int m = 0; m < islandFromLeft.leftBoundSocket.size(); m++) {
						Socket leftBoundSkt = islandFromLeft.leftBoundSocket
						.get(m);
						Hypothesis leftCellTop = islandFromLeft.hypo[m][i][0];
						if (leftCellTop == null)
							continue; // no matching hypo
						double leftCellTopScore = leftCellTop.getHypoScore();
						contextgold = contextgold && leftCellTop.isgold;

						for (int j = 0; j < islandFromRight.leftBoundSocket
						.size(); j++) {
							Socket rightContextSkt = islandFromRight.leftBoundSocket
							.get(j);
							for (int n = 0; n < islandFromRight.rightBoundSocket
							.size(); n++) {
								Socket rightBoundSkt = islandFromRight.rightBoundSocket
								.get(n);
								Hypothesis rightCellTop = islandFromRight.hypo[j][n][0];
								if (rightCellTop == null)
									continue; // no matching hypo
								double rightCellTopScore = rightCellTop
								.getHypoScore();
								contextgold = contextgold
								&& rightCellTop.isgold;
								genSktHypo(label, leftBoundSkt, leftContextSkt,
										i, leftCellTopScore, rightBoundSkt,
										rightContextSkt, j, rightCellTopScore,
										leftSktSet, rightSktSet, skt2hypos,
										training, contextgold,
										leftCellTop.mistake
										+ rightCellTop.mistake);
							}
						}
					}
				}
			}
		}

		// maintain k-best
		keepKBest(leftSktSet, rightSktSet, skt2hypos, training);

		// set the hypothesis with the highest labelScore
		setTopHypo();

		// gold standard for training
		if (training) {
			for (int j = 0; j < leftBoundSocket.size(); j++) {
				if (leftBoundSocket.get(j).sktLabel.get(0) == sen.tags[posi]) {
					goldLeftBoundSktID = j;
					break;
				}
			}
			for (int j = 0; j < rightBoundSocket.size(); j++) {
				if (rightBoundSocket.get(j).sktLabel.get(0) == sen.tags[posi]) {
					goldRightBoundSktID = j;
					break;
				}
			}
		}
	}

	// ///////////////////////////////////////////////////////////////////

	public String toString()
	{
		if (leftBoundPosi != rightBoundPosi)
			return "(" + leftBoundPosi + "," + lastPosi + "("
			+ sen.words[lastPosi] + ")," + rightBoundPosi + ")";
		return "" + lastPosi + "(" + sen.words[lastPosi] + ")";
	}

	/**
	 * Generate all possible sockets and hypotheses depending on them, according
	 * to left and righ Context sockets. !!! WE ASSUME TRIGRAM MODEL NOW!!!
	 */
	private void genSktHypo(Label label, Socket leftBoundSkt,
			Socket leftContextSkt, int leftContextSktID,
			double leftContextScore, Socket rightBoundSkt,
			Socket rightContextSkt, int rightContextSktID,
			double rightContextScore, Set<Socket> leftSktSet,
			Set<Socket> rightSktSet,
			Map<List<Socket>, List<Hypothesis>> skt2hypos, boolean training,
			boolean contextgold, int contextmistake)
	{

		Hypothesis onehypo = new Hypothesis(this, label);
		onehypo.socketIDFromLeft = leftContextSktID;
		onehypo.socketIDFromRight = rightContextSktID;

		// generate the new left bound socket (!!!Bigram Socket!!!)
		List<Label> skv = new ArrayList<Label>();
		if (leftContextSkt == null) {
			skv.add(label);
			if (rightContextSkt != null) {
				skv.add(rightContextSkt.sktLabel.get(0));
			}
		} else { // leftContextSkt != null
			skv.add(leftBoundSkt.sktLabel.get(0));
			if (leftBoundSkt.sktLabel.size() > 1)
				skv.add(leftBoundSkt.sktLabel.get(1));
			else
				skv.add(label);
		}
		Socket leftSkt = new Socket(skv);
		leftSktSet.add(leftSkt);

		// generate the new right bound socket (!!!Bigram Socket!!!)
		skv = new ArrayList<Label>();
		if (rightContextSkt == null) {
			if (leftContextSkt != null) {
				final List<Label> sktLabel = leftContextSkt.sktLabel;
				skv.add(sktLabel.get(sktLabel.size() - 1));
			}
			skv.add(label);
		} else { // rightContextSkt != null
			final List<Label> sktLabel = rightBoundSkt.sktLabel;
			if (sktLabel.size() > 1)
				skv.add(sktLabel.get(0));
			else
				skv.add(label);
			skv.add(sktLabel.get(sktLabel.size() - 1));
		}
		Socket rightSkt = new Socket(skv);
		rightSktSet.add(rightSkt);

		// link hypothesis to sockets
		List<Socket> pair = new ArrayList<Socket>(2);
		pair.add(leftSkt); // left
		pair.add(rightSkt); // right
		List<Hypothesis> hypos = skt2hypos.get(pair);
		if (hypos == null) {
			hypos = new ArrayList<Hypothesis>();
			skt2hypos.put(pair, hypos);
		}
		hypos.add(onehypo);

		// generate features
		// setInitCandFeat(onehypo, sen, lastPosi, label);
		genCandFeat(onehypo, sen, lastPosi, label, leftContextSkt,
				rightContextSkt, leftBoundSkt, rightBoundSkt );

		// compute score
		onehypo.setContextScore(leftContextScore + rightContextScore);
		onehypo.compLblTtlScores(tagger.features);

		// gold standard for training
		if (training) {
			if (contextgold && label == sen.tags[lastPosi]) {
				onehypo.isgold = true;
				onehypo.mistake = 0;
				goldHypo = onehypo;
			} else {
				onehypo.isgold = false;
				if (label == sen.tags[lastPosi]) {
					onehypo.mistake = contextmistake;
				} else {
					onehypo.mistake = contextmistake + 1;
				}
			}
		}
	}

	/**
	 * set the information of the hypothesis with highest lableScore
	 */
	public void setTopHypo()
	{

		double topOpScore = Double.NEGATIVE_INFINITY;

		for (int j = 0; j < leftBoundSocket.size(); j++) {
			for (int k = 0; k < rightBoundSocket.size(); k++) {
				Hypothesis ahypo = hypo[j][k][0];
				if (ahypo == null)
					continue;

				double itemopscore = ahypo.getHypoScoreMGN();

				if (itemopscore > topOpScore) {
					topOpScore = itemopscore;
					topOpHypo = ahypo;
					topLeftBoundSktID = j;
					topRightBoundSktID = k;
					topHypoID = 0;
				}
			}
		}

	}

	/**
	 * Keep K-Best Sockets and Hypothesis
	 */
	public void keepKBest(Set<Socket> leftSktSet, Set<Socket> rightSktSet,
			Map<List<Socket>, List<Hypothesis>> skt2hypos, boolean training)
	{

		// get to top KSOCKET socket pairs with the highest score
		List<List<Socket>> topKSktPair = new ArrayList<List<Socket>>(
				tagger.context.KSOCKET + 1);
		List<Double> topKScore = new ArrayList<Double>(
				tagger.context.KSOCKET + 1);

		Iterator<List<Socket>> pairs = skt2hypos.keySet().iterator();
		while (pairs.hasNext()) {
			List<Socket> pair = pairs.next();
			List<Hypothesis> hypos = skt2hypos.get(pair);

			// get the highest score of the hypos associated with this bound
			// socket pair
			double highest = hypos.get(0).getHypoScoreMGN();
			for (int i = 1; i < hypos.size(); i++) {
				double nextScore = hypos.get(i).getHypoScoreMGN();
				if (nextScore > highest)
					highest = nextScore;
			}

			// put it into the top KSOCKET (pair) array;
			boolean inserted = false;
			for (int i = 0; i < topKScore.size(); i++) {
				if (highest > topKScore.get(i)) {
					topKScore.add(i, highest);
					topKSktPair.add(i, pair);
					inserted = true;

					// remove if there are to many candidates
					int j = topKScore.size();
					if (j > tagger.context.KSOCKET) {
						topKScore.remove(--j);
						topKSktPair.remove(j);
					}

					break;
				}
			}

			if ((!inserted) && topKScore.size() < tagger.context.KSOCKET) {
				topKScore.add(highest);
				topKSktPair.add(pair);
			}

		}

		// extract left and right bound sockets w.r.t. topKSktPair
		leftBoundSocket = new ArrayList<Socket>();
		rightBoundSocket = new ArrayList<Socket>();
		for (int i = 0; i < topKSktPair.size(); i++) {
			final List<Socket> pair = topKSktPair.get(i);
			Socket leftskt = pair.get(0);
			Socket rightskt = pair.get(1);
			addSocket(leftskt, leftBoundSocket);
			addSocket(rightskt, rightBoundSocket);
		}

		// generate sockets:hypotheses table
		hypo = new Hypothesis[leftBoundSocket.size()][rightBoundSocket.size()][tagger.context.KHYPO];
		for (int i = 0; i < leftBoundSocket.size(); i++) {
			Socket leftSkt = leftBoundSocket.get(i);
			for (int j = 0; j < rightBoundSocket.size(); j++) {
				Socket rightSkt = rightBoundSocket.get(j);
				List<Socket> skv = new ArrayList<Socket>(2);
				skv.add(leftSkt);
				skv.add(rightSkt);
				List<Hypothesis> hypov = skt2hypos.get(skv);
				if (hypov != null) {
					topKHypo(hypo[i][j], hypov);
				}
			}
		}

	}

	/**
	 * add newskt to vec while avoiding duplicated items
	 */
	private void addSocket(Socket newskt, List<Socket> vec)
	{

		if (!vec.contains(newskt)) {
			vec.add(newskt);
		}
	}

	/**
	 * keep top K BHypothesis
	 */
	private void topKHypo(Hypothesis[] toparr, List<Hypothesis> hypov)
	{

		List<Hypothesis> topv = new ArrayList<Hypothesis>(tagger.context.KHYPO);
		for (int i = 0; i < hypov.size(); i++) {
			boolean added = false;
			double itemscore = hypov.get(i).getHypoScoreMGN();

			for (int j = 0; j < topv.size(); j++) {
				double inscore = topv.get(j).getHypoScoreMGN();
				if (itemscore > inscore) {
					topv.add(j, hypov.get(i));
					added = true;
					if (topv.size() > tagger.context.KHYPO) {
						topv.remove(topv.size() - 1);
					}
					break;
				}
			}
			if (!added && topv.size() < tagger.context.KHYPO) {
				topv.add(hypov.get(i));
			}
		}

		for (int i = 0; i < topv.size(); i++) {
			toparr[i] = topv.get(i);
		}
	}

	/**
	 * Generate the features for candidates
	 */
	private void genCandFeat(Hypothesis onehypo, TagSample sen, int posi,
			Label lbl, Socket leftContextSkt, Socket rightContextSkt ,Socket leftBoundSkt,Socket rigthBoundSkt)
	{
		if (tagger.context.BASEFEAT) {
			genAdwaitFeatures(onehypo, sen, posi, lbl, leftContextSkt,
					rightContextSkt);
		}
		if (tagger.context.ADDITIONALFEAT) {
			genAdditionalFeatures(onehypo, sen, posi, lbl, leftContextSkt,
					rightContextSkt);
		}
		if (tagger.context.GAZETTEERS) {
			genGazetteersFeatures(onehypo, sen, posi, lbl);
		}
		if(tagger.context.DOCUMENTFEAT){
			genDocumentLevelFeatures(onehypo, sen, posi, lbl);
		}
		if(tagger.context.BARRIERFEAT){
			genBarrierFeatures(onehypo, lbl, leftBoundSkt, rigthBoundSkt);
		}
	}

	/**
	 * Generate the features for candidates according to Table 1 of Adwait's
	 * paper, A Maximum Entropy Model for Part-Of-Speech Tagging. Extended to
	 * bidirectional mode. Words are normalized to the lower case.
	 */
	private void genAdwaitFeatures(Hypothesis onehypo, TagSample sen, int posi,
			Label lbl, Socket leftContextSkt, Socket rightContextSkt)
	{

		final String tag = lbl.toString();
		// String lex = "|W:"+sen.words[posi].word.toLowerCase();

		// onehypo.features.add(tag+lex); // # 1
		// lex features : # 2 - 6
		String wordraw = sen.words[posi];
		String wordstr = wordraw.toLowerCase();
		char[] chars = new char[Math.max(wordraw.length(), wordstr.length())];
		wordstr.getChars(0, wordstr.length(), chars, 0);

		//TODO remove and use from script (but will be slower 70s instead of 49)
		final StringBuilder prefix = new StringBuilder(tag.length() + 12)
		.append(tag).append("|P:");
		final StringBuilder suffix = new StringBuilder(tag.length() + 12)
		.append(tag).append("|S:");
		int featlength = 4;
		if (tagger.context.EXTENDFEAT) {
			featlength = 9;
		}
		for (int i = 0; i < featlength; i++) {
			int leftidx = i;
			if (leftidx < chars.length) {
				prefix.append((char) chars[leftidx]);
				onehypo.features.add(prefix.toString()); // # 2
			}

			int rightidx = chars.length - 1 - i;
			if (rightidx >= 0) {//TODO this test is useless, refer to previous if
				suffix.append((char) chars[rightidx]);
				onehypo.features.add(suffix.toString()); // # 3
			}
		}
		//

		boolean number = false;
		boolean upper = false;
		boolean allUpper = true;
		boolean hyphen = false;
		wordraw.getChars(0, wordstr.length(), chars, 0); // raw string
		for (int i = 0; i < chars.length; i++) {
			char ch = (char) chars[i];
			number |= Character.isDigit(ch);
			upper |= Character.isUpperCase(ch);
			allUpper &= Character.isUpperCase(ch);
			hyphen |= (ch == '-');
		}

		if (upper/*&&posi>0*/)
			onehypo.features.add(tag + "|UP"); // # 5
		if (allUpper/*&&posi>0*/)
			onehypo.features.add(tag + "|AU"); 
		if (number)
			onehypo.features.add(tag + "|NM"); // # 4
		if (hyphen)
			onehypo.features.add(tag + "|HF"); // # 6

		//L1 lex feat //TODO Implemet in more flexible way
		if(posi>0){

			String wordrawL1 =sen.words[posi-1];
			String wordstrL1 = wordrawL1.toLowerCase();
			char[] charsL1 = new char[Math.max(wordrawL1.length(), wordstrL1.length())];
			wordrawL1.getChars(0, wordrawL1.length(), charsL1, 0); // raw string
			boolean upperL1 = false;
			boolean allUpperL1 = true;
			//			boolean hyphenL1 = false;
			//			boolean numberL1 = false;

			for (int i = 0; i < charsL1.length; i++) {
				char ch = (char) charsL1[i];
				//				numberL1 |= Character.isDigit(ch);
				upperL1 |= Character.isUpperCase(ch);
				allUpperL1 &= Character.isUpperCase(ch);
				//				hyphenL1 |= (ch == '-');
			}

			if (upper&&upperL1)
				onehypo.features.add(tag + "|L1l:UP|UP"); 
			if (upper&&allUpperL1)
				onehypo.features.add(tag + "|L1l:AU|UP"); 
			if (upper&&!upperL1)
				onehypo.features.add(tag + "|L1l:NU|UP"); 

			if (allUpper&&upperL1)
				onehypo.features.add(tag + "|L1l:UP|AU"); 
			if (allUpper&&allUpperL1)
				onehypo.features.add(tag + "|L1l:AU|AU"); 
			if (allUpper&&!upperL1)
				onehypo.features.add(tag + "|L1l:NU|AU");

		}
		else{


			//if first word in the sentence, look at second word
			//R1 lex feat
			if(posi<sen.words.length-1){

				String wordrawR1 =sen.words[posi+1];
				String wordstrR1 = wordrawR1.toLowerCase();
				char[] charsR1 = new char[Math.max(wordrawR1.length(), wordstrR1.length())];
				wordrawR1.getChars(0, wordrawR1.length(), charsR1, 0); // raw string
				boolean upperR1 = false;
				boolean allUpperR1 = true;


				for (int i = 0; i < charsR1.length; i++) {
					char ch = (char) charsR1[i];
					upperR1 |= Character.isUpperCase(ch);
					allUpperR1 &= Character.isUpperCase(ch);
				}

				if (upper&&upperR1)
					onehypo.features.add(tag + "|L1l:@|R1l:UP|UP"); 
				if (upper&&allUpperR1)
					onehypo.features.add(tag + "|L1l:@|R1l:AU|UP"); 
				if (upper&&!upperR1)
					onehypo.features.add(tag + "|L1l:@|R1l:NU|UP"); 

				if (allUpper&&upperR1)
					onehypo.features.add(tag + "|L1l:@|R1l:UP|AU"); 
				if (allUpper&&allUpperR1)
					onehypo.features.add(tag + "|L1l:@|R1l:AU|AU"); 
				if (allUpper&&!upperR1)
					onehypo.features.add(tag + "|L1l:@|R1l:NU|AU");

			}else{
				if (upper)
					onehypo.features.add(tag + "|L1l:@|UP"); 
				if (allUpper)
					onehypo.features.add(tag + "|L1l:@|AU"); 
			}
			if (number)
				onehypo.features.add(tag + "|L1l:@|NM");
			if (hyphen)
				onehypo.features.add(tag + "|L1l:@|HF"); 
		}


	}

	/**
	 * Generate extra features
	 */
	@SuppressWarnings("unused")
	private void genExtraFeatures(Hypothesis onehypo, TagSample sen, int posi,
			Label lbl, Socket leftContextSkt, Socket rightContextSkt)
	{

		String tag = "" + lbl;
		String lex = "|W:" + sen.words[posi].toLowerCase();

		// collect the context tags and sentence boundaries
		List<String> leftcxt = new ArrayList<String>();
		if (leftContextSkt != null) {
			for (int i = 0; i < leftContextSkt.sktLabel.size(); i++) {
				leftcxt.add(leftContextSkt.sktLabel.get(i).lbl);
			}
			if (leftcxt.size() < tagger.context.NGRAM - 1
					&& posi - leftcxt.size() == 0) {
				leftcxt.add(0, "@");
			}
		} else {
			if (posi == 0) {
				leftcxt.add("@");
			}
		}
		List<String> rightcxt = new ArrayList<String>();
		if (rightContextSkt != null) {
			for (int i = 0; i < rightContextSkt.sktLabel.size(); i++) {
				rightcxt.add(rightContextSkt.sktLabel.get(i).lbl);
			}
			if (rightcxt.size() < tagger.context.NGRAM - 1
					&& posi + rightcxt.size() == sen.words.length - 1) {
				rightcxt.add("@");
			}
		} else {
			if (posi == sen.words.length - 1) {
				rightcxt.add("@");
			}
		}

		// [T-2, T-1, W0], [T-1, W0], [T-2], [T-2, W0]
		if (leftcxt.size() > 0) {

			String leftTags = tag;
			int idx = 0;
			for (int i = leftcxt.size() - 1; i >= 0; i--) {
				idx++;
				leftTags += "|L" + idx + ":" + leftcxt.get(i);
				onehypo.features.add(leftTags + lex); // [T-2, T-1, W0], [T-1,
				// W0]
				if (idx > 1) {
					onehypo.features.add(tag + "|L" + idx + ":"
							+ leftcxt.get(i)); // [T-2]
					onehypo.features.add(tag + "|L" + idx + ":"
							+ leftcxt.get(i) + lex); // [T-2, W0]
					// System.out.println(tag+"|L"+idx+":"+leftcxt.get(i)+lex);
				}
			}
		}

		// [T+1, T+2, W0], [T+1, W0], [T+2], [T+2, W0]
		if (rightcxt.size() > 0) {
			String rightTags = tag;
			int idx = 0;
			for (int i = 0; i < rightcxt.size(); i++) {
				idx++;
				rightTags += "|R" + idx + ":" + rightcxt.get(i);
				onehypo.features.add(rightTags + lex); // [T+1, T+2, W0], [T+1,
				// W0]
				if (idx > 1) {
					onehypo.features.add(tag + "|R" + idx + ":"
							+ rightcxt.get(i)); // [T+2]
					onehypo.features.add(tag + "|R" + idx + ":"
							+ rightcxt.get(i) + lex); // [T+2, W0]
				}
			}
		}

		// [T-1, T+1, W0], T-2to1, T-1to2, T-2to2
		if (leftcxt.size() > 0 && rightcxt.size() > 0) {

			String lrTags = "|L1:" + leftcxt.get(leftcxt.size() - 1) + "|R1:"
			+ rightcxt.get(0);
			onehypo.features.add(tag + lrTags + lex); // [T-1, T+1, W0]

			// Libin: 4-gram and 5-gram features do not help
			// if (leftcxt.size() > 1){ // T-2to1, and with W0
			// String ft = tag+"|L2:"+leftcxt.get(leftcxt.size()-2)
			// +"|L1:"+leftcxt.get(leftcxt.size()-1)
			// +"|R1:"+rightcxt.get(0);
			// onehypo.features.add(ft);
			// onehypo.features.add(ft+lex);
			// }
			// if (rightcxt.size() > 1){ // T-1to2, and with W0
			// String ft = tag+"|L1:"+leftcxt.get(leftcxt.size()-1)
			// +"|R1:"+rightcxt.get(0)
			// +"|R2:"+rightcxt.get(1);
			// onehypo.features.add(ft);
			// onehypo.features.add(ft+lex);
			// }
			// if (leftcxt.size() > 1 && rightcxt.size() > 1){ // T-2to2, and
			// with W0
			// String ft = tag+"|L2:"+leftcxt.get(leftcxt.size()-2)
			// +"|L1:"+leftcxt.get(leftcxt.size()-1)
			// +"|R1:"+rightcxt.get(0)
			// +"|R2:"+rightcxt.get(1);
			// onehypo.features.add(ft);
			// onehypo.features.add(ft+lex);
			// }
		}

		// [W-1, W0] [W0, W+1]
		String leftlex = "@";
		if (posi - 1 >= 0) {
			leftlex = sen.words[posi - 1].toLowerCase();
		}
		onehypo.features.add(tag + lex + "|L1W:" + leftlex); // [W-1, W0]
		String rightlex = "@";
		if (posi + 1 < sen.words.length) {
			rightlex = sen.words[posi + 1].toLowerCase();
		}
		onehypo.features.add(tag + lex + "|R1W:" + rightlex); // [W0, W+1]

	}

	/**
	 * generate features depending on the additional tags.
	 */
	private void genAdditionalFeatures(Hypothesis onehypo, TagSample sen,
			int posi, Label lbl, Socket leftContextSkt, Socket rightContextSkt)
	{
		for (Feature fds:tagger.context.addFeature) {
			if (/*tagger.context.SENTENCE_LIMIT_CHAR
					||*/ (((posi - fds.leftEdge) >= -1) && ((posi + fds.rightEdge) < sen.words.length + 1))) {
				genAdditionalFeature(onehypo, sen, posi, lbl, leftContextSkt,
						rightContextSkt, fds.details);
			}
		}
	}

	/**
	 * generate a single feature depending on the additional tags.
	 */
	private void genAdditionalFeature(Hypothesis onehypo, TagSample sen,
			int posi, Label lbl, Socket leftContextSkt, Socket rightContextSkt,
			FeatureDetail featDet[])
	{
		final StringBuilder lex = new StringBuilder(40).append(lbl.lbl);
		int posiOffset; //position + offset
		boolean completed=true; //false if the feature is not to be considered;
		String detailString;
		for (FeatureDetail fd:featDet) {
			completed=true;
			posiOffset=posi + fd.offset;

			// handle feature details
			lex.append(fd.positionAndTagName);

			// sentence border simbol
			if ((posiOffset >= sen.words.length)|| (posiOffset < 0)) {
				if(fd.featureProperty==null){
					lex.append("@");
				}
				else{
					completed=false;
					break;
				}
			}
			// word
			else if (fd.isWord) {
				// save word in lower case
				if(fd.featureProperty==null){
					lex.append(sen.words[posiOffset].toLowerCase());
				}
				else{
					detailString=fd.applyProperty(sen.words[posiOffset].toLowerCase());
					if(detailString==null){
						completed=false;
						break;
					}
					else{
						lex.append(detailString);
					}
				}
			}
			// main tag
			else if (fd.isMainTag) {
				if (fd.isOnLeft) {
					if (leftContextSkt != null
							&& leftContextSkt.sktLabel.size() >= fd.distance) {
						if(fd.featureProperty==null){
							lex.append(leftContextSkt.sktLabel.get(leftContextSkt.sktLabel.size()- fd.distance));
						}
						else{
							detailString=fd.applyProperty(leftContextSkt.sktLabel.get(leftContextSkt.sktLabel.size()- fd.distance).toString());
							if(detailString==null){
								completed=false;
								break;
							}
							else{
								lex.append(detailString);
							}
						}
					} else {
						// if the tag requested is not already assigned, skip this feature
						completed=false;
						break;
					}
				} else {
					// Must be to the right: [T!,0] is not allowed
					if (rightContextSkt != null
							&& rightContextSkt.sktLabel.size() >= (fd.distance)) {
						if(fd.featureProperty==null){
							lex.append(rightContextSkt.sktLabel.get(fd.distance - 1));
						}
						else{
							detailString=fd.applyProperty(rightContextSkt.sktLabel.get(fd.distance - 1).toString());
							if(detailString==null){
								completed=false;
								break;
							}
							else{
								lex.append(detailString);
							}
						}
					} else {
						// if the tag requested is not already assigned, skip this feature
						completed=false;
						break;
					}
				}
			}
			// additional tags
			else {

				if(fd.featureProperty==null){
					lex.append(fd.applyProperty(sen.addTags[fd.addFeatIndex][posiOffset]));
				}
				else{
					detailString=fd.applyProperty(sen.addTags[fd.addFeatIndex][posiOffset]);
					if(detailString==null){
						completed=false;
						break;
					}
					else{
						lex.append(detailString);
					}
				}
			}
		}
		if (completed) {
			onehypo.features.add(lex.toString());
		}
	}

	/**
	 * Generate features relative to the gazetteers.
	 * 
	 * @param sen
	 *            Current sentece.
	 * @param posi
	 *            Index of the word in consideration.
	 */
	private void genGazetteersFeatures(Hypothesis onehypo, TagSample sen,
			int posi, Label lbl)
	{
		final StringBuilder buf = new StringBuilder(lbl.lbl.length() + 5)
		.append(lbl.lbl).append("|g");
		final int prefixLen = buf.length();
		for (int i = 0; i < tagger.context.gazetteers.size(); i++) {
			if (tagger.context.gazetteers.get(i).isInTheGazetteers(sen, posi)) {
				buf.setLength(prefixLen);
				buf.append(i + 1);
				onehypo.features.add(buf.toString());
			}
		}
	}

	/**
	 * Generate document level features.
	 */
	private void genDocumentLevelFeatures(Hypothesis onehypo, TagSample sen,
			int posi, Label lbl)
	{
		for(int k=0;k<tagger.context.DOCUMENTCOLUMNS.size();k++){
			String colPrefix = tagger.context.DOCUMENTCOLUMNS.get(k);

			//			{//working on b but not on a?
			//				final StringBuilder buf = new StringBuilder(lbl.lbl.length() + 7)
			//				.append(lbl.lbl).append("|"+colPrefix+"-pldg");
			//				final int prefixLen = buf.length();
			//				Set<String> keySet = tagger.context.documentGazetteers.get(k).keySet();
			//				int [] maxLenAndPosition=null;
			//				for (String key : keySet) {
			//					maxLenAndPosition=tagger.context.documentGazetteers.get(k).getLblGazet(key).isInTheGazetteersGetPositionInLongest(sen.words[posi]);
			//					if (maxLenAndPosition!=null) {
			//						buf.setLength(prefixLen);
			//						buf.append(":"+maxLenAndPosition[1]+""+key+""+maxLenAndPosition[0]);
			//						onehypo.features.add(buf.toString());
			//					}
			//				}
			//			}
			{
				final StringBuilder buf = new StringBuilder(lbl.lbl.length() + 7)
				.append(lbl.lbl).append("|"+colPrefix+"-swdg");
				final int prefixLen = buf.length();
				Set<String> keySet = tagger.context.documentGazetteers.get(k).keySet();
				for (String key : keySet) {
					if (tagger.context.documentGazetteers.get(k).getLblGazet(key).isInTheGazetteersSingleWord(sen.words[posi])) {
						buf.setLength(prefixLen);
						buf.append(":"+key);
						if(tagger.context.documentGazetteers.get(k).getLblGazet(key).isInTheGazetteersSingleWordCount(sen.words[posi])>1){
							buf.append("1+");
							onehypo.features.add(buf.toString());
						}

					}
				}
			}
			{
				final StringBuilder buf = new StringBuilder(lbl.lbl.length() + 7)
				.append(lbl.lbl).append("|"+colPrefix+"-dg");
				final int prefixLen = buf.length();
				Set<String> keySet = tagger.context.documentGazetteers.get(k).keySet();
				for (String key : keySet) {
					if (tagger.context.documentGazetteers.get(k).getLblGazet(key).isInTheGazetteers(sen, posi)) {
						buf.setLength(prefixLen);
						buf.append(key);
						onehypo.features.add(buf.toString());
					}
				}
			}
		}
	}

	private void genBarrierFeatures(Hypothesis onehypo,Label lbl ,Socket leftBoundSkt, Socket rigthBoundSkt){

		if ( lastPosi<3){
			return;
		}

		Label[] hypLabels = new Label[sen.tags.length];
		hypLabels[lastPosi] = lbl;

		if (islandFromLeft != null) {
			int childRightBdSktID = onehypo.socketIDFromLeft;
			int childLeftBdSktID = getCompLeftSktID(
					islandFromLeft.leftBoundSocket, leftBoundSkt);
			islandFromLeft
			.retrieve(hypLabels, childLeftBdSktID, childRightBdSktID);
		}

		if (islandFromRight != null) {
			int childLeftBdSktID = onehypo.socketIDFromRight;
			int childRightBdSktID = getCompRightSktID(
					islandFromRight.rightBoundSocket, rigthBoundSkt);
			islandFromRight.retrieve(hypLabels, childLeftBdSktID,
					childRightBdSktID);
		}


		//				if (lastPosi-5>=0 && hypLabels[lastPosi-5]!=null ){
		//					onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-5]+"("+hypLabels[lastPosi-4]+","+hypLabels[lastPosi-3]+")");
		//					if (lastPosi-6>=0 && hypLabels[lastPosi-6]!=null ){
		//						onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-6]+"("+hypLabels[lastPosi-4]+","+hypLabels[lastPosi-3]+")");
		//						onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-6]+"("+hypLabels[lastPosi-5]+","+hypLabels[lastPosi-3]+")");
		//						onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-6]+"("+hypLabels[lastPosi-5]+","+hypLabels[lastPosi-4]+")");
		//						if (lastPosi-7>=0 && hypLabels[lastPosi-7]!=null ){
		//							onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-4]+","+hypLabels[lastPosi-3]+")");
		//							onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-5]+","+hypLabels[lastPosi-3]+")");
		//							onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-6]+","+hypLabels[lastPosi-3]+")");
		//							onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-5]+","+hypLabels[lastPosi-4]+")");
		//							onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-6]+","+hypLabels[lastPosi-4]+")");
		//							onehypo.features.add(lbl+"|BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-6]+","+hypLabels[lastPosi-5]+")");
		//						}
		//					}
		//				}

		//		if (lastPosi-5>=0 && hypLabels[lastPosi-5]!=null ){
		//		onehypo.features.add(lbl+"|L5BF:"+hypLabels[lastPosi-5]+"("+hypLabels[lastPosi-4]+","+hypLabels[lastPosi-3]+")");
		//		if (lastPosi-6>=0 && hypLabels[lastPosi-6]!=null ){
		//			onehypo.features.add(lbl+"|L6BF:"+hypLabels[lastPosi-6]+"("+hypLabels[lastPosi-4]+","+hypLabels[lastPosi-3]+")");
		//			onehypo.features.add(lbl+"|L6BF:"+hypLabels[lastPosi-6]+"("+hypLabels[lastPosi-5]+","+hypLabels[lastPosi-3]+")");
		//			onehypo.features.add(lbl+"|L6BF:"+hypLabels[lastPosi-6]+"("+hypLabels[lastPosi-5]+","+hypLabels[lastPosi-4]+")");
		//			if (lastPosi-7>=0 && hypLabels[lastPosi-7]!=null ){
		//				onehypo.features.add(lbl+"|L7BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-4]+","+hypLabels[lastPosi-3]+")");
		//				onehypo.features.add(lbl+"|L7BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-5]+","+hypLabels[lastPosi-3]+")");
		//				onehypo.features.add(lbl+"|L7BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-6]+","+hypLabels[lastPosi-3]+")");
		//				onehypo.features.add(lbl+"|L7BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-5]+","+hypLabels[lastPosi-4]+")");
		//				onehypo.features.add(lbl+"|L7BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-6]+","+hypLabels[lastPosi-4]+")");
		//				onehypo.features.add(lbl+"|L7BF:"+hypLabels[lastPosi-7]+"("+hypLabels[lastPosi-6]+","+hypLabels[lastPosi-5]+")");
		//			}
		//		}
		//	}

		//		//skip window size positions
		//
		//		if(lastPosi>=3 && hypLabels[lastPosi-1]!=null && hypLabels[lastPosi-2]!=null && hypLabels[lastPosi-3]!=null){
		//
		//			//starting barrier from first position after the window
		//			int barrierPosi = lastPosi-3;
		//
		//			//inializing sorted list  with two labels in the window
		//			SortedList sortedList = new SortedList();
		//			sortedList.add(hypLabels[lastPosi-1].lbl);
		//			sortedList.add(hypLabels[lastPosi-2].lbl);
		//
		//			//moving further the position of the barrier untill the size of the sorted reache the maximum allowed
		//			for(;;barrierPosi-- ){
		//				if (barrierPosi>=0 && hypLabels[barrierPosi]!=null ){
		//
		//					//add feature in the set
		//					onehypo.features.add(lbl+"|BF:"+hypLabels[barrierPosi].lbl+""+sortedList.toString());
		//
		//					sortedList.add(hypLabels[barrierPosi].lbl);
		//					if(sortedList.size()>7){
		//						break;
		//					}
		//
		//				}
		//				else{
		//					break;
		//				}
		//			}
		//		}

				//skip window size positions
				//BEST (less worst then the other) on POS tagging didn't show improvements
				if(lastPosi>=2 && hypLabels[lastPosi-1]!=null && hypLabels[lastPosi-2]!=null ){
		
					//starting barrier from first position after the window
					int barrierPosi = lastPosi-2;
		
					//inializing sorted list  with two labels in the window
					SortedList sortedList = new SortedList();
					sortedList.add(hypLabels[lastPosi-1].lbl);
		
					//moving further the position of the barrier untill the size of the sorted reache the maximum allowed
					for(;;barrierPosi-- ){
						if (barrierPosi>=0 && hypLabels[barrierPosi]!=null ){
		
							//add feature in the set
							onehypo.features.add(lbl+"|BF:"+hypLabels[barrierPosi].lbl+""+sortedList.toString());
		
							sortedList.add(hypLabels[barrierPosi].lbl);
							if(sortedList.size()>7){
								break;
							}
		
						}
						else{
							break;
						}
					}
				}


//		//use only prefix of length 2 for BF
//
//		//skip window size positions
//		if(lastPosi>=2 && hypLabels[lastPosi-1]!=null && hypLabels[lastPosi-2]!=null ){
//
//
//			//starting barrier from first position after the window
//			int barrierPosi = lastPosi-2;
//
//			//inializing sorted list  with two labels in the window
//			SortedList sortedList = new SortedList();
//			if(hypLabels[lastPosi-1].lbl.length()<3){
//				sortedList.add(hypLabels[lastPosi-1].lbl);
//			}
//			else{
//				sortedList.add(hypLabels[lastPosi-1].lbl.substring(0, 2));
//			}
//			
//			//moving further the position of the barrier untill the size of the sorted reache the maximum allowed
//			for(;;barrierPosi-- ){
//				if (barrierPosi>=0 && hypLabels[barrierPosi]!=null ){
//
//					//add feature in the set
//					if(hypLabels[barrierPosi].lbl.length()<3){
//						onehypo.features.add(lbl+"|BF:"+hypLabels[barrierPosi].lbl+""+sortedList.toString());
//						sortedList.add(hypLabels[barrierPosi].lbl);
//					}
//					else{
//						onehypo.features.add(lbl+"|BF:"+hypLabels[barrierPosi].lbl.substring(0, 2)+""+sortedList.toString());
//						sortedList.add(hypLabels[barrierPosi].lbl.substring(0, 2));
//					}
//
//					
//					
//					if(sortedList.size()>7){
//						break;
//					}
//
//				}
//				else{
//					break;
//				}
//			}
//		}


//				if(lastPosi>=2 && hypLabels[lastPosi-1]!=null && hypLabels[lastPosi-2]!=null ){
//		
//					//starting barrier from first position after the window
//					int barrierPosi = lastPosi-2;
//		
//					//inializing sorted list  with two labels in the window
//					SortedList sortedList = new SortedList();
//					sortedList.add(hypLabels[lastPosi-1].lbl);
//		
//					//moving further the position of the barrier untill the size of the sorted reache the maximum allowed
//					for(;;barrierPosi-- ){
//						if (barrierPosi>=0 && hypLabels[barrierPosi]!=null ){
//		
//							//add feature in the set
//							onehypo.features.add(lbl+"|BF:"+hypLabels[barrierPosi].lbl+""+sortedList.toString());
//		
//							sortedList.add(hypLabels[barrierPosi].lbl);
//							if(sortedList.size()>3){
//								break;
//							}
//		
//						}
//						else{
//							break;
//						}
//					}
//				}
		
//		//skip window size positions
//		if(lastPosi>=2 && hypLabels[lastPosi-1]!=null && hypLabels[lastPosi-2]!=null ){
//
//			//starting barrier from first position after the window
//			int barrierPosi = lastPosi-2;
//			int i=1;
//			//inializing sorted list  with two labels in the window
////			SortedList sortedList = new SortedList();
////			sortedList.add(hypLabels[lastPosi-1].lbl);
//
//			//moving further the position of the barrier untill the size of the sorted reache the maximum allowed
//			for(;;barrierPosi-- ){
//				i++;
//				if (barrierPosi>lastPosi-6 && barrierPosi>=0 && hypLabels[barrierPosi]!=null ){
//
//					//add feature in the set
//					onehypo.features.add(lbl+"|BFL"+i+":"+hypLabels[barrierPosi].lbl/*+""+sortedList.toString()*/);
//
////					sortedList.add(hypLabels[barrierPosi].lbl);
////					if(sortedList.size()>7){
////						break;
////					}
//
//				}
//				else{
//					break;
//				}
//			}
//		}

		
	}

	private class SortedList{ //sorted List with no doubles
		List<String> sortedList = new ArrayList <String>();

		void add(String element){
			// Search for the non-existent item
			int index = Collections.binarySearch(sortedList, element);      

			// Add the non-existent item to the list
			if (index < 0) {
				sortedList.add(-index-1, element);
			}
		}

		int size(){
			return sortedList.size();
		}

		public String toString(){
			StringBuffer rtn = new StringBuffer("(");
			for (String element : sortedList){
				rtn.append(element + ",");
			}
			rtn.deleteCharAt(rtn.length()-1);
			rtn.append(")");
			return rtn.toString();
		}

	}

	/**
	 * Display the lebeling results
	 */
	public void display(Appendable sb) throws IOException
	{

		TagSample tagspl = new TagSample(sen.words);
		retrieve(tagspl.tags, topLeftBoundSktID, topRightBoundSktID);

		tagspl.display(sb);
	}

	/**
	 * retrieve the tags for each word recursively
	 */
	public void retrieve(Label[] tags, int leftBdSktID, int rightBdSktID)
	{

		// TODO: research the order of tagging here

		// DEBUG
		// System.err.println(this);
		// System.err.println(leftBoundSocket.get(leftBdSktID)+" "+rightBoundSocket.get(rightBdSktID));

		Hypothesis globalTopHypo = hypo[leftBdSktID][rightBdSktID][0];
		tags[lastPosi] = globalTopHypo.lastLabel;

		if (islandFromLeft != null) {
			int childRightBdSktID = globalTopHypo.socketIDFromLeft;
			int childLeftBdSktID = getCompLeftSktID(
					islandFromLeft.leftBoundSocket, leftBoundSocket
					.get(leftBdSktID));
			islandFromLeft
			.retrieve(tags, childLeftBdSktID, childRightBdSktID);
		}

		if (islandFromRight != null) {
			int childLeftBdSktID = globalTopHypo.socketIDFromRight;
			int childRightBdSktID = getCompRightSktID(
					islandFromRight.rightBoundSocket, rightBoundSocket
					.get(rightBdSktID));
			islandFromRight.retrieve(tags, childLeftBdSktID,
					childRightBdSktID);
		}

	}

	/**
	 * get the ID of the BSocket in childLeftSkts which is compatible with
	 * parentSkt
	 */
	private int getCompLeftSktID(List<Socket> childLeftSkts,
			Socket parentLeftSkt)
	{

		// System.err.println("Parent : "+parentLeftSkt);

		for (int i = 0; i < childLeftSkts.size(); i++) {
			Socket childSkt = childLeftSkts.get(i);

			// System.err.println(" Left Child "+i+" : "+childSkt);

			boolean match = true;
			for (int j = 0; j < childSkt.sktLabel.size(); j++) {
				if (childSkt.sktLabel.get(j) != parentLeftSkt.sktLabel.get(j)) {
					match = false;
					break;
				}
			}
			if (match)
				return i;
		}

		System.err.println("LEFT LINK BROKEN at " + lastPosi);
		return -1;
	}

	/**
	 * get the ID of the BSocket in childRightSkts which is compatible with
	 * parentSkt
	 */
	private int getCompRightSktID(List<Socket> childRightSkts,
			Socket parentRightSkt)
	{

		// System.err.println("Parent : "+parentRightSkt);

		for (int i = 0; i < childRightSkts.size(); i++) {
			Socket childSkt = childRightSkts.get(i);

			// System.err.println(" Right Child "+i+" : "+childSkt);

			boolean match = true;
			for (int j = 0; j < childSkt.sktLabel.size(); j++) {
				if (childSkt.sktLabel.get(childSkt.sktLabel.size() - 1 - j) != parentRightSkt.sktLabel
						.get(parentRightSkt.sktLabel.size() - 1 - j)) {
					match = false;
					break;
				}
			}
			if (match)
				return i;
		}

		System.err.println("RIGHT LINK BROKEN at " + lastPosi);
		return -1;
	}

	/**
	 * Generate extra features [T-2, T-1, W0], [T-1, W0], [T+1, T+2, W0], [T+1,
	 * W0], [T-1, T+1, W0] [W-1, W0], [W0, W1]
	 */
	// private void genExtraFeat_old(BHypothesis onehypo, BLinTagSample sen, int
	// posi, SLabel lbl,
	// BSocket leftContextSkt, BSocket rightContextSkt){
	// String tag = ""+lbl;
	// String lex = "|W:"+sen.words[posi].word.toLowerCase();
	// // [T-2, T-1, W0], [T-1, W0]
	// List<SLabel> leftcxt = null;
	// if (leftContextSkt != null){
	// leftcxt = leftContextSkt.sktLabel;
	// String leftTags = tag;
	// int idx = 0;
	// for (int i=leftcxt.size()-1; i>=0; i--){
	// idx++;
	// leftTags += "|L"+ idx + ":" + leftcxt.get(i);
	// onehypo.features.add(leftTags+lex);
	// }
	// }
	// // [T+1, T+2, W0], [T+1, W0]
	// List<SLabel> rightcxt = null;
	// if (rightContextSkt != null){
	// rightcxt = rightContextSkt.sktLabel;
	// String rightTags = tag;
	// int idx = 0;
	// for (int i=0; i<rightcxt.size(); i++){
	// idx++;
	// rightTags += "|R" +idx + ":" + rightcxt.get(i);
	// onehypo.features.add(rightTags+lex);
	// }
	// }
	// // [T-1, T+1, W0]
	// if (leftcxt != null && rightcxt != null){
	// String lrTags = "|L1:"+leftcxt.lastElement()+"|R1:"+rightcxt.get(0);
	// onehypo.features.add(tag+lrTags+lex);
	// }
	// // [Wi, Wi+1], i=-1 to 0
	// String leftlex = "@";
	// if (posi-1 >=0 ){
	// leftlex = sen.words[posi-1].word.toLowerCase();
	// }
	// onehypo.features.add(tag+lex+"|L1W:"+leftlex);
	// String rightlex = "@";
	// if (posi+1 < sen.words.length){
	// rightlex = sen.words[posi+1].word.toLowerCase();
	// }
	// onehypo.features.add(tag+lex+"|R1W:"+rightlex);
	// }
}
