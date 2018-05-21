package cn.aorise.grid.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.List;

import cn.aorise.grid.R;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.databinding.GridFragmentPopedomBinding;
import cn.aorise.grid.module.network.entity.response.Region;
import cn.aorise.grid.ui.activity.TabActivity;
import cn.aorise.grid.ui.adapter.PopedomAdapter;
import cn.aorise.grid.ui.base.GridBaseFragment;
import cn.aorise.grid.ui.view.RecycleViewDivider;


/**
 * Created by pc on 2017/3/2.
 */
public class PopedomFragment extends GridBaseFragment implements BaseQuickAdapter.OnItemClickListener {
    private final String TAG = PopedomFragment.class.getSimpleName();
    private GridFragmentPopedomBinding mBinding;
    private List<Region> mRegionList;
    private PopedomAdapter mPopedomAdapter;
    private int mRegionRid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.grid_fragment_popedom, container, false);
        initItemView();
        return mBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public PopedomFragment() {
    }

    public static PopedomFragment newInstance() {
        PopedomFragment fragment = new PopedomFragment();
        return fragment;
    }


    private void initItemView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mBinding.rvPopedom.setLayoutManager(linearLayoutManager);
        mBinding.rvPopedom.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, 1, getResources().getColor(R.color.grid_div_item)));
        mPopedomAdapter = new PopedomAdapter(R.layout.grid_item_popedom, mRegionList);
        mPopedomAdapter.setOnItemClickListener(this);
        mBinding.rvPopedom.setAdapter(mPopedomAdapter);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Intent intent = new Intent(getContext(), TabActivity.class);
        intent.putExtra(Constant.TransportKey.REGION_RID_KEY, mRegionList.get(position));
        getContext().startActivity(intent);
    }


    public void update(List<Region> regions) {
        mRegionList = regions;
        initItemView();
        mPopedomAdapter.notifyDataSetChanged();
    }
}
