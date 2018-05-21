package cn.aorise.grid.ui.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

/**
 * Created by tangjy on 2017/8/25.
 */

public interface ITabLayerCycle {
    /**
     * 创建tab
     *
     * @param fm
     * @param fragments
     * @param titles
     */
    void setup(FragmentManager fm, ArrayList<Fragment> fragments, ArrayList<String> titles);

    /**
     * 更新UI
     *
     * @param object
     */
    void update(Object object);
}
