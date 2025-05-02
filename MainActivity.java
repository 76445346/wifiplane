package com.example.myapplication4;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.Switch;

public class MainActivity extends Activity {
//    private TextView textLeft, textRight;
    private byte x1, y1, x2, y2; // 左右摇杆的4个方向变量
    private JoystickView joystickLeft, joystickRight;
    private Switch sw1, sw2, sw3, sw4;
    private UDPBuild udpBuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置横屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 让屏幕保持常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 隐藏导航栏和状态栏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        setContentView(R.layout.activity_main);
        udpBuild = UDPBuild.getUdpBuild();
        udpBuild.setUdpReceiveCallback(data -> {
            if(data.getLength() >= 3){

            }
        });
        // 绑定控件
//        textLeft = findViewById(R.id.text_left);
//        textRight = findViewById(R.id.text_right);
        joystickLeft = findViewById(R.id.joystick_left);
        joystickRight = findViewById(R.id.joystick_right);
        sw1 = findViewById(R.id.sw1);
        sw2 = findViewById(R.id.sw2);
        sw3 = findViewById(R.id.sw3);
        sw4 = findViewById(R.id.sw4);

        joystickLeft.setLeftJoystick(true); // 让左摇杆不回中

        // 设置左摇杆监听器
        joystickLeft.setJoystickListener((x, y) -> {
            udpBuild.arr[3]=x;
            udpBuild.arr[2]=y;
//            runOnUiThread(() -> textLeft.setText("Left: X=" + (Byte.toUnsignedInt(x)) + ", Y=" + (Byte.toUnsignedInt(y))));
        });

        // 设置右摇杆监听器
        joystickRight.setJoystickListener((x, y) -> {
            x2 = x; // 映射到 0-255
            y2 = y;
            udpBuild.arr[0]=x;
            udpBuild.arr[1]=y;
//            runOnUiThread(() -> textRight.setText("Right: X=" + (Byte.toUnsignedInt(x)) + ", Y=" + (Byte.toUnsignedInt(y))));
        });

        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {// 打开时执行
                    udpBuild.arr[4] |= 1;
                } else {// 关闭时执行
                    udpBuild.arr[4] &= ~1;
                }
            }
        });
        sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {// 打开时执行
                    udpBuild.arr[4] |= 2;
                } else {// 关闭时执行
                    udpBuild.arr[4] &= ~2;
                }
            }
        });
        sw3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {// 打开时执行
                    udpBuild.arr[4] |= 4;
                } else {// 关闭时执行
                    udpBuild.arr[4] &= ~4;
                }
            }
        });
        sw4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {// 打开时执行
                    udpBuild.arr[4] |= 8;
                } else {// 关闭时执行
                    udpBuild.arr[4] &= ~8;
                }
            }
        });

        udpBuild.startTxTask();
    }
}
