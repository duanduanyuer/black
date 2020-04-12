package dlpspring.framework.beans.support;

import dlpspring.framework.beans.config.DLPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Description: 加载配置信息到beanDefinition顶层实现
 * 源码中是把beanDefinition都放到BeanDefinitionRegistry里的，这里直接把load方法返回一个beanDefinition的list，
 * 比源码稍稍简化
 * @Author duanliping
 * @Date 2020/4/6
 **/
public class DLPBeanDefinitionReader {

    private Properties config = new Properties();
    private final String SCAN_PACKAGE = "scanPackage";
    private List<String> registryBeanClass = new ArrayList<>(); //所有目录下的className

    public DLPBeanDefinitionReader(String[] locations) {
        //通过url定位找到其所对应的文件，然后转换为文件流
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    /**
     * 把配置文件中所有扫描到的配置信息转换为BeanDefinition，以便于ioc操作方便
     * @return
     */
    public List<DLPBeanDefinition> loadBeanDefinitions(){
        List<DLPBeanDefinition> result = new ArrayList<>();
        try{
            for(String className:registryBeanClass){
                Class<?> beanClass = Class.forName(className);
                if(beanClass.isInterface()){continue;}
                //beanName有三种情况：
                //1、默认是类名首字母小写
                //2、自定义名字
                //3、接口注入
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
                result.add(doCreateBeanDefinition(beanClass.getName(),beanClass.getName()));
                Class<?> [] interfaces = beanClass.getInterfaces();
                for(Class<?> i:interfaces){
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private DLPBeanDefinition doCreateBeanDefinition(String beanName, String className){
        try{
            Class<?> beanClass = Class.forName(className);
            //有可能是接口，用他的实现类作为beanClassName
            if(beanClass.isInterface()){
                return null;
            }
            DLPBeanDefinition beanDefinition = new DLPBeanDefinition();
            beanDefinition.setBeanName(className);//com.jd.***.Test
            beanDefinition.setFactoryBeanName(beanName); //test
            return beanDefinition;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
    private void doScanner(String property) {
        //this.getClass().getClassLoader().getResource()??
        URL url = this.getClass().getResource("/" + property.replaceAll("\\.", "/"));
        File classFile = new File(url.getFile());
        for(File file : classFile.listFiles()){
            if(file.isDirectory()){
                doScanner(property + "." +file.getName());
            }
            if(!file.getName().endsWith(".class")){
                continue;
            }

            String className = property + "." + file.getName().replace(".class", "");
            registryBeanClass.add(className);
        }
    }
    public Properties getConfig(){
        return this.config;
    }
}
