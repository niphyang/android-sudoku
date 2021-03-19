package com.niphyang.sudoku;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Stack;

import static android.widget.Toast.LENGTH_LONG;

public class GameActivity extends AppCompatActivity {
    static public final int[] NUMBER_OF_EMPTY_CELLS = {0, 30, 35, 45, 50};

    // Difficulty expected to be simple, easy, intermediate, expert, or any
    //static public final String[] DIFFICULT_NAME = {"NONE", "Easy", "Normal", "Hard", "Extreme"};
    static public final String[] DIFFICULT_NAME = {"NONE", "simple", "easy", "intermediate", "expert"};

    static private SudokuGrid grid;
    static private Numpad numpad;
    static private Stack<CellState> stack = new Stack<>();

    boolean isDelete = false;
    boolean isPause = false;

    SudokuSolver solver = new SudokuSolver();


    /* game state */
    private int[][] solution;
    private int difficulty;
    static private int status; // -3 game done | -2: auto solved | -1: auto fill | 0: playing | 1: player solved
    private Timer timer;
    private int hintCnt;

    // TEST
//    private String [] rewardsAdUnitIdArr = {
//            "ca-app-pub-3940256099942544/5224354917"
//            ,"ca-app-pub-3940256099942544/5224354917"
//            ,"ca-app-pub-3940256099942544/5224354917"
//            ,"ca-app-pub-3940256099942544/5224354917"
//            ,"ca-app-pub-3940256099942544/5224354917"
//    };

    // REAL
    private String [] rewardsAdUnitIdArr = { "ca-app-pub-2327476184552798/2721932269"
            , "ca-app-pub-2327476184552798/4169194545"
            , "ca-app-pub-2327476184552798/5733557653"
            , "ca-app-pub-2327476184552798/7467465246"
            , "ca-app-pub-2327476184552798/6154383572"};

    private void generateGrid() {
        // generate a grid
        solution = solver.getRandomGrid(DIFFICULT_NAME[difficulty]);
        int[][] masks = new int[9][9];

        // compute masks
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                if(solution[row][col] > 9) {
                    masks[row][col] = 0;
                }
                else {
                    masks[row][col] = (1 << solution[row][col]);
                }
            }
        }

        grid = new SudokuGrid(this, masks, solution);

        //정답 채우기
        //grid.showSolution();


    }

    private void restoreGrid(String solutionString, String gridString) {
        // restore solution
        solution = new int[9][9];
        Scanner scanner = new Scanner(solutionString);
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                solution[row][col] = scanner.nextInt();
            }
        }
        // restore masks
        int[][] masks = new int[9][9];
        scanner = new Scanner(gridString);
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                masks[row][col] = scanner.nextInt();
            }
        }

        grid = new SudokuGrid(this, masks, solution);
    }

    private void saveGame() {
        if (status < -1) return;
        int[][] currentMask = grid.getCurrentMasks();


        GameState state = new GameState(status, difficulty, timer.getElapsedSeconds(), solution, currentMask, hintCnt);
        try {
            DatabaseHelper DBHelper = DatabaseHelper.newInstance(this);
            SQLiteDatabase database = DBHelper.getWritableDatabase();
            database.insert("GameState", null, state.getContentValues());
        } catch (Exception e) {
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        if(!isDelete){
            saveGame();
            if(!isPause){
                isPause = true;
                timer.stop();
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {

        if(isPause){
            timer.start();
            isPause = false;
        }

        super.onResume();
    }

    boolean wannaBack = false;
    @Override
    public void onBackPressed() {
        if (wannaBack) {
            super.onBackPressed();
            return;
        }

        wannaBack = true;
        Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 진행상항이 저장되고 메인 메뉴로 돌아갑니다.", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wannaBack = false;
            }
        }, 3000);
    }



    private void onClickAutoFill() {
        if (status < -1) return;
        if (status == -1) {
            autoFill();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog);
            dialog.setMessage("Nếu sử dụng tính năng này, kết quả của bạn sẽ không được công nhận trên bảng xếp hạng. \nBạn có chắc chắn muốn sử dụng?")
                    .setTitle("Chú ý\n")
                    .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            status = -1;
                            autoFill();
                        }
                    })
                    .setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show();
        }
    }

    private void onClickTutorial() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyTutorialTheme);
        Spannable message = SpannableWithImage.getTextWithImages(this, getString(R.string.tutorial), 50);

        int position = getString(R.string.tutorial).indexOf("Chú ý:");
        message.setSpan(new RelativeSizeSpan(1.2f), position, position + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        dialog.setMessage(message).setTitle("Hướng dẫn").show();
    }

    private void autoFill() {
        grid.fill();
        updateNumpad();
    }

    //애드몹 광고
    private AdView adView = null;
    private RewardedAd [] rewardedAdArr = new RewardedAd[rewardsAdUnitIdArr.length];
    private int rewardedAdIdx = 0;

    @SuppressLint({"ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

                //리워드 광고 1개 먼저 준비
                rewardedAdArr[0] = createAndLoadRewardedAd(0);

                //배너광고 로드
                adView = findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);


                for(int i=1;i<rewardedAdArr.length;i++){
                    rewardedAdArr[i] = createAndLoadRewardedAd(i);
                }

            }
        });







        Bundle bundle = getIntent().getExtras();
        difficulty = bundle.getInt("difficulty", 0);
        status = bundle.getInt("status", 0);
        hintCnt = bundle.getInt("hintCnt", 0);

        // lock screen orientation
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);


        // hide status bar
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);







        String solutionString = bundle.getString("solutionString", "none");
        String gridString = bundle.getString("gridString", "none");
        if (solutionString.equals("none") || gridString.equals("none")) {
            generateGrid();
        } else {
            restoreGrid(solutionString, gridString);
        }

        numpad = new Numpad(this);

        int elapsedTime = bundle.getInt("elapsedSeconds", 0);
        timer = new Timer(this, elapsedTime);
        timer.start();


    }

    public RewardedAd createAndLoadRewardedAd(int idx) {


        final RewardedAd rewardedAd = new RewardedAd(this,rewardsAdUnitIdArr[idx]);
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad Fail loaded

            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }

    public static void updateNumpad() {
        Cell selectedCell = grid.getSelectedCell();
        if (selectedCell != null) {
            numpad.update(selectedCell.getMask(), selectedCell.isMarked());
        }
    }

    public void onClickHint(View view) {


        if(grid.getSelectedCell() == null || grid.getSelectedCell().isLocked()){
            Toast.makeText(this, "힌트를 얻을 셀을 선택한 후 힌트 버튼을 눌러주세요.", Toast.LENGTH_SHORT).show();
        }else{

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("짧은 광고를 시청 후 선택된 셀의 정답을 표시하시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                            if (rewardedAdArr[rewardedAdIdx].isLoaded()) {
                                Activity activityContext = GameActivity.this;
                                RewardedAdCallback adCallback = new RewardedAdCallback() {
                                    @Override
                                    public void onRewardedAdOpened() {
                                        // Ad opened.
                                    }

                                    @Override
                                    public void onRewardedAdClosed() {
                                        rewardedAdArr[rewardedAdIdx] = createAndLoadRewardedAd(rewardedAdIdx);
                                    }

                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem reward) {
                                        // User earned reward.
                                        int selectIndex = grid.getSelectedCell().getIndex();
                                        int row = selectIndex / 9;
                                        int col = selectIndex % 9;
                                        grid.getSelectedCell().setNumber(solution[row][col] & ~1024);
                                        timer.setHintPenalty();

                                        hintCnt++;


                                        updateNumpad();

                                    }

                                    @Override
                                    public void onRewardedAdFailedToShow(AdError adError) {
                                        Toast.makeText(getApplicationContext(), "리워드 광고를 불러오지 못했습니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                    }
                                };
                                rewardedAdArr[rewardedAdIdx].show(activityContext, adCallback);

                                rewardedAdIdx++;
                                if(rewardedAdIdx == rewardedAdArr.length){
                                    rewardedAdIdx = 0;
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "리워드 광고가 준비되지 않았습니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                            }





                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.cancel();
                        }
                    })
                    .show();




        }
    }
    public void onClickBack(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("진행상항을 저장하고 메인 메뉴로 돌아가시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.cancel();
                        finish();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }
    public void onClickGiveup(View view) {



        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("포기하시면 진행상황이 삭제됩니다. 포기하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            DatabaseHelper DBHelper = DatabaseHelper.newInstance(getApplicationContext());
                            SQLiteDatabase database = DBHelper.getWritableDatabase();

                            database.execSQL("DELETE FROM GameState WHERE difficulty = '" + difficulty + "';");




                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            dialogInterface.cancel();
                            isDelete = true;
                            finish();
                        }
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {



                    }
                })
                .show();

    }
    public void onClickSubmit(View view) {

        boolean isHaveEmpty = false;

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){
                if(grid.getCell(i,j).getNumber() == 0){
                    isHaveEmpty = true;
                    break;
                }
            }
            if(isHaveEmpty){
                break;
            }
        }




        if (isHaveEmpty) {
            //빈 칸이 있다
            Toast.makeText(this, "빈 칸을 채워주세요", LENGTH_LONG).show();
            return;
        }
        if (solver.checkValidGrid(grid.getNumbers())) {
            status = 0;
            if (status >= 0) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("정답! 축하합니다!\n기록 : " + timer.getElapsedTimeString())
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveAchievement();
                            }
                        })
                        .show();
                timer.stop();
                status = 1;

            }
            // set status to GAME_DONE
            status = -3;
        } else {
            //정답 틀림




            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("틀렸습니다. 수정 후 다시 제출해주세요. 짧은 광고를 시청 후 틀린 부분을 표시하시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int v) {


                            if (rewardedAdArr[rewardedAdIdx].isLoaded()) {
                                Activity activityContext = GameActivity.this;
                                RewardedAdCallback adCallback = new RewardedAdCallback() {
                                    @Override
                                    public void onRewardedAdOpened() {
                                        // Ad opened.
                                    }

                                    @Override
                                    public void onRewardedAdClosed() {
                                        rewardedAdArr[rewardedAdIdx] = createAndLoadRewardedAd(rewardedAdIdx);
                                    }

                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem reward) {
                                        // User earned reward.
                                        int [][] nowGrid  = grid.getNumbers();
                                        for(int i=0;i<nowGrid.length;i++){
                                            for(int j=0;j<nowGrid[i].length;j++){
                                                if(grid.getCell(i,j).getNumber() != solution[i][j]){

                                                    if(grid.getCell(i,j).getNumber() != (solution[i][j] & ~1024)){
                                                        grid.getCell(i,j).setTextColor(Color.RED);
                                                        updateNumpad();
                                                    }

                                                }
                                            }
                                        }

                                    }

                                    @Override
                                    public void onRewardedAdFailedToShow(AdError adError) {
                                        Toast.makeText(getApplicationContext(), "리워드 광고를 불러오지 못했습니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                    }
                                };
                                rewardedAdArr[rewardedAdIdx].show(activityContext, adCallback);

                                rewardedAdIdx++;
                                if(rewardedAdIdx == rewardedAdArr.length){
                                    rewardedAdIdx = 0;
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "리워드 광고가 준비되지 않았습니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                            }







                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                        }
                    })
                    .show();

           //Toast.makeText(this, "Đáp án sai", LENGTH_LONG).show();
        }
    }

    private void saveAchievement() {
//        Intent intent = new Intent(this, SaveAchievementActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        intent.putExtra("difficulty", difficulty);
//        intent.putExtra("elapsedSeconds", timer.getElapsedSeconds());
//        startActivity(intent);


        try {
            DatabaseHelper DBHelper = DatabaseHelper.newInstance(this);
            SQLiteDatabase database = DBHelper.getWritableDatabase();

            // insert into achievement table
            SimpleDateFormat formatFactory = new SimpleDateFormat("dd/MM/yyyy");
            ContentValues values = new ContentValues();
            values.put("difficulty", difficulty);
            values.put("date", formatFactory.format(Calendar.getInstance().getTime()));
            values.put("elapsedSeconds", timer.getElapsedSeconds());
            values.put("hintCnt", hintCnt);

            database.insert("achievement", null, values);
            database.execSQL("DELETE FROM GameState WHERE difficulty = '" + difficulty + "';");


        } catch (Exception e) {
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            Intent intent = new Intent(this,LadderboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
            finish();

        }

    }

    public void onClickSolve() {
        if(status >= 0) {
            status = -2;
            onClickSolve();
        }

        if(status < 0) {
            status = -2;
            grid.showSolution();
            updateNumpad();
            timer.stop();
        }
    }

    private void onClickReset() {
        if (status < -1) return;
        grid.clear();
        updateNumpad();
    }

    public static void onPressNumpad(int number) {
        if(status < -1) return;
        Cell selectedCell = grid.getSelectedCell();
        if (number < 10) {

            // backup current selected cell state
            stack.push(selectedCell.getState());


            if(!selectedCell.isMarked()){
                selectedCell.setNumber(number);
            }else{
                // add a number to selected cell
                selectedCell.addNumber(number);
            }

        } else if (number == 10) {
            // mark selected cell
            selectedCell.setMarked(!selectedCell.isMarked());
        } else if (number == 11) {
            // restore previous selected cell state
            if (!stack.isEmpty()) {
                CellState preState = stack.peek();
                stack.pop();
                int index = preState.index;
                int row = index / 9;
                int col = index - row * 9;
                grid.getCell(row, col).setMask(preState.mask);
            }
        }
        updateNumpad();
    }

    public static void setNumpadVisible(int state) {
        numpad.setVisibility(state);
    }

    public static void highlightNeighborCells(int index) {
        grid.highlightNeighborCells(index);
    }

    public static void setSelectedCell(int index) {
        grid.setSelectedCell(index);
    }
}
