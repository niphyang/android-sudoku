package com.niphyang.sudoku;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Button;

public class DifficultyMenuActivity extends AppCompatActivity {
    private  int selectedDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        /* get screen size */
        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        int width = screenSize.x / 2;

        Typeface defaultFont = Typeface.createFromAsset(getAssets(), getString(R.string.app_font));

        /* resize buttons */
        Button btnEasy = findViewById(R.id.btn_easy);
        btnEasy.setTypeface(defaultFont);
        btnEasy.setWidth(width);

        Button btnNormal = findViewById(R.id.btn_normal);
        btnNormal.setTypeface(defaultFont);
        btnNormal.setWidth(width);

        Button btnHard = findViewById(R.id.btn_hard);
        btnHard.setTypeface(defaultFont);
        btnHard.setWidth(width);

        Button btnExtreme = findViewById(R.id.btn_extreme);
        btnExtreme.setTypeface(defaultFont);
        btnExtreme.setWidth(width);

        /* set fullscreen */
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.btn_easy: {
                selectedDifficulty = 1;
                break;
            }
            case R.id.btn_normal: {
                selectedDifficulty = 2;
                break;
            }
            case R.id.btn_hard: {
                selectedDifficulty = 3;
                break;
            }
            case R.id.btn_extreme: {
                selectedDifficulty = 4;
                break;
            }
            default: {
                selectedDifficulty = 0;
            }
        }

        final Intent intentContinue = new Intent(this, GameActivity.class);
        intentContinue.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        String status, difficulty, elapsedSeconds, solutionString, gridString;
        Cursor cursor;
        try {
            DatabaseHelper DBHelper = DatabaseHelper.newInstance(this);
            SQLiteDatabase database = DBHelper.getWritableDatabase();
            cursor = database.rawQuery("SELECT * FROM GameState WHERE difficulty = " + selectedDifficulty+ " ORDER BY lastPlaying DESC LIMIT 1", null);
            if(cursor.getCount() > 0) {

                cursor.moveToFirst();

                status = cursor.getString(cursor.getColumnIndex("status"));
                difficulty = cursor.getString(cursor.getColumnIndex("difficulty"));
                elapsedSeconds = cursor.getString(cursor.getColumnIndex("elapsedSeconds"));
                solutionString = cursor.getString(cursor.getColumnIndex("solutionString"));
                gridString = cursor.getString(cursor.getColumnIndex("gridString"));

                intentContinue.putExtra("status", Integer.parseInt(status));
                intentContinue.putExtra("difficulty", Integer.parseInt(difficulty));
                intentContinue.putExtra("elapsedSeconds", Integer.parseInt(elapsedSeconds));
                intentContinue.putExtra("solutionString", solutionString);
                intentContinue.putExtra("gridString", gridString);


                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("진행중인 게임이 있습니다.\n이어서 진행하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                startActivity(intentContinue);
                                finish();
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                View progress = findViewById(R.id.base_progressBar);
                                progress.setVisibility(View.VISIBLE);


                                Intent intent = new Intent(DifficultyMenuActivity.this, GameActivity.class);
                                intent.putExtra("difficulty", selectedDifficulty);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                DifficultyMenuActivity.this.startActivity(intent);
                                finish();
                            }
                        })
                        .show();
                // remove old data
                database.execSQL("DELETE FROM GameState WHERE difficulty = '" + selectedDifficulty + "';");

            }else{


                View progress = findViewById(R.id.base_progressBar);
                progress.setVisibility(View.VISIBLE);

                Intent intent = new Intent(DifficultyMenuActivity.this, GameActivity.class);
                intent.putExtra("difficulty", selectedDifficulty);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                DifficultyMenuActivity.this.startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }







    }
}
