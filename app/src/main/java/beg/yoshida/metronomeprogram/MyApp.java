package beg.yoshida.metronomeprogram;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;


//マニフェストどこで必要になっているかのテスト用。後にマニフェスト追記文ともども消す。android:name=".MyApp"
public final class MyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        // HookedContext に差し替える
        super.attachBaseContext(new HookedContext(base));
    }

    // ここが監視用の Context
    static final class HookedContext extends ContextWrapper {
        HookedContext(Context base) {
            super(base);
        }

        @Override
        public Context createAttributionContext(String tag) {
            Log.e("AttrFinder", "hooked! tag=" + tag,
                    new Throwable("stack trace"));
            return super.createAttributionContext(tag);
        }
    }
}
