package cn.aorise.grid.ui.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.aorise.grid.R;
import cn.aorise.grid.module.network.entity.response.Region;

/**
 * Author: gaoxu
 * TIME: 2017/8/28
 * Description: This is PopedomAdapter
 * Function:
 */

public class PopedomAdapter extends BaseQuickAdapter<Region, BaseViewHolder> {
    public PopedomAdapter(@LayoutRes int layoutResId, @Nullable List<Region> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Region item) {
        helper.setText(R.id.tv_popedom_name, item.name);
    }

}
