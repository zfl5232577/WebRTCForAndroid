package cn.aorise.grid.ui.activity;

import org.webrtc.SurfaceViewRenderer;

import cn.aorise.grid.R;
import cn.aorise.webrtc.ui.BaseCallActivity;
import cn.aorise.webrtc.webrtc.PercentFrameLayout;

public class NewCallActivity extends BaseCallActivity {

    @Override
    protected void initView() {
        setContentView(R.layout.activity_new_call);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected SurfaceViewRenderer setSurfaceLocal() {
        return findViewById(R.id.surface_local);
    }

    @Override
    protected SurfaceViewRenderer setSurfaceRemote() {
        return findViewById(R.id.surface_remote);
    }

    @Override
    protected PercentFrameLayout setLayoutLocalVideo() {
        return findViewById(R.id.layout_local_video);
    }

    @Override
    protected PercentFrameLayout setLayoutRemoteVideo() {
        return findViewById(R.id.layout_remote_video);
    }

    @Override
    protected void updateCallView() {

    }

}
