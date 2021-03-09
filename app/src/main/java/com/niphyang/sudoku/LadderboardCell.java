package com.niphyang.sudoku;

import android.content.Context;
import android.graphics.Color;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.Gravity;

public class LadderboardCell extends AppCompatTextView {

    public LadderboardCell(final Context context, final String text, int width) {
        super(context);
        setText(text);
        setTextColor(Color.WHITE);
        int padding = (int) AppConstant.convertDpToPixel(3, context);
        setPadding(0, padding, 0, padding);
        setGravity(Gravity.CENTER);
        setMaxLines(1);
        setWidth(width);
        setTypeface(AppConstant.APP_FONT);
        

    }

}
