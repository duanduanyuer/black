package dlpspring.framework.webmvc.servlet;

import java.util.Map;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPModelAndView {

    private String viewName;
    private Map<String,?> model;

    public DLPModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public DLPModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
