package dlpspring.framework.webmvc.servlet;

import java.io.File;
import java.util.Locale;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPViewResolver {

    private File templateRootDir;
//    private String viewName;
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    public DLPViewResolver(String templateRoot){
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateRootDir = new File(templateRootPath);
    }
    public DLPView resolveViewName(String viewName, Locale locale) throws Exception{
        if(null == viewName || "".equals(viewName.trim())){
            return null;
        }
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)?viewName:(viewName + DEFAULT_TEMPLATE_SUFFIX);
        File file = new File((templateRootDir.getPath()+"/"+viewName).replaceAll("/+","/"));
        return new DLPView(file);
    }

//    public String getViewName() {
//        return viewName;
//    }
}
