package cn.aorise.grid.module.network;

import java.util.List;

import cn.aorise.grid.BuildConfig;
import cn.aorise.grid.module.network.entity.response.Region;
import cn.aorise.grid.module.network.entity.response.Session;
import cn.aorise.grid.module.network.entity.response.User;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import io.reactivex.Observable;

/**
 * Created by pc on 2017/3/8.
 */
public interface GridApiService {
    @FormUrlEncoded
    @POST("api/user/login")
    Observable<Response<ResponseBody>> login(@Field("username") String username,
                                             @Field("password") String password);


    @POST("api/user/logout")
    Observable<Response<ResponseBody>> logout();

    @GET("api/user/session")
    Observable<Session> getSession();

    //获取用户所处辖区
    @GET("api/region/root")
    Observable<Region> getCurrentPopedom();

    //获取某辖区【id】用户列表
    @GET("api/region/member/all")
    Observable<List<User>> getUser(@Query("rid") int rid);

    //获取某辖区的子辖区列表
    @GET("api/region/sub-region/all")
    Observable<List<Region>> getChildPopedom(@Query("rid") int rid);


    class Factory {
        public Factory() {
        }

        public static GridApiService create() {
            return RetrofitFactory.getInstance().create(BuildConfig.MOCK_MODE, GridApiService.class, API.BASE_URL);
        }
    }
}
