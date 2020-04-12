package dlpspring.framework.aop;

import com.alibaba.fastjson.JSONObject;
import dlpspring.framework.aop.intercept.DLPMethodInvocation;
import dlpspring.framework.aop.support.DLPAdvisedSupport;
import dlpspring.framework.webmvc.servlet.DLPModelAndView;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPJdkDynamicAopProxy implements DLPAopProxy, InvocationHandler {

    private DLPAdvisedSupport advised;
    public DLPJdkDynamicAopProxy(DLPAdvisedSupport config) {
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.advised.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, this.advised.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> interceptorsAndDynamicMethodMatchers = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method,this.advised.getTargetClass());
        DLPMethodInvocation invocation = new DLPMethodInvocation(proxy,this.advised.getTarget(),method,args,this.advised.getTargetClass(),interceptorsAndDynamicMethodMatchers);
        return invocation.proceed();
    }
}
