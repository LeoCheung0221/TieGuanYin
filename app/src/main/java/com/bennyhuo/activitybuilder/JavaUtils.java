package com.bennyhuo.activitybuilder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bennyhuo.activitybuilder.runtime.core.ActivityBuilder;
import com.bennyhuo.activitybuilder.runtime.core.OnActivityResultListener;
import com.bennyhuo.activitybuilder.runtime.utils.Utils;

/**
 * Created by benny on 2/6/18.
 */

public class JavaUtils {
    public static void open(Context context, int num, boolean isJava,
                            final JavaActivityBuilder.OnJavaActivityResultListener onJavaActivityResultListener) {
        ActivityBuilder.INSTANCE.init(context);
        Intent intent = new Intent(context, JavaActivity.class);
        intent.putExtra("num", num);
        intent.putExtra("isJava", isJava);
        if(context instanceof Activity) {
            ActivityBuilder.INSTANCE.startActivityForResult((Activity) context, intent, new OnActivityResultListener() {
                @Override
                public void onResult(Bundle bundle) {
                    if(onJavaActivityResultListener != null) {
                        onJavaActivityResultListener.onResult(Utils.<String>get(bundle, "java"),Utils.<Integer>get(bundle, "kotlin"));
                    }
                }
            });
        }
        else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        JavaActivityBuilder.inject();
    }
}
