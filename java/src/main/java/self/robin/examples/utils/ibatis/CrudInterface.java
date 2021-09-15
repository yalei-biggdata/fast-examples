package self.robin.examples.utils.ibatis;

import java.util.List;

public interface CrudInterface {

    int insert(String id, Object param);

    int update(String id, Object param);

    int delete(String id, Object param);

    int delete(String sqlStr, Object[] params);

    <T> T selectOne(String id, Object param, Class<T> resultType);

    <T> T selectOne(String sqlStr, Object[] params, Class<T> resultType);

    <T> List<T> selectList(String id, Object param, Class<T> resultType);

    <T> List<T> selectList(String sqlStr, Object[] params, Class<T> resultType);

    <T, E> T select(String id, Object param, Class<T> resultType, Class<E> elementType);

}
