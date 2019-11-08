package com.biubiu.widget;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.biubiu.widget.layout.DragLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DragLayout dragLayout = findViewById(R.id.dl);

        dragLayout.setOnDragListener(new DragLayout.OnDragListener() {
            @Override
            public void onDragStart(View view) {
                Toast.makeText(view.getContext(), "Start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDragEnd(View view) {
                Toast.makeText(view.getContext(), "End", Toast.LENGTH_SHORT).show();
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
                return true;
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
}
