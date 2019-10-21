package com.vsoontech.plugin;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.vsoontech.plugin.api.LiveCourseList2Resp;
import com.vsoontech.plugin.api.LiveCourseList2Resp.Purchased;
import java.util.ArrayList;

/**
 * @desc
 */
public class ScrollingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });

        TextView contentText = findViewById(R.id.contentText);
        LiveCourseList2Resp mResp = new LiveCourseList2Resp();
        mResp.purchasedList = new ArrayList<>();
        LiveCourseList2Resp.Purchased purchased = new Purchased();
        purchased.subject = "一个亿";
        mResp.purchasedList.add(purchased);
        contentText.setText(mResp.toString());




    }
}
