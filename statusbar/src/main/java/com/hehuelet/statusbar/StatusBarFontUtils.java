package com.hehuelet.statusbar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.IntDef;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 状态栏字体和图标颜色修改
 * Created by HeHu on 2017/7/25/0025.
 */

public class StatusBarFontUtils {

    @IntDef({
            OTHER,
            MIUI,
            FLYME,
            ANDROID_M
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SystemType {

    }

    public static final int OTHER = 0;
    public static final int MIUI = 1;
    public static final int FLYME = 2;
    public static final int ANDROID_M = 3;


    public static boolean isLightColor(Activity activity, int color) {
        return toGrey(color) > 225;
    }

    /**
     * 把颜色转换成灰度值。
     * 代码来自 Flyme 示例代码
     */
    public static int toGrey(int color) {
        int blue = Color.blue(color);
        int green = Color.green(color);
        int red = Color.red(color);
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }

    /**
     * 已知系统类型时，设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUI6、Flyme和6.0以上版本其他Android
     */

    public static void setLightMode(Activity activity) {
        setStatusBarMode(activity, true);

    }

    /**
     * 清除MIUI或flyme或6.0以上版本状态栏黑色字体
     */
    public static void setDarkMode(Activity activity) {
        setStatusBarMode(activity, false);
    }

    /**
     * 已知系统类型时，设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUI6、Flyme和6.0以上版本其他Android
     *
     * @param type 1:MIUI 2:Flyme 3:android6.0
     */
    public static void setLightMode(Activity activity, @SystemType int type) {
        setStatusBarMode(activity, type, true);

    }

    /**
     * 清除MIUI或flyme或6.0以上版本状态栏黑色字体
     */
    public static void setDarkMode(Activity activity, @SystemType int type) {
        setStatusBarMode(activity, type, false);
    }

    /**
     * 设置状态栏黑色字体图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @return 1:MIUI 2:Flyme 3:android6.0
     */
    public static int setStatusBarMode(Activity activity, boolean isFontColorDark) {
        @SystemType int result = OTHER;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (new MIUIHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                result = MIUI;
            } else if (new FlymeHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                result = FLYME;
            } else if (new AndroidMHelper().setStatusBarLightMode(activity, isFontColorDark)) {
                result = ANDROID_M;
            }
        }
        return result;
    }

    /**
     * 已知系统类型时，设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUI6、Flyme和6.0以上版本其他Android
     *
     * @param type            1:MIUI 2:Flyme 3:android6.0
     * @param isFontColorDark
     */
    private static void setStatusBarMode(Activity activity, @SystemType int type, boolean
            isFontColorDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (type == MIUI) {
                new MIUIHelper().setStatusBarLightMode(activity, isFontColorDark);
            } else if (type == FLYME) {
                new FlymeHelper().setStatusBarLightMode(activity, isFontColorDark);
            } else if (type == ANDROID_M) {
                new AndroidMHelper().setStatusBarLightMode(activity, isFontColorDark);
            }
        }
    }

    public interface IStatusBarFontHelper {
        boolean setStatusBarLightMode(Activity activity, boolean isFontColorDark);

    }

    public static class AndroidMHelper implements IStatusBarFontHelper {
        /**
         * @return if version is lager than M
         */
        @Override
        public boolean setStatusBarLightMode(Activity activity, boolean isFontColorDark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isFontColorDark) {
                    // 沉浸式
                    activity.getWindow().getDecorView().setSystemUiVisibility(View
                            .SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                            .SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    //非沉浸式
                    //activity.getWindow().getDecorView().setSystemUiVisibility(View
                    //        .SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    //非沉浸式
                    activity.getWindow().getDecorView().setSystemUiVisibility(View
                            .SYSTEM_UI_FLAG_VISIBLE);
                }
                return true;
            }
            return false;
        }

    }

    public static class MIUIHelper implements IStatusBarFontHelper {

        /**
         * 设置状态栏字体图标为深色，需要MIUI6以上
         *
         * @param isFontColorDark 是否把状态栏字体及图标颜色设置为深色
         * @return boolean 成功执行返回true
         */
        @Override
        public boolean setStatusBarLightMode(Activity activity, boolean isFontColorDark) {
            Window window = activity.getWindow();
            boolean result = false;
            if (window != null) {
                Class clazz = window.getClass();
                try {
                    int darkModeFlag = 0;
                    Class layoutParams = Class.forName("android.view" +
                            ".MiuiWindowManager$LayoutParams");
                    Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                    darkModeFlag = field.getInt(layoutParams);
                    Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                    if (isFontColorDark) {
                        extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                    } else {
                        extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                    }
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }


    public static class FlymeHelper implements IStatusBarFontHelper {

        /**
         * 设置状态栏图标为深色和魅族特定的文字风格
         * 可以用来判断是否为Flyme用户
         *
         * @param isFontColorDark 是否把状态栏字体及图标颜色设置为深色
         * @return boolean 成功执行返回true
         */
        @Override
        public boolean setStatusBarLightMode(Activity activity, boolean isFontColorDark) {
            Window window = activity.getWindow();
            boolean result = false;
            if (window != null) {
                try {
                    WindowManager.LayoutParams lp = window.getAttributes();
                    Field darkFlag = WindowManager.LayoutParams.class
                            .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                    Field meizuFlags = WindowManager.LayoutParams.class
                            .getDeclaredField("meizuFlags");
                    darkFlag.setAccessible(true);
                    meizuFlags.setAccessible(true);
                    int bit = darkFlag.getInt(null);
                    int value = meizuFlags.getInt(lp);
                    if (isFontColorDark) {
                        value |= bit;
                    } else {
                        value &= ~bit;
                    }
                    meizuFlags.setInt(lp, value);
                    window.setAttributes(lp);
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }


}

