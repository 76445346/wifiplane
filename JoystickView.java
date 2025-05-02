package com.example.myapplication4;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private float centerX, centerY, baseSize, knobX, knobY, knobSize;
    private Paint basePaint, knobPaint, shadowPaint;
    private JoystickListener listener;
    private boolean isLeftJoystick = false; // 默认是右摇杆

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 方形底座
        basePaint = new Paint();
        basePaint.setColor(Color.parseColor("#333333")); // 深灰色
        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setAntiAlias(true);

        // 摇杆（渐变玻璃球效果）
        knobPaint = new Paint();
        knobPaint.setStyle(Paint.Style.FILL);
        knobPaint.setAntiAlias(true);

        // 阴影
        shadowPaint = new Paint();
        shadowPaint.setColor(Color.parseColor("#66000000")); // 半透明黑色
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        baseSize = Math.min(w, h) * 0.6f;  // 设定方形区域大小
        knobSize = baseSize / 8;  // 摇杆大小
        centerX = w / 2f;
        centerY = h / 2f;
        resetKnob();
    }

    public void setLeftJoystick(boolean isLeft) {
        this.isLeftJoystick = isLeft;
    }

    private void resetKnob() {
        knobX = centerX;
        knobY = isLeftJoystick ? (centerY + baseSize / 2) : centerY; // 左摇杆初始化到底部
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = centerX - baseSize / 2;
        float top = centerY - baseSize / 2;
        float right = centerX + baseSize / 2;
        float bottom = centerY + baseSize / 2;

        // 画阴影
        canvas.drawRect(left, top + 10, right, bottom + 10, shadowPaint);

        // 画底座（带圆角）
        Paint roundRectPaint = new Paint(basePaint);
        roundRectPaint.setShader(new LinearGradient(left, top, right, bottom,
                Color.parseColor("#444444"), Color.parseColor("#222222"), Shader.TileMode.CLAMP));
        canvas.drawRoundRect(left, top, right, bottom, 40, 40, roundRectPaint);

        // 画摇杆（带渐变）
        knobPaint.setShader(new RadialGradient(knobX, knobY, knobSize,
                Color.parseColor("#55A1FF"), // 亮蓝色高光
                Color.parseColor("#0055AA"), // 深蓝色阴影
                Shader.TileMode.CLAMP));
        canvas.drawCircle(knobX, knobY, knobSize, knobPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float newX = event.getX();
                float newY = event.getY();

                // 限制在正方形区域内
                float halfSize = baseSize / 2;
                float minX = centerX - halfSize;
                float maxX = centerX + halfSize;
                float minY = centerY - halfSize;
                float maxY = centerY + halfSize;

                knobX = Math.max(minX, Math.min(newX, maxX));
                knobY = Math.max(minY, Math.min(newY, maxY));

                invalidate();

                if (listener != null) {
                    float normalizedX = (knobX - centerX) / halfSize;
                    float normalizedY = (knobY - centerY) / halfSize;

                    byte xByte = mapToByte(normalizedX);
                    byte yByte = mapToByte(normalizedY);

                    listener.onJoystickMoved(xByte, yByte);
                }
                break;

            case MotionEvent.ACTION_UP:
                knobX = centerX;
                if (!isLeftJoystick) {
                    knobY = centerY;
                }
                invalidate();

                if (listener != null) {
                    byte xByte = 127; // 中间位置
                    byte yByte = isLeftJoystick ? mapToByte((knobY - centerY) / (baseSize / 2)) : 127;
                    listener.onJoystickMoved(xByte, yByte);
                }
                break;
        }
        return true;
    }

    /**
     * 将 -1.0 ~ 1.0 转换为 0 ~ 255 的 byte 值
     */
    private byte mapToByte(float value) {
        int intValue = (int) ((value + 1.0) * 127.5);
        return (byte) Math.max(0, Math.min(255, intValue)); // 限制范围
    }

    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;
    }

    public interface JoystickListener {
        void onJoystickMoved(byte xByte, byte yByte);
    }
}
