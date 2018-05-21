package cn.aorise.grid.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.grid.R;
import cn.aorise.grid.common.GlideCircleTransform;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.module.network.API;
import cn.aorise.grid.module.network.entity.response.Session;
import cn.aorise.grid.module.network.entity.response.User;
import cn.aorise.grid.ui.base.GridBaseActivity;
import cn.aorise.webrtc.chat.ChatClient;
import cn.aorise.webrtc.chat.SignalManager;

/**
 * Author: gaoxu
 * TIME: 2017/8/28
 * Description: This is PersonalAdapter
 * Function:
 */

public class PersonalAdapter extends RecyclerView.Adapter {
    private static final String TAG = PersonalAdapter.class.getSimpleName();
    private List<User> mUserList;
    private Context mContext;
    private String userName;
    private User sender;

    public PersonalAdapter(List<User> userList, Context context) {
        this.mUserList = userList;
        this.mContext = context;
        Session session = GsonUtils.fromJson(SPUtils.getInstance().getString(Constant.SPCache.USER, ""), Session.class);
        if (session == null) {
            return;
        }
        sender = session.getUser();
        userName = sender.username;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.grid_item_personal, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        User user = mUserList.get(position);
        String imgurl = user.imgurl;
        myViewHolder.ivFavicon.setImageResource(R.drawable.grid_def_favicon);
        myViewHolder.ivFavicon.setTag(imgurl);

        if (mUserList.get(position).id == Constant.ADMIN_ID) {
            myViewHolder.tvPersonName.setText(mContext.getString(R.string.grid_person_admin_name));
        } else {
            myViewHolder.tvPersonName.setText(mUserList.get(position).name);
        }

        if (user.status.equals(Constant.Status.ON_LINE)) {
            myViewHolder.tvPersonStatus.setText(mContext.
                    getResources().getString(R.string.grid_person_status1));
            myViewHolder.ivStatus.setImageResource(R.drawable.grid_ic_online_status);
            myViewHolder.layoutCall.setVisibility(View.VISIBLE);
            if (imgurl != null && imgurl.equals(myViewHolder.rvPersonInformation.getTag())) {
                Glide.with(mContext).load(API.BASE_URL + imgurl)
                        .transform(new GlideCircleTransform(mContext))
                        .error(R.drawable.grid_def_online_favicon)
                        .placeholder(R.drawable.grid_def_online_favicon)
                        .crossFade(500)
                        .into(myViewHolder.ivFavicon);
            }
        } else if (user.status.equals(Constant.Status.OFF_Line)) {
//            myViewHolder.tvPersonStatus.setText(mContext.
//                    getResources().getString(R.string.grid_person_status2));
//            myViewHolder.ivStatus.setImageResource(R.drawable.grid_ic_offline_status);
//            myViewHolder.layoutCall.setVisibility(View.GONE);
            myViewHolder.tvPersonStatus.setText(mContext.
                    getResources().getString(R.string.grid_person_status1));
            myViewHolder.ivStatus.setImageResource(R.drawable.grid_ic_online_status);
            if (imgurl != null && imgurl.equals(myViewHolder.rvPersonInformation.getTag())) {
                Glide.with(mContext).load(API.BASE_URL + imgurl)
                        .transform(new GlideCircleTransform(mContext))
                        .error(R.drawable.grid_def_online_favicon)
                        .placeholder(R.drawable.grid_def_online_favicon)
                        .crossFade(500)
                        .into(myViewHolder.ivFavicon);
            }
//            if (imgurl != null && imgurl.equals(myViewHolder.rvPersonInformation.getTag())) {
//                Glide.with(mContext).load(API.BASE_URL + imgurl)
//                        .error(R.drawable.grid_def_offline_favicon)
//                        .placeholder(R.drawable.grid_def_offline_favicon)
////                        .bitmapTransform(new GlideGrayscaleTransformation(mContext), new GlideCircleTransform(mContext))
//                        .crossFade(500)
//                        .into(myViewHolder.ivFavicon);
//            } else {
//                myViewHolder.ivFavicon.setImageResource(R.drawable.grid_def_offline_favicon);
//            }
        } else if (user.status.equals(Constant.Status.BUSY)) {
//            myViewHolder.tvPersonStatus.setText(mContext.
//                    getResources().getString(R.string.grid_person_status3));
//            myViewHolder.ivStatus.setImageResource(R.drawable.grid_ic_busy_status);
//            myViewHolder.layoutCall.setVisibility(View.GONE);
            myViewHolder.tvPersonStatus.setText(mContext.
                    getResources().getString(R.string.grid_person_status1));
            myViewHolder.ivStatus.setImageResource(R.drawable.grid_ic_online_status);
            if (imgurl != null && imgurl.equals(myViewHolder.rvPersonInformation.getTag())) {
                Glide.with(mContext).load(API.BASE_URL + imgurl)
                        .bitmapTransform(new GlideCircleTransform(mContext))
                        .error(R.drawable.grid_def_online_favicon)
                        .placeholder(R.drawable.grid_def_online_favicon)
                        .crossFade(500)
                        .into(myViewHolder.ivFavicon);
            }
        }

/*        RxView.clicks(myViewHolder.layoutCall)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new rx.Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {
                        if (Constant.hasNetWork) {
                            if (!Utils.isCalling()) {
                                User user = mUserList.get(position);
                                if (!mUserList.get(position).username.equals(userName)) {
                                    Intent intent = CallActivity.getIntent(mContext, user.username, user.imgurl, user.name, CallActivity.TYPE_INVITING);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);
                                } else {
                                    Toast.makeText(getContext(), getContext().getString(R.string.grid_call_self), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.grid_call_busy),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    getContext().getResources().getString(R.string.grid_network_setting),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
        myViewHolder.layoutCall.setOnClickListener(view -> {
            User user1 = mUserList.get(position);
            cn.aorise.webrtc.chat.User user2 = new cn.aorise.webrtc.chat.User();
            user2.setUserName(user1.username);
            user2.setNickName(user1.name);
            user2.setUserIcon(user1.imgurl);
//            user2.setStatus(user1.status);
            ChatClient.getInstance().getSignalManager().call(mContext, user2, new SignalManager.PushCallBack() {
                @Override
                public void onSuccess() {
                    ((GridBaseActivity)mContext).showToast("推送成功");
                }

                @Override
                public void onFailure() {
                    ((GridBaseActivity)mContext).showToast("推送失败");
                }
            });

        });
    }

    @Override
    public int getItemCount() {
        return mUserList == null ? 0 : mUserList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFavicon;
        private TextView tvPersonName;
        private TextView tvPersonStatus;
        private ImageView ivStatus;
        private ImageView ivCall;
        private RelativeLayout rvPersonInformation;
        private LinearLayout layoutCall;

        public MyViewHolder(View itemView) {
            super(itemView);
            rvPersonInformation = (RelativeLayout) itemView.findViewById(R.id.rv_person_information);
            ivFavicon = (ImageView) itemView.findViewById(R.id.iv_peron_favicon);
            tvPersonName = (TextView) itemView.findViewById(R.id.tv_person_name);
            tvPersonStatus = (TextView) itemView.findViewById(R.id.tv_person_status);
            ivStatus = (ImageView) itemView.findViewById(R.id.iv_status);
            ivCall = (ImageView) itemView.findViewById(R.id.iv_call_facetime);
            layoutCall = (LinearLayout) itemView.findViewById(R.id.layout_call_facetime);
        }
    }
}
