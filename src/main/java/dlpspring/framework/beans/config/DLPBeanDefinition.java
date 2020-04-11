package dlpspring.framework.beans.config;

import lombok.Data;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/6
 **/
@Data
public class DLPBeanDefinition {
    public String beanName;  //com.jd.**.Test
    private boolean isLazyInit;
    private String factoryBeanName; //在bean工厂里的名字 test
    private boolean isSingleton=true; //默认单例
}
