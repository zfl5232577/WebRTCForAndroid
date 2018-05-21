package cn.aorise.grid.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.aorise.grid.R;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.databinding.GridFragmentPersonalBinding;
import cn.aorise.grid.module.network.entity.response.User;
import cn.aorise.grid.ui.adapter.PersonalAdapter;
import cn.aorise.grid.ui.base.GridBaseFragment;


/**
 * Created by pc on 2017/3/2.
 */
public class PersonalFragment extends GridBaseFragment {
    private GridFragmentPersonalBinding mBinding;
    private List<User> mUserList = new ArrayList<>();
    private List<User> mOnLineUserList = new ArrayList<>();
    private List<User> mOffLineUserList = new ArrayList<>();
    private PersonalAdapter mPersonalAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.grid_fragment_personal, container, false);
        initItemView();
        return mBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public PersonalFragment() {
    }

    public static PersonalFragment newInstance() {
        PersonalFragment fragment = new PersonalFragment();
        return fragment;
    }

    private void initItemView() {
        mBinding.rvPerson.setLayoutManager(new LinearLayoutManager(getContext()));
        mPersonalAdapter = new PersonalAdapter(mUserList, getContext());
        mBinding.rvPerson.setAdapter(mPersonalAdapter);

    }

    public void update(List<User> userList) {
        if (userList != null) {
            mUserList.clear();
            mOffLineUserList.clear();
            mOnLineUserList.clear();
            for (User user : userList) {
                if (Constant.Status.ON_LINE.equals(user.status) || Constant.Status.BUSY.equals(user.status)) {
                    mOnLineUserList.add(user);
                } else if (Constant.Status.OFF_Line.equals(user.status)) {
                    mOffLineUserList.add(user);
                }
            }
            mUserList.addAll(mOnLineUserList);
            mUserList.addAll(mOffLineUserList);
        }
        mPersonalAdapter.notifyDataSetChanged();
    }
}
