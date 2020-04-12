package dlpspring.framework.aop.support;

import com.alibaba.fastjson.JSONObject;
import dlpspring.framework.aop.aspect.DLPAfterThrowingAdviceInterceptor;
import dlpspring.framework.aop.aspect.DLPAfterReturningAdviceInterceptor;
import dlpspring.framework.aop.aspect.DLPMethodBeforeAdviceInterceptor;
import dlpspring.framework.aop.config.DLPAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPAdvisedSupport {

    private Class<?> targetClass;
    private Object target;
    private DLPAopConfig config;
    private Pattern pointCutClassPattern;

    private Map<Method, List<Object>> methodCache;
    public DLPAdvisedSupport(DLPAopConfig config) {
        this.config = config;
    }

    public Class<?> getTargetClass(){
        return this.targetClass;
    }

    private void parse() {
        String pointCut = config.getPointCut().replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        //dlpspring.mymvc.service..*Service
        String pointCutForClassRegix = pointCut.substring(0, pointCut.lastIndexOf("\\(")-4);
        pointCutClassPattern = Pattern.compile("class "+pointCutForClassRegix.substring(
                pointCutForClassRegix.lastIndexOf(" ")+1));
        try{
            methodCache = new HashMap<>();
            Pattern pattern = Pattern.compile(pointCut);
            Class aspectClass = Class.forName(this.config.getAspectClass()); //拿到切面类
            Map<String, Method> aspectMethods = new HashMap<>();
            for(Method m: aspectClass.getMethods()){
                aspectMethods.put(m.getName(), m);
            }

            for(Method m:this.targetClass.getMethods()){
                String methodString = m.toString();
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pattern.matcher(methodString);
                if(matcher.matches()){
                    //把每一个方法包装成MethodIntercepter
                    //before
                    //after
                    //afterThrowing
                    //执行器链
                    List<Object> advices = new LinkedList<>();
                    if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))){
                        //创建一个advices对象
                        advices.add(new DLPMethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()),aspectClass.newInstance()));
                    }
                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
                        //创建一个advices对象
                        advices.add(new DLPAfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()),aspectClass.newInstance()));
                    }
                    if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))){
                        //创建一个advices对象
                        DLPAfterThrowingAdviceInterceptor throwingAdvice =
                                new DLPAfterThrowingAdviceInterceptor(aspectMethods.get(config.getAspectAfterThrow()),aspectClass.newInstance());
                        throwingAdvice.setThrowName(config.getAspectAfterThrowingName());
                        advices.add(throwingAdvice);

                    }
                    methodCache.put(m,advices);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public Object getTarget(){
        return this.target;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws NoSuchMethodException {
        List<Object> cached = methodCache.get(method);
        if(cached == null){
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            //底层逻辑？对代理方法进行一个兼容处理
            cached = methodCache.get(m);
            this.methodCache.put(m, cached);
        }
        return cached;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public void setTarget(Object instance) {
        this.target = instance;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}
