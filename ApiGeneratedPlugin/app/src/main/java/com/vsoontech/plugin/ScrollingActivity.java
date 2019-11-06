package com.vsoontech.plugin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.linkin.base.app.BaseActivity;
import com.vsoontech.base.http.RequestManager;
import com.vsoontech.plugin.BModel.Grade;
import com.vsoontech.plugin.api.LiveCourseListReq;
import com.vsoontech.plugin.api.LiveCourseListReq.Params;

/**
 * @desc
 */
public class ScrollingActivity extends BaseActivity {

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
        RequestManager.getInstance().setRequestAssertData(true);

        LiveCourseListReq.Params params = new Params();
        int a = params.grade;




//        final TextView contentText = findViewById(R.id.contentText);
//        SampleComparamReq.Params params = new Params();
//        params.grade = 1;
//        contentText.setText("SampleComparamReq");
//        new SampleComparamReq(params).execute(new IHttpObserver() {
//            @Override
//            public void onHttpSuccess(String s, Object o) {
//                Log.d("SampleComparamReq", s);
//                SampleComparamModel model = new SampleComparamModel((SampleComparamResp) o);
//
//                contentText.setText(model.toString());
//
//
//            }
//
//            @Override
//            public void onHttpError(String s, int i, HttpError httpError) {
//                Log.d("SampleComparamReq", httpError.getMessage());
//                contentText.setText(httpError.toString());
//            }
//        }, SampleComparamResp.class);

    }

    @NonNull
    @Override
    protected String getActivityName() {
        return "ScrollingActivity";
    }
}
