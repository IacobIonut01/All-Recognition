// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.iacob.finder.processors.cloudtextrecognition;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.iacob.finder.R;
import com.iacob.finder.common.GraphicOverlay;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class CloudTextGraphic extends GraphicOverlay.Graphic {
    private static final int TEXT_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final FirebaseVisionText.Element element;
    private final GraphicOverlay overlay;

    CloudTextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element element) {
        super(overlay);

        this.element = element;
        this.overlay = overlay;

        rectPaint = new Paint();
        rectPaint.setColor(getApplicationContext().getColor(R.color.theme_accent));
        rectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);
        rectPaint.setAlpha(150);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (element == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        Rect rect = element.getBoundingBox();
        RectF rectf = new RectF(element.getBoundingBox());
        rectf.left = translateX(rectf.left);
        rectf.top = translateY(rectf.top);
        rectf.right = translateX(rectf.right);
        rectf.bottom = translateY(rectf.bottom);
        canvas.drawRoundRect(rectf, 12f, 12f, rectPaint);
        textPaint.setTextSize((rect.bottom - rect.top) - ((rectf.right-rectf.left)/100f));
        canvas.drawText(element.getText(), rectf.left, rectf.bottom, textPaint);
    }
}
