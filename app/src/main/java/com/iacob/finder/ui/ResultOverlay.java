package com.iacob.finder.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.iacob.finder.R;

public class ResultOverlay extends RelativeLayout {

    public ResultOverlay(Context context) {
        super(context);
    }

    public ResultOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResultOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    public void drawCircleTip(RectF rectF, float x, float y, Canvas canvas, OnClickListener clickListener) {
        Paint paintBig = new Paint();
        paintBig.setColor(getResources().getColor(R.color.card_background));
        paintBig.setStyle(Paint.Style.STROKE);
        paintBig.setStrokeWidth(10f);
        Paint paintCenter = new Paint();
        paintCenter.setColor(getResources().getColor(R.color.theme_accent));
        paintCenter.setStyle(Paint.Style.FILL);
        paintCenter.setAlpha(150);
        canvas.drawCircle(x, y, 32, paintBig);
        canvas.drawCircle(x, y, 26, paintCenter);
        FrameLayout clickableCanvas = new FrameLayout(getContext());
        clickableCanvas.setTop((int) rectF.top);
        clickableCanvas.setLeft((int) rectF.left);
        clickableCanvas.setRight((int) rectF.right);
        clickableCanvas.setBottom((int) rectF.bottom);
        clickableCanvas.setClickable(true);
        clickableCanvas.setOnClickListener(clickListener);
        addView(clickableCanvas);
    }
}
