package dlpspring.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public abstract class DLPAbstractAspectAdvice implements DLPAdvice{
    private Method aspectMethod;
    private Object aspectTarget;
    public DLPAbstractAspectAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    public Object invokeAdviceMethod(DLPJoinPoint joinPoint, Object returnValue, Throwable tw) throws Exception{
//        System.out.println("this.aspectMethod==="+this.aspectMethod);
        Class<?>[] paramTypes = this.aspectMethod.getParameterTypes();
        if(null == paramTypes || paramTypes.length == 0){
            return this.aspectMethod.invoke(aspectTarget);
        }else{
//            Class<?> [] paramTypes = this.aspectMethod.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            for(int i=0;i<paramTypes.length;i++){
                if(paramTypes[i] == DLPJoinPoint.class){
                    args[i] = joinPoint;
                }else if(paramTypes[i] == Throwable.class){
                    args[i] = tw;
                }else if(paramTypes[i] == Object.class){
                    args[i]=returnValue;
                }
            }
            return this.aspectMethod.invoke(aspectTarget,args);
        }
    }

}
