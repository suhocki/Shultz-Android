package suhockii.dev.shultz.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

import java.util.Locale;

import suhockii.dev.shultz.R;

public class Util {
    public static Locale getCurrentLocale(@NonNull Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return configuration.getLocales().get(0);
        } else {
            //noinspection deprecation
            return configuration.locale;
        }
    }

    public static int getPageSize(@NonNull RecyclerView view) {
        Resources resources = view.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int itemHeight = resources.getDimensionPixelSize(R.dimen.item_shultz_height);
        return displayMetrics.heightPixels / itemHeight * 3 / 2;
    }
}
