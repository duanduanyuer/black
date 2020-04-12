package dlpspring.framework.webmvc.servlet;

import dlpspring.framework.annotation.DLPController;
import dlpspring.framework.annotation.DLPRequestMapping;
import dlpspring.framework.context.DLPApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/11
 **/
@Slf4j
public class DLPDispatcherServlet extends HttpServlet {

    private DLPApplicationContext context;

    private final String CONTEXT_CONFIG_LOCATION = "application.properties";

    private List<DLPHandlerMapping> handlerMappings = new ArrayList<>();
    private Map<DLPHandlerMapping, DLPHandlerAdapter> handlerAdapterMap = new HashMap<>();
    private List<DLPViewResolver> viewResolvers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.doDispatch(req, resp);
        } catch (Exception e) {
//            resp.getWriter().write("500 error..."+e.getStackTrace());
//            new DLPModelAndView("500");
            try {
                processDispatchResult(req, resp, new DLPModelAndView("500"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //1、通过从request中拿到url匹配一个handlerMapping
        DLPHandlerMapping handler = getHandler(req);
        if(null == handler){
            processDispatchResult(req, resp, new DLPModelAndView("404"));
            return;
        }
        //2、准备调用前的参数
        DLPHandlerAdapter handlerAdapter = getHandlerAdapter(handler);
        //3、真正调用方法 modelAndView存储了要传给页面的值和页面模板的名称
        DLPModelAndView modelAndView = handlerAdapter.handle(req, resp, handler);
        //这一步才是真正的输出
        processDispatchResult(req, resp, modelAndView);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, DLPModelAndView modelAndView) throws Exception {
        //把modelAndView变成一个html outputstream json freemark。。。
        //contextType 暂时只处理html和json
        if(null == modelAndView){
            return;
        }
        //如果modelAndView不是空，做个渲染
        if(this.viewResolvers.isEmpty()){
            return;
        }
        for(DLPViewResolver viewResolver : this.viewResolvers){
            DLPView view = viewResolver.resolveViewName(modelAndView.getViewName(), null);
            view.render(modelAndView.getModel(), req, resp);
        }
        return;
    }

    private DLPHandlerAdapter getHandlerAdapter(DLPHandlerMapping handler) {

        if(this.handlerAdapterMap.isEmpty()){return null;}
        DLPHandlerAdapter handlerAdapter = this.handlerAdapterMap.get(handler);
        if(handlerAdapter.supports(handler)){
            return handlerAdapter;
        }
        return null;
    }

    private DLPHandlerMapping getHandler(HttpServletRequest req) throws Exception{
        if(handlerMappings.isEmpty()){return null;}
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        uri = uri.replaceAll(contextPath,"").replaceAll("/+", "/");
        for(DLPHandlerMapping handler : this.handlerMappings){
            try{
                Matcher matcher = handler.getPattern().matcher(uri);
                if(!matcher.matches()){continue;}
                return handler;
            }catch (Exception e){
                throw e;
            }
        }
        return null;
    }
    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、初始化applicationContext
        context = new DLPApplicationContext(CONTEXT_CONFIG_LOCATION);
        //2、初始化spring mvc 9大组件
        initStrategies(context);
    }

    private void initStrategies(DLPApplicationContext context) {
        //多文件上传组件
        initMultipartResolver(context);
        //初始化本地语言环境
        initLocaleResolver(context);
        //初始化模板处理器
        initThemeResolver(context);
        //handlerMapping **
        initHandlerMappings(context);
        //初始化参数适配器 **
        initHandlerAdapters(context);
        //初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        initRequestToViewNameTranslator(context);
        //初始化视图转换器 **
        initViewResolvers(context);
        //flash映射管理器 参数缓存器
        initFlashMapManager(context);
        
    }

    private void initFlashMapManager(DLPApplicationContext context) {
    }

    private void initViewResolvers(DLPApplicationContext context) {
        //拿到模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String path = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(path);
        for(File file:templateRootDir.listFiles()){
            this.viewResolvers.add(new DLPViewResolver(templateRoot));
        }

    }

    private void initRequestToViewNameTranslator(DLPApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(DLPApplicationContext context) {
    }

    /**
     * 把一个request请求变成一个handler，参数都是字符串的，自动配到handler中的形参
     * 要拿到handlermapping才行 有几个handlerMapping就有几个handlerAdapter
     * @param context
     */
    private void initHandlerAdapters(DLPApplicationContext context) {
        for(DLPHandlerMapping handlerMapping:this.handlerMappings){
            this.handlerAdapterMap.put(handlerMapping, new DLPHandlerAdapter());
        }
    }

    private void initThemeResolver(DLPApplicationContext context) {
    }

    private void initHandlerMappings(DLPApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        try{
            for(String beanName:beanNames){
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();
                if(!clazz.isAnnotationPresent(DLPController.class)){
                    continue;
                }
                String baseUrl = "";
                if(clazz.isAnnotationPresent(DLPRequestMapping.class)){
                    DLPRequestMapping mapping = (DLPRequestMapping)clazz.getAnnotation(DLPRequestMapping.class);
                    baseUrl = mapping.value();
                }
                Method[] methods = clazz.getMethods();
                for(Method method:methods){
                    if(!method.isAnnotationPresent(DLPRequestMapping.class)){
                        continue;
                    }
                    DLPRequestMapping dlpRequestMapping = method.getAnnotation(DLPRequestMapping.class);
                    String regex = (baseUrl + "/" + dlpRequestMapping.value().replaceAll("\\*",".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new DLPHandlerMapping(pattern, controller,method));
                    log.info(method.getName());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initLocaleResolver(DLPApplicationContext context) {
    }

    private void initMultipartResolver(DLPApplicationContext context) {
    }
}
