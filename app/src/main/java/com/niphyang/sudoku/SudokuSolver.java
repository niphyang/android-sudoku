package com.niphyang.sudoku;

import com.niphyang.utils.QQWingMain;
import com.qqwing.QQWing;

import java.util.Random;

/**
 * Created by tuana on 10-03-2018.
 */

public class SudokuSolver {
    private int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private Random rnd = new Random();
    private int[][] grid;
    private boolean[][] rows, cols, boxes;
    private boolean genFlag;

    public SudokuSolver() {
        grid = new int[9][9];
        rows = new boolean[9][10];
        cols = new boolean[9][10];
        boxes = new boolean[9][10];
        randomValuesArray(10);
    }

    private void clearGrid() {
        for (int r = 0; r < 9; ++r) {
            for (int c = 0; c < 9; ++c) {
                grid[r][c] = 0;
            }
        }
        for (int i = 0; i < 9; ++i) {
            for (int v = 1; v <= 9; ++v) {
                rows[i][v] = false;
                cols[i][v] = false;
                boxes[i][v] = false;
            }
        }
    }

    private void randomValuesArray(int numberOfTurns) {
        for (int turn = 0; turn < numberOfTurns; ++turn) {
            int randomIndex = 1 + rnd.nextInt(8);

            int tmp = values[0];
            values[0] = values[randomIndex];
            values[randomIndex] = tmp;
        }
    }

    private void generate(int pos) {
        if (pos == 81) {
            genFlag = false;
        } else if (genFlag) {
            int row = pos / 9;
            int col = pos - row * 9;
            int box = (row / 3) * 3 + col / 3;
            randomValuesArray(5);
            for (int val : values) {
                if (genFlag && rows[row][val] == false && cols[col][val] == false && boxes[box][val] == false) {
                    grid[row][col] = val;
                   // rows[row][val] = true;
                  //  cols[col][val] = true;
                   // boxes[box][val] = true;
                    generate(pos + 1);
                   // rows[row][val] = false;
                   // cols[col][val] = false;
                  //  boxes[box][val] = false;
                }
            }
        }
    }

    public int[][] getRandomGrid(String difficultyString) {
        clearGrid();
        genFlag = true;

        QQWing ss = null;
        while(ss == null || ss.countSolutions() != 1){
            // Difficulty expected to be simple, easy, intermediate, expert, or any
            ss = QQWingMain.generateSudoku(difficultyString);
        }

        String puzzleString = ss.getPuzzleString().replaceAll("\\.","0");
        ss.solve();
        String puzzleSolutionString = ss.getSolutionString().replaceAll("\\.","0");



        for(int i=0;i<puzzleSolutionString.length()-1;i++){

            int row = i / 9;
            int col = i - row * 9;

            grid[row][col] = Integer.parseInt(puzzleSolutionString.substring(i,i+1));

            if(Integer.parseInt(puzzleString.substring(i,i+1)) == 0){
                /* the 10-th bit mark the cell is empty*/
                grid[row][col] |= 1024;
            }
        }




        return grid;
    }


    public boolean checkValidGrid(int[][] grid) {
        boolean[][] rowSet = new boolean[9][10];
        boolean[][] colSet = new boolean[9][10];
        boolean[][] boxSet = new boolean[9][10];

        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                int value = grid[row][col];
                if (value == 0) return false;
                int box = (row / 3) * 3 + col / 3;
                if (rowSet[row][value] || colSet[col][value] || boxSet[box][value]) return false;
                rowSet[row][value] = true;
                colSet[col][value] = true;
                boxSet[box][value] = true;
            }
        }

        return true;
    }

}
