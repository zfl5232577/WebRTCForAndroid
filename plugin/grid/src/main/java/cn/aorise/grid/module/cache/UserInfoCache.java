package cn.aorise.grid.module.cache;

import cn.aorise.common.core.util.CacheUtils;

import static cn.aorise.grid.interfaces.CacheConsts.UserConsts.SET_COOKIE;

/**
 * Author: gaoxu
 * TIME: 2017/9/7
 * Description: This is UserInfoCache
 * Function:用户信息缓存(暂时只用来存储session)
 */
public class UserInfoCache {


    private UserInfoCache() {
    }

//    private static final CacheImpl INSTANCE = new CacheImpl();

//  public static void saveLoginBean(UserData bean) {
//    saveUpUserData(bean);
//  }

//    public static void saveSetCookie(String cookie) {
//        INSTANCE.saveString(SET_COOKIE, cookie);
//    }
//
//    public static String getSetCookie() {
//        return INSTANCE.loadString(SET_COOKIE);
//    }
//
//    public static void removeCookie() {
//        INSTANCE.removeString(SET_COOKIE);
//    }

    //
//    private static void saveUpUserData(UserData bean) {
//        INSTANCE.saveObject(USERDATA, bean);
//    }
//
//  public static UserData getUserData() {
//    return (UserData) INSTANCE.loadObject(USERDATA);
//  }


    //-----------------------------------------------------------------------------
    //---------------------------缓存实现-------------------------------------------
    //-----------------------------------------------------------------------------

//    private static final class CacheImpl implements ICache {
//
//        private final ACache mACache = initACache();
//
//        private ACache initACache() {
//            return ACache.get(GridApplication.getContext());
//        }
//
//        private Map<String, SoftReference<Object>> mReferenceMap = new ConcurrentHashMap<>();
//
//        @Override
//        public void saveString(String key, String value) {
//            if (TextUtils.isEmpty(value)) {
//                return;
//            }
//            SoftReference<Object> softReference = new SoftReference<Object>(value);
//            mReferenceMap.put(key, softReference);
//            mACache.remove(key);
//            mACache.put(key, value);
//        }
//
//        @Override
//        public void removeString(String key) {
//            if (mReferenceMap.containsKey(key)) {
//                mReferenceMap.remove(key);
//            }
//            if (!TextUtils.isEmpty(mACache.getAsString(key))) {
//                mACache.remove(key);
//            }
//        }
//
//        @Override
//        public String loadString(String key) {
//            String value = null;
//            if (mReferenceMap.containsKey(key)) {
//                SoftReference<Object> softReference = mReferenceMap.get(key);
//                value = (String) softReference.get();
//                if (value != null) {
//                    return value;
//                }
//            }
//            value = mACache.getAsString(key);
//            SoftReference<Object> softReference = new SoftReference<Object>(value);
//            mReferenceMap.put(key, softReference);
//            return value;
//        }
//
//        @Override
//        public void saveObject(String key, Object value) {
//            SoftReference<Object> softReference = new SoftReference<>(value);
//            mReferenceMap.put(key, softReference);
//            mACache.remove(key);
//            mACache.put(key, (Serializable) value);
//        }
//
//        @Override
//        public Object loadObject(String key) {
//            Object value = null;
//            if (mReferenceMap.containsKey(key)) {
//                SoftReference<Object> softReference = mReferenceMap.get(key);
//                value = softReference.get();
//                if (value != null) {
//                    return value;
//                }
//            }
//            value = mACache.getAsObject(key);
//            SoftReference<Object> softReference = new SoftReference<>(value);
//            mReferenceMap.put(key, softReference);
//            return value;
//        }
//    }

    public static void saveSetCookie(String cookie) {
        CacheUtils.getInstance().put(SET_COOKIE, cookie);
    }

    public static String getSetCookie() {
        return CacheUtils.getInstance().getString(SET_COOKIE);
    }

    public static void removeCookie() {
        CacheUtils.getInstance().remove(SET_COOKIE);
    }
}
