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

    public void before(Method method,Object [] args, Object target) throws Exception{
        //传送给织入的参数
        super.invokeAdviceMethod(this.joinPoint, null, null);
    }

    @Override
    public Object invoke(DLPMethodInvocation mi) throws Throwable {
        //从被织入的代码中拿到，JoinPoint
        this.joinPoint = mi;
        before(mi.getMethod(), mi.getArguments(), mi.getThis());
//        System.out.println("hhh before");
        return mi.proceed();
    }
}
