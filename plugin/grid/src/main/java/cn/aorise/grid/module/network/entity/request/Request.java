package cn.aorise.grid.module.network.entity.request;

import java.io.Serializable;

import cn.aorise.common.core.util.GsonUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by tangjy on 2017/9/5.
 */
public class Request implements Serializable {
    public Request() {
    }

    public String toJson() {
        return GsonUtils.toJson(this);
    }


    @Deprecated
    public RequestBody toRequestBody() {
        return RequestBody.create(MediaType.parse("application/json"), this.toJson());
    }
}
