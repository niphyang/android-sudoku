package com.niphyang.sudoku.viewmodel

import android.arch.lifecycle.ViewModel
import com.niphyang.sudoku.game.SudokuGame

class PlaySudokuViewModel : ViewModel() {
    val sudokuGame = SudokuGame()
}