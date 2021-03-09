package com.biubiu.widget;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.biubiu.widget.layout.DragLayout;

public class MainActivity extends AppCompatActivity {

    private DragLayout dragLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dragLayout = findViewById(R.id.dl);
        dragLayout.setDragLimited(DragLayout.DragLimited.WITHOUT_LIMITED);
        dragLayout.setOnDragListener(new DragLayout.OnDragListener() {
            @Override
            public void onDragStart(View view) {
                Toast.makeText(view.getContext(), "Start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDragEnd(View view) {
                Toast.makeText(view.getContext(), "End", Toast.LENGTH_SHORT).show();
                moveToCenter(dragLayout);
            }
        });


        //DragLayout中的TextView的点击事件
        TextView textView = findViewById(R.id.tv);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Click" + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
            }
        });
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(v.getContext(), "Long Click", Toast.LENGTH_SHORT).show();
                dragLayout.setDragEnable(false);
                return true;
            }
        });

        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE ||
                        event.getAction() == MotionEvent.ACTION_CANCEL ||
                        event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(v.getContext(), "drag enable", Toast.LENGTH_SHORT).show();
                    dragLayout.setDragEnable(true);
                    return false;
                }
                return false;
            }
        });


        //正常TextView的点击事件
        TextView textViewClick = findViewById(R.id.tv_click);

        textViewClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Click View", Toast.LENGTH_SHORT).show();
            }
        });
        textViewClick.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(v.getContext(), "Long Click View", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void moveToCenter(View view) {
        float x = view.getX();
        float y = view.getY();

        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view,
                "x",
                x,
                400);
        animatorX.setDuration(160);
        animatorX.start();

        ObjectAnimator animatorY = ObjectAnimator.ofFloat(view,
                "y",
                y,
                400);
        animatorY.setDuration(160);
        animatorY.start();
    }
}
