package dlpspring.framework.webmvc.servlet;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
@Data
public class DLPHandlerMapping {

    private Object controller;
    private Method method;
    private Pattern pattern; //url正则匹配

    public DLPHandlerMapping(Pattern pattern, Object controller, Method method) {
        this.pattern = pattern;
        this.controller = controller;
        this.method = method;
    }
}
