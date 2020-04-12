package dlpspring.framework.aop;

import dlpspring.framework.aop.support.DLPAdvisedSupport;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPCglibAopProxy implements DLPAopProxy {
    public DLPCglibAopProxy(DLPAdvisedSupport config) {
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
