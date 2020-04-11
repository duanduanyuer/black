package dlpspring.framework.servlet;

import dlpspring.framework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import static jdk.nashorn.api.scripting.ScriptUtils.convert;

/**
 * @Description: TODO  http://localhost:8080/dlp/hello?word=dajiahao
 * @Author duanliping
 * @Date 2020/4/4
 **/
public class DLPDispatcherNewServlet extends HttpServlet {

    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> ioc = new HashMap<String, Object>();
//    private Map<String, Method> handlerMapping = new HashMap<String, Method>();
    private List<HandlerMapping> handlerMapping = new ArrayList<HandlerMapping>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6、调用
        try{
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getWriter().write("500 error");
            System.out.println(e.toString());
            return;
        }
    }

    private Object doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        uri = uri.replaceAll(contextPath,"").replaceAll("/+", "/");
        HandlerMapping handlerMapping = getHandler(req);
        if(null == handlerMapping){
            resp.getWriter().write("404 error");
            return null;
        }
        Class<?> [] paramTypes = handlerMapping.getParamTypes();
        Object[] paramValues = new Object[paramTypes.length];
        Map<String, String[]> params = req.getParameterMap();
        for(Map.Entry<String, String[]> param:params.entrySet()){
            String value = Arrays.toString(param.getValue())
                    .replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", ",");
            if(!handlerMapping.paramIndexMapping.containsKey(param.getKey())){
                continue;
            }
            int index = handlerMapping.paramIndexMapping.get(param.getKey());
            paramValues[index] = convertParam(paramTypes[index] ,value);
        }
        if(handlerMapping.paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int reqIndex = handlerMapping.paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }
        if(handlerMapping.paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int respIndex = handlerMapping.paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }
        try {
            Object result = handlerMapping.method.invoke(handlerMapping.controller, paramValues);
            if(result == null || result instanceof Void){
                return null;
            }
            return result;
        } catch (IllegalAccessException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object convertParam(Class<?> type, String value) {
        if(Integer.class == type){
            return Integer.valueOf(value);
        }else if(Double.class == type){
            return Double.valueOf(value);
        }
        return value;
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if(handlerMapping.isEmpty()){return null;}
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        uri = uri.replaceAll(contextPath,"").replaceAll("/+", "/");
        for(HandlerMapping handler : this.handlerMapping){
            if(uri.equals(handler.getUrl())){
                return handler;
            }
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        
        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        
        //3、实例化相关的类
        doInstance();
        
        //4、完成依赖注入
        doAutowied();
        
        //5、初始化HandlerMapping
        doInitHandlerMapping();
        System.out.println("dlp spring framework init finish!!");
    }

    /**
     * 初始化url和method一一对应关系
     */
    private void doInitHandlerMapping() {
        if(ioc.isEmpty()){
            return;
        }
        for(Map.Entry<String, Object> entry:ioc.entrySet()){
            Class clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(DLPController.class)){
                continue;
            }
            String baseUrl = "";
            if(clazz.isAnnotationPresent(DLPRequestMapping.class)){
                DLPRequestMapping mapping = (DLPRequestMapping)clazz.getAnnotation(DLPRequestMapping.class);
                baseUrl = mapping.value();
            }
            for(Method method:clazz.getMethods()){
                if(!method.isAnnotationPresent(DLPRequestMapping.class)){
                    continue;
                }
                DLPRequestMapping dlpRequestMapping = method.getAnnotation(DLPRequestMapping.class);
                String url = (baseUrl + "/" + dlpRequestMapping.value()).replaceAll("/+", "/");
//                handlerMapping.put(url, method);
                this.handlerMapping.add(new HandlerMapping(url, method, entry.getValue()));
            }
        }
    }

    private void doAutowied() {
        if(ioc.isEmpty()){
            return;
        }
        for(Map.Entry<String, Object> entry: ioc.entrySet()){
            //所有的特定的字段，包括private protected default
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for(Field field:fields){
                if(!field.isAnnotationPresent(DLPAutowired.class)){
                    continue;
                }
                DLPAutowired dlpAutowired = field.getAnnotation(DLPAutowired.class);
                String beanName = dlpAutowired.value().trim();
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if(classNames.isEmpty()){
            return;
        }
        for(String className:classNames){
            try {
                Class clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(DLPController.class)){
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                }else if(clazz.isAnnotationPresent(DLPService.class)){
                    //2、看注解上是否有定义
                    DLPService service = (DLPService)clazz.getAnnotation(DLPService.class);
                    String beanName = service.value();
                    if("".equals(service.value())){
                        //1、默认类名首字母小写
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //3、接口不能直接实例化，要实例化实现类 接口全类名做key
                    for(Class i:clazz.getInterfaces()){
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The beanName is exsits!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                }else{
                    continue;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String toLowerFirstCase(String simpleName) {
//        if(StringUtils.isBlank(simpleName)){
//            return "";
//        }
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String property) {
        System.out.println(this.getClass().getClassLoader().getClass().getSimpleName());
        URL url = this.getClass().getClassLoader().getResource("/" + property.replaceAll("\\.", "/"));
        File classFile = new File(url.getFile());
        for(File file : classFile.listFiles()){
            if(file.isDirectory()){
                doScanner(property + "." +file.getName());
            }
            if(!file.getName().endsWith(".class")){
                continue;
            }

            String className = property + "." + file.getName().replace(".class", "");
            classNames.add(className);
        }
    }

    private void doLoadConfig(String config) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(config);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class HandlerMapping {

        private String url;
        private Method method;
        private Object controller;
        private Class<?>[] paramTypes;
        //形参列表 参数名字作为key，位置作为值
        private Map<String, Integer> paramIndexMapping;

        public HandlerMapping(String url, Method method, Object controller) {
            this.url = url;
            this.method = method;
            this.controller = controller;
            this.paramTypes = method.getParameterTypes();
            this.paramIndexMapping = new HashMap<String, Integer>();
            putParamIndexMapping(method);
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        public String getUrl() {
            return url;
        }

        /**
         * 一个字段可以多个注解，一个方法可以多个参数
         *
         * @param method
         */
        private void putParamIndexMapping(Method method) {
            Annotation[][] pa = method.getParameterAnnotations();
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
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] paramValues = new Object[paramTypes.length];
            for (int i = 0; i < paramValues.length; i++) {
                Class paramType = paramTypes[i];
                if (paramType == HttpServletRequest.class || paramType == HttpServletResponse.class) {
                    paramIndexMapping.put(paramType.getName(), i);
                }
            }

        }
    }
}
