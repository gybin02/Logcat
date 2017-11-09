package com.meiyou.jet.logcat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.meiyou.framework.LogCatHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogCatHelper logCatHelper = LogCatHelper.getInstance();
        if (BuildConfig.DEBUG) { //avoid execution on release, it is only for testing purpoise

            logCatHelper.startServer(getApplicationContext());
//            RemoteLogcatServer logcatServer
//                    = new RemoteLogcatServer(
//                    8080,  //port to open connection
//                    5000,  //page reload rate (ms)
//                    getApplicationContext()
//            );
//            logcatServer.startServer();
        }

        findViewById(R.id.btn_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Show Toast", Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.btn_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = 9 / 0;
            }
        });
        
        
        TextView tvShow = findViewById(R.id.tv_show);
        tvShow.setText(logCatHelper.getServerIp(this));
    }


}
