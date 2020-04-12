package dlpspring.framework.aop.aspect;

import dlpspring.framework.aop.intercept.DLPMethodInterceptor;
import dlpspring.framework.aop.intercept.DLPMethodInvocation;

import java.lang.reflect.Method;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPAfterThrowingAdviceInterceptor extends DLPAbstractAspectAdvice implements DLPAdvice,DLPMethodInterceptor {

    private String throwName;
    public DLPAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(DLPMethodInvocation invocation) throws Throwable {
        try{
            return invocation.proceed();
        }catch (Exception e){
            invokeAdviceMethod(invocation,null, e.getCause());
            throw e;
        }
    }

    public void setThrowName(String throwName){
        this.throwName = throwName;
    }
}
