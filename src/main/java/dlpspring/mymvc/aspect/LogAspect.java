package dlpspring.mymvc.aspect;

import dlpspring.framework.aop.aspect.DLPJoinPoint;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/

public class LogAspect {
//    public void before(DLPJoinPoint joinPoint){
////        joinPoint.setUserAttribute("startTime_"+joinPoint.getMethod().getName(),System.currentTimeMillis());
//        System.out.println("before");
//        //往对象里面记录调用的开始时间
//    }
    public void before(){
        System.out.println("before");
        //系统当前时间-记录的开始时间=方法调用消耗的时间 监测方法性能
    }
    public void after(){
        System.out.println("after");
        //系统当前时间-记录的开始时间=方法调用消耗的时间 监测方法性能
    }
    public void afterThrowing(){
        System.out.println("throw");
        //异常监测 拿到异常信息
    }
}

