package dlpspring.framework.beans.config;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/11
 **/
public class DLPBeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception{
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception{
        return bean;
    }
}
