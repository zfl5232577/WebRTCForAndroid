package cn.aorise.grid.module.cache;

/**
 * Author: gaoxu
 * TIME: 2017/9/7
 * Description: This is ICache
 * Function:
 */

public interface ICache {

    void saveString(String key, String value);

    String loadString(String key);

    void saveObject(String key, Object value);

    Object loadObject(String object);

    void removeString(String key);
}
