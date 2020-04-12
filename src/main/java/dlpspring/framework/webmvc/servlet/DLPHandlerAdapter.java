package dlpspring.framework.webmvc.servlet;

import dlpspring.framework.annotation.DLPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPHandlerAdapter {
    public boolean supports(Object handler){
        return (handler instanceof DLPHandlerMapping);
    }

    public DLPModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        DLPHandlerMapping handlerMapping = (DLPHandlerMapping) handler;
        //形参列表 参数名字作为key，位置作为值
        Map<String, Integer> paramIndexMapping = new HashMap<>();
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof DLPRequestParam) {
                    String paramName = ((DLPRequestParam) a).value();
                    if (!"".equals(paramName)) {
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }
        Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
        //实参列表
        Object[] paramValues = new Object[paramTypes.length];
        for (int i = 0; i < paramValues.length; i++) {
            Class paramType = paramTypes[i];
            if (paramType == HttpServletRequest.class || paramType == HttpServletResponse.class) {
                paramIndexMapping.put(paramType.getName(), i);
            }
        }

        Map<String, String[]> params = request.getParameterMap();

        for(Map.Entry<String, String[]> param:params.entrySet()){
            String value = Arrays.toString(param.getValue())
                    .replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", ",");
            if(!paramIndexMapping.containsKey(param.getKey())){
                continue;
            }
            int index = paramIndexMapping.get(param.getKey());
            paramValues[index] = caseStringValue(value, paramTypes[index]);
        }
        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int reqIndex = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = request;
        }
        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int respIndex = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = response;
        }
        try {
            Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);
            if(result == null || result instanceof Void){
                return null;
            }
            boolean isModelAndView = handlerMapping.getMethod().getReturnType() == DLPModelAndView.class;
            if(isModelAndView){
                return (DLPModelAndView)result;
            }
        } catch (IllegalAccessException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object caseStringValue(String value, Class<?> paramType) {

        if(Integer.class == paramType){
            return Integer.valueOf(value);
        }
        else if(Double.class == paramType){
            return Double.valueOf(value);
        }
        else if(String.class == paramType){
            return value;
        }else{
            if(value != null){
                return value;
            }
            return null;
        }
    }

}
