package comb.example.cold.plugdemo;

import android.app.Activity;
import android.os.Environment;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    private PlugContext plugContext;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        plugContext=new PlugContext(MainActivity.this);
        // apk所在的路径, 换成自己的
        String dexpath = Environment.getExternalStorageDirectory() + "/plug.apk";
        plugContext.loadApk(dexpath,"com.example.cold.sometest");
        rootView=plugContext.getLayout("activity_main");
        setContentView(rootView);
    }

}
