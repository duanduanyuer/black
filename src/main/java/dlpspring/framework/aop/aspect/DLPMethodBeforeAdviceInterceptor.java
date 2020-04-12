package dlpspring.framework.aop.aspect;

import dlpspring.framework.aop.intercept.DLPMethodInterceptor;
import dlpspring.framework.aop.intercept.DLPMethodInvocation;

import java.lang.reflect.Method;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPMethodBeforeAdviceInterceptor extends DLPAbstractAspectAdvice implements DLPAdvice,DLPMethodInterceptor {

    private DLPJoinPoint joinPoint;
    public DLPMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(DLPMethodInvocation invocation) throws Throwable {
        //从被织入的代码中拿到，JoinPoint
        this.joinPoint = invocation;
        before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return invocation.proceed();
    }
    private void before(Method method,Object [] arguments, Object aThis) throws Exception{
        //传送给织入的参数
        super.invokeAdviceMethod(this.joinPoint, null, null);
    }

}
