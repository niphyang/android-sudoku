package com.niphyang.sudoku;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class LadderboardActivity extends Activity {
    private int difficulty = 1;
    private int one; // a width unit
    private TextView txtEasy, txtNormal, txtHard, txtExtreme;
    private TableLayout table;
    Typeface appFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ladderboard);


        difficulty = getIntent().getIntExtra("difficulty", 1);
        // hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // reference variables
        one = AppConstant.SCREEN_SIZE.x / 4;
        table = findViewById(R.id.table_main);
        appFont = AppConstant.APP_FONT;

        txtEasy = findViewById(R.id.txtEasy);
        txtNormal = findViewById(R.id.txtNormal);
        txtHard = findViewById(R.id.txtHard);
        txtExtreme = findViewById(R.id.txtExtreme);

        adjustDifficultyTextView();
    }

    void adjustDifficultyTextView() {
        txtEasy.setWidth(one);
        txtEasy.setTypeface(appFont);
        txtEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                difficulty = 1;
                drawLadderboard();
            }
        });


        txtNormal.setWidth(one);
        txtNormal.setTypeface(appFont);
        txtNormal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                difficulty = 2;
                drawLadderboard();
            }
        });


        txtHard.setWidth(one);
        txtHard.setTypeface(appFont);
        txtHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                difficulty = 3;
                drawLadderboard();
            }
        });


        txtExtreme.setWidth(one);
        txtExtreme.setTypeface(appFont);
        txtExtreme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                difficulty = 4;
                drawLadderboard();
            }
        });


        drawLadderboard();


    }

    void drawLadderboard() {


        TextView [] txtViewArr = {txtEasy,txtNormal,txtHard,txtExtreme};

        for(int i=0;i<txtViewArr.length;i++){

            if(i+1 == difficulty){
                txtViewArr[i].setBackgroundColor(getResources().getColor(R.color.RANK_BUTTON_SELECT_COLOR));
                txtViewArr[i].setTextColor(Color.WHITE);
            }else{
                txtViewArr[i].setBackgroundColor(getResources().getColor(R.color.RANK_BUTTON_NOSELECT_COLOR));
                txtViewArr[i].setTextColor(Color.BLACK);
            }

        }





        // clear old data
        table.removeAllViews();

        // draw new table
        TableRow header = new TableRow(this);

        TextView rankHeader = new LadderboardCell(this, "Rank", one );
        header.addView(rankHeader);



        TextView timeElapsedHeader = new LadderboardCell(this, "Time", one);
        header.addView(timeElapsedHeader);

        TextView dateHeader = new LadderboardCell(this, "Date (DD/MM/YYYY)", one * 2 );
        header.addView(dateHeader);

        table.addView(header);


        // read and show information
        DatabaseHelper DBHelper = DatabaseHelper.newInstance(this);
        SQLiteDatabase database = DBHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM achievement WHERE difficulty = '" + difficulty + "'ORDER BY elapsedSeconds", null);
            cursor.moveToFirst();
        } catch (Exception e) {
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        for (int r = 1; r <= 10; ++r) {
            TableRow newRow = new TableRow(this);
            String rank = "", nickname = "", time = "", date = "", note = "";
            if (cursor != null && !cursor.isAfterLast()) {
                rank = String.valueOf(r);
                time = cursor.getString(cursor.getColumnIndex("elapsedSeconds"));
                date = cursor.getString(cursor.getColumnIndex("date"));
                cursor.moveToNext();
            }

            TextView rankCell = new LadderboardCell(this, rank, one / 2);
            newRow.addView(rankCell);


            String text = (time == "") ? time : Timer.getTimeFormat(Integer.parseInt(time));
            TextView timeCell = new LadderboardCell(this, text, one / 2);
            newRow.addView(timeCell);

            TextView dateCell = new LadderboardCell(this, date, one);
            newRow.addView(dateCell);


            table.addView(newRow);
        }

/*

        for(int i=0;i<10;i++) {

            TableRow newRow = new TableRow(this);
            String rank = Integer.toString(i+1), nickname = "1231", time = "3214", date = "1253", note = "11253";

            TextView rankCell = new LadderboardCell(this, rank, one / 2);
            newRow.addView(rankCell);

            TextView nicknameCell = new LadderboardCell(this, nickname, one);
            newRow.addView(nicknameCell);

            String text = (time == "") ? time : Timer.getTimeFormat(Integer.parseInt(time));
            TextView timeCell = new LadderboardCell(this, text, one / 2);
            newRow.addView(timeCell);

            TextView dateCell = new LadderboardCell(this, date, one);
            newRow.addView(dateCell);

            TextView noteCell = new LadderboardCell(this, note, one);
            newRow.addView(noteCell);

            table.addView(newRow);
        }

*/







    }
}
