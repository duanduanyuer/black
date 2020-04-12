package dlpspring.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public interface DLPJoinPoint {
    Object getThis();

    Object[] getArguments();

    Method getMethod();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}
