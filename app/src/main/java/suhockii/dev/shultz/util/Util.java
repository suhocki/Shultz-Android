package suhockii.dev.shultz.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

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

    public static int getPageSize(@NonNull Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int itemHeight = resources.getDimensionPixelSize(R.dimen.item_shultz_height);
        return displayMetrics.heightPixels / itemHeight * 3 / 2;
    }

    public static int getFabY(@NonNull Resources resources, int visibleCount) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int itemHeight = resources.getDimensionPixelSize(R.dimen.item_shultz_height);
        int fabHeight = resources.getDimensionPixelSize(R.dimen.fab_width);
        int containerHeight = displayMetrics.heightPixels - itemHeight * visibleCount;
        return (containerHeight - fabHeight) / 2 - resources.getDimensionPixelSize(R.dimen.fab_offset);
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
}
