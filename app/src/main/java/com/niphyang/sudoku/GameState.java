package com.niphyang.sudoku;

import android.content.ContentValues;

public class GameState {
    private int status;
    private int difficulty;
    private int timeElapsed;
    private int[][] solution = new int[9][9];
    private int[][] grid = new int[9][9];
    private int hintCnt;

    public GameState (int status, int difficulty, int timeElapsed, int[][] solution, int[][] grid, int hintCnt) {
        this.status = status;
        this.difficulty = difficulty;
        this.timeElapsed = timeElapsed;
        this.hintCnt = hintCnt;
        for(int row = 0; row < 9; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.solution[row][col] = solution[row][col];
                this.grid[row][col] = grid[row][col];
            }
        }
    }

    public ContentValues getContentValues () {
        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("difficulty", difficulty);
        values.put("elapsedSeconds",timeElapsed);
        values.put("solutionString", getString(solution));
        values.put("gridString", getString(grid));
        values.put("hintCnt", hintCnt);
        return values;
    }
    public String getString(int[][] arr) {
        StringBuilder builder = new StringBuilder();
        for(int row = 0; row < 9; ++row) {
            for(int col = 0; col < 9; ++col) {
                builder.append(arr[row][col]).append(" ");
            }
        }
        return builder.toString();
    }
}
