package dlpspring.framework.context;

/**
 * 通过解耦方式获得ioc容器的顶层设计
 * 后面讲通过一个监听器去扫描所有的类，只要实现了此接口
 * 实现applicationContext自动注入到目标类 观察者模式？
 */
public interface DLPApplicationContextAware {

    void setApplicationContext(DLPApplicationContext context);
}
