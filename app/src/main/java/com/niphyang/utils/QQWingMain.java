package com.niphyang.utils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.qqwing.Action;
import com.qqwing.Difficulty;
import com.qqwing.PrintStyle;
import com.qqwing.QQWing;
import com.qqwing.Symmetry;

public class QQWingMain {

	private static final String NL = "\n";

	public static QQWing generateSudoku(String diff) {

		final QQWingOptions opts = new QQWingOptions();

		opts.printPuzzle = true;
		opts.printSolution = true;
		opts.printHistory = false;
		opts.printInstructions = false;
		opts.printStats = false;
		opts.timer = false;
		opts.countSolutions = true;
		opts.action = Action.GENERATE;
		opts.printPuzzle = true;
		opts.numberToGenerate = 1;
		// Difficulty expected to be simple, easy, intermediate, expert, or any
		opts.difficulty = Difficulty.get(diff);
		opts.logHistory = false;
		opts.printStyle = PrintStyle.CSV;


		// The number of puzzles solved or generated.
		final AtomicInteger puzzleCount = new AtomicInteger(0);
		final AtomicBoolean done = new AtomicBoolean(false);

		QQWing ss = new QQWing();
		ss.setRecordHistory(opts.printHistory || opts.printInstructions || opts.printStats
				|| opts.difficulty != Difficulty.UNKNOWN);
		ss.setLogHistory(opts.logHistory);
		ss.setPrintStyle(opts.printStyle);

		try {

			// Solve puzzle or generate puzzles
			// until end of input for solving, or
			// until we have generated the specified number.
			while (!done.get()) {


				// iff something has been printed for this
				// particular puzzle
				StringBuilder output = new StringBuilder();

				// Record whether the puzzle was possible or
				// not,
				// so that we don't try to solve impossible
				// givens.
				boolean havePuzzle = false;

				if (opts.action == Action.GENERATE) {
					// Generate a puzzle
					havePuzzle = ss.generatePuzzleSymmetry(opts.symmetry);

					if (!havePuzzle && opts.printPuzzle) {
						output.append("Could not generate puzzle.");
						if (opts.printStyle == PrintStyle.CSV) {
							output.append(",").append(NL);
						} else {
							output.append(NL);
						}
					}
				} else {
					// Read the next puzzle on STDIN
					int[] puzzle = new int[QQWing.BOARD_SIZE];
					if (readPuzzleFromStdIn(puzzle)) {
						havePuzzle = ss.setPuzzle(puzzle);
						if (havePuzzle) {
							puzzleCount.getAndDecrement();
						} else {
							if (opts.printPuzzle) {
								output.append(ss.getPuzzleString());
							}
							if (opts.printSolution) {
								output.append("Puzzle is not possible.");
								if (opts.printStyle == PrintStyle.CSV) {
									output.append(",");
								} else {
									output.append(NL);
								}
							}
						}
					} else {
						// Set loop to terminate when nothing is
						// left on STDIN
						havePuzzle = false;
						done.set(true);
					}
					puzzle = null;
				}

				int solutions = 0;

				if (havePuzzle) {

					// Count the solutions if requested.
					// (Must be done before solving, as it would
					// mess up the stats.)
					if (opts.countSolutions) {
						solutions = ss.countSolutions();
					}

					// Solve the puzzle
					if (opts.printSolution || opts.printHistory || opts.printStats || opts.printInstructions
							|| opts.difficulty != Difficulty.UNKNOWN) {
						ss.solve();
					}

					// Bail out if it didn't meet the difficulty
					// standards for generation
					if (opts.action == Action.GENERATE) {
						if (opts.difficulty != Difficulty.UNKNOWN && opts.difficulty != ss.getDifficulty()) {
							havePuzzle = false;
							// check if other threads have
							// finished the job
							if (puzzleCount.get() >= opts.numberToGenerate)
								done.set(true);
						} else {
							int numDone = puzzleCount.incrementAndGet();
							if (numDone >= opts.numberToGenerate)
								done.set(true);
							if (numDone > opts.numberToGenerate)
								havePuzzle = false;
						}
					}
				}

				// Check havePuzzle again, it may have changed
				// based on difficulty
				if (havePuzzle) {
					// With a puzzle now in hand and possibly
					// solved
					// print out the solution, stats, etc.
					// Record the end time for the timer.

					// Print the puzzle itself.
					if (opts.printPuzzle)
						output.append(ss.getPuzzleString());

					// Print the solution if there is one
					if (opts.printSolution) {
						if (ss.isSolved()) {
							output.append(ss.getSolutionString());
						} else {
							output.append("Puzzle has no solution.");
							if (opts.printStyle == PrintStyle.CSV) {
								output.append(",");
							} else {
								output.append(NL);
							}
						}
					}

					// Print the steps taken to solve or attempt
					// to solve the puzzle.
					if (opts.printHistory)
						output.append(ss.getSolveHistoryString());
					// Print the instructions for solving the
					// puzzle
					if (opts.printInstructions)
						output.append(ss.getSolveInstructionsString());

					// Print the number of solutions to the
					// puzzle.
					if (opts.countSolutions) {
						if (opts.printStyle == PrintStyle.CSV) {
							output.append(solutions + ",");
						} else {
							if (solutions == 0) {
								output.append("There are no solutions to the puzzle.").append(NL);
							} else if (solutions == 1) {
								output.append("The solution to the puzzle is unique.").append(NL);
							} else {
								output.append("There are " + solutions + " solutions to the puzzle.").append(NL);
							}
						}
					}


					// Print any stats we were able to gather
					// while solving the puzzle.
					if (opts.printStats) {
						int givenCount = ss.getGivenCount();
						int singleCount = ss.getSingleCount();
						int hiddenSingleCount = ss.getHiddenSingleCount();
						int nakedPairCount = ss.getNakedPairCount();
						int hiddenPairCount = ss.getHiddenPairCount();
						int pointingPairTripleCount = ss.getPointingPairTripleCount();
						int boxReductionCount = ss.getBoxLineReductionCount();
						int guessCount = ss.getGuessCount();
						int backtrackCount = ss.getBacktrackCount();
						String difficultyString = ss.getDifficultyAsString();
						if (opts.printStyle == PrintStyle.CSV) {
							output.append(givenCount).append(",").append(singleCount).append(",")
									.append(hiddenSingleCount).append(",").append(nakedPairCount).append(",")
									.append(hiddenPairCount).append(",").append(pointingPairTripleCount).append(",")
									.append(boxReductionCount).append(",").append(guessCount).append(",")
									.append(backtrackCount).append(",").append(difficultyString).append(",");
						} else {
							output.append("Number of Givens: ").append(givenCount).append(NL);
							output.append("Number of Singles: ").append(singleCount).append(NL);
							output.append("Number of Hidden Singles: ").append(hiddenSingleCount).append(NL);
							output.append("Number of Naked Pairs: ").append(nakedPairCount).append(NL);
							output.append("Number of Hidden Pairs: ").append(hiddenPairCount).append(NL);
							output.append("Number of Pointing Pairs/Triples: ").append(pointingPairTripleCount)
									.append(NL);
							output.append("Number of Box/Line Intersections: ").append(boxReductionCount).append(NL);
							output.append("Number of Guesses: ").append(guessCount).append(NL);
							output.append("Number of Backtracks: ").append(backtrackCount).append(NL);
							output.append("Difficulty: ").append(difficultyString).append(NL);
						}
					}
				}
				if (output.length() > 0) {
					

					output.append(ss.getDifficultyAsString()).append(",");
					
				
					
					
					return ss;
					//return output.toString();
					
					

				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
		return null;

	
	}

	private static boolean isAlive(Thread[] threads) {
		for (int i = 0; i < threads.length; i++) {
			if (threads[i].isAlive())
				return true;
		}
		return false;
	}

	private static void printVersion() {
		System.out.println("qqwing " + QQWing.QQWING_VERSION);
	}

	private static class QQWingOptions {
		// defaults for options
		boolean printPuzzle = true;

		boolean printSolution = true;

		boolean printHistory = false;

		boolean printInstructions = false;

		boolean timer = false;

		boolean countSolutions = true;

		Action action = Action.GENERATE;

		boolean logHistory = false;

		PrintStyle printStyle = PrintStyle.CSV;

		int numberToGenerate = 2;

		boolean printStats = true;

		Difficulty difficulty = Difficulty.SIMPLE;

		Symmetry symmetry = Symmetry.NONE;

	}


	/**
	 * Read a sudoku puzzle from standard input. STDIN is processed one character at
	 * a time until the sudoku is filled in. Any digit or period is used to fill the
	 * sudoku, any other character is ignored.
	 */
	private static boolean readPuzzleFromStdIn(int[] puzzle) throws IOException {
		synchronized (System.in) {
			int read = 0;
			while (read < QQWing.BOARD_SIZE) {
				int c = System.in.read();
				if (c < 0)
					return false;
				if (c >= '1' && c <= '9') {
					puzzle[read] = c - '0';
					read++;
				}
				if (c == '.' || c == '0') {
					puzzle[read] = 0;
					read++;
				}
			}
			return true;
		}
	}



}