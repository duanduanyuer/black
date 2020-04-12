package dlpspring.framework.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
public class DLPView {

    public final String DEFAULT_CONTENT_TYPE="text/html;charset=utf-8";
    private File viewFile;

    public DLPView(File viewFile) {
        this.viewFile = viewFile;
    }

    public void render(Map<String, ?> model,
                       HttpServletRequest req, HttpServletResponse resp) throws Exception{
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");
        String line = null;
        while(null != (line = ra.readLine())){
            line = new String(line.getBytes("ISO-8859-1"),"utf-8");
            Pattern pattern = Pattern.compile("￥\\{^\\}+\\}",Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                String paramName = matcher.group();
                paramName.replaceAll("$\\{|\\}","");
                Object paramValue = model.get(paramName);
                if(null == paramValue){
                    continue;
                }
                line = matcher.replaceFirst(paramValue.toString());
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }
        resp.setCharacterEncoding("utf-8");
//        resp.setContentType(DEFAULT_CONTENT_TYPE);
        resp.getWriter().write(sb.toString());
    }
}
