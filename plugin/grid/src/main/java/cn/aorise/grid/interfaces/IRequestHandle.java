package cn.aorise.grid.interfaces;

/**
 * 网络请求回调
 * Created by tangjy on 2017/3/7.
 */
public interface IRequestHandle<T> {

    /**
     * 下载错误
     *
     * @param e
     */
    void handleError(Throwable e);

    /**
     * 下载成功
     *
     * @param t
     */
    void handleSuccess(T t);
}
