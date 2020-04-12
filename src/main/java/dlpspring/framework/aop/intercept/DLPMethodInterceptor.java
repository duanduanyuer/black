package dlpspring.framework.aop.intercept;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public interface DLPMethodInterceptor {
    Object invoke(DLPMethodInvocation invocation) throws Throwable;
}
