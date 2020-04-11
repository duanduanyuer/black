package dlpspring.framework.beans.support;

import dlpspring.framework.beans.config.DLPBeanDefinition;
import dlpspring.framework.context.support.DLPAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 兜底的beanFactory默认实现，继承AbstractApplicationContext
 * @Author duanliping
 * @Date 2020/4/6
 **/
public class DLPDefaultListableBeanFactory extends DLPAbstractApplicationContext {

    public final Map<String, DLPBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
}
