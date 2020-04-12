package dlpspring.framework.aop;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public interface DLPAopProxy {

    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
