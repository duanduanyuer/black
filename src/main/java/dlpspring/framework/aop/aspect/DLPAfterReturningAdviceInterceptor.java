package dlpspring.framework.aop.aspect;

import dlpspring.framework.aop.intercept.DLPMethodInterceptor;
import dlpspring.framework.aop.intercept.DLPMethodInvocation;

import java.lang.reflect.Method;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPAfterReturningAdviceInterceptor extends DLPAbstractAspectAdvice implements DLPAdvice,DLPMethodInterceptor {
    private DLPJoinPoint joinPoint;
    public DLPAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(DLPMethodInvocation invocation) throws Throwable {
        Object returnValue = invocation.proceed();
        this.joinPoint = invocation;
        this.afterReturning(returnValue, invocation.getMethod(), invocation.getThis());
//        System.out.println("hhh after");
        return returnValue;
    }
    public Object afterReturning(Object returnValue, Method method, Object aThis) throws Exception {
        return super.invokeAdviceMethod(this.joinPoint,returnValue, null);
    }
}
