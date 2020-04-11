package dlpspring.framework.beans;

/**
 * 接口规范 单例工厂的顶层设计
 */
public interface DLPBeanFactory {

    /**
     * 单例对象全局访问点
     * @param beanName
     * @return
     */
    Object getBean(String beanName);

    Object getBean(Class<?> beanClass);
}
