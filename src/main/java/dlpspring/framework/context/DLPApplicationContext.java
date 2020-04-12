package dlpspring.framework.context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dlpspring.framework.annotation.DLPAutowired;
import dlpspring.framework.annotation.DLPController;
import dlpspring.framework.annotation.DLPService;
import dlpspring.framework.aop.DLPAopProxy;
import dlpspring.framework.aop.DLPCglibAopProxy;
import dlpspring.framework.aop.DLPJdkDynamicAopProxy;
import dlpspring.framework.aop.config.DLPAopConfig;
import dlpspring.framework.aop.support.DLPAdvisedSupport;
import dlpspring.framework.beans.DLPBeanFactory;
import dlpspring.framework.beans.DLPBeanWrapper;
import dlpspring.framework.beans.config.DLPBeanDefinition;
import dlpspring.framework.beans.config.DLPBeanPostProcessor;
import dlpspring.framework.beans.support.DLPBeanDefinitionReader;
import dlpspring.framework.beans.support.DLPDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: IOC DI MVC AOP
 * ioc容器：beanDefinitionMap
 * @Author duanliping
 * @Date 2020/4/6
 **/
public class DLPApplicationContext extends DLPDefaultListableBeanFactory implements DLPBeanFactory {

    private String[] locations;
    private DLPBeanDefinitionReader reader;
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private Map<String, Object> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public DLPApplicationContext(String... locations) {
        this.locations = locations;
        refresh();
    }

    @Override
    public void refresh() {
        //IOC初始化步骤
        //1、定位 配置文件，把所有类名装载到一个list
        reader = new DLPBeanDefinitionReader(locations);
        //2、扫描相关的类，封装成BeanDefinition对象，放到BeanDefinitionList
        List<DLPBeanDefinition> beanDefinitionList = reader.loadBeanDefinitions();
        //3、注册，把配置信息放到ioc容器（伪IOC容器，真正的叫beanWrapper）
        doRegitsterBeanDefinition(beanDefinitionList);
        //4、把不是延时加载的类 提前初始化
        doAutoWired();
    }

    /**
     * 只处理非延迟加载情况
     */
    private void doAutoWired() {
        for(Map.Entry<String, DLPBeanDefinition> beanDefinitionEntry:super.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();
            if(beanDefinitionEntry.getValue().isLazyInit()){
                continue;
            }
            try {
                getBean(beanName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void doRegitsterBeanDefinition(List<DLPBeanDefinition> beanDefinitionList) {
        for(DLPBeanDefinition beanDefinition:beanDefinitionList){
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }
    @Override
    public Object getBean(Class<?> beanClass) {
        return getBean(beanClass.getName());
    }

    /**
     * 根据ioc容器里的beanName找到对应的对象实例 dlpService
     * @param beanName
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(String beanName) {
        try{
            DLPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
            Object instance = instantiateBean(beanName, beanDefinition);
            //工厂模式+策略模式
            DLPBeanPostProcessor postProcessor = new DLPBeanPostProcessor();
            postProcessor.postProcessBeforeInitialization(instance, beanName); //应该在初始化之前？
            //doCreateBean 为什么先初始化后注入？解决循环依赖
            //instantiateBean 初始化实体对象 beanName,beanDefinition
            DLPBeanWrapper beanWrapper = new DLPBeanWrapper(instance);
            //把beanWrapper保存到ioc容器中
//        if(factoryBeanInstanceCache.containsKey(beanName)){
//            throw new Exception("The" + beanName + "is exists!");
            this.factoryBeanInstanceCache.put(beanName, beanWrapper);
            postProcessor.postProcessAfterInitialization(instance, beanName);
            //populateBean 注入 把bd转换成beanWrapper 缓存
            populateBean(beanName, new DLPBeanDefinition(), beanWrapper);
            DLPBeanWrapper bw = (DLPBeanWrapper)this.factoryBeanInstanceCache.get(beanName);
            return bw.getWrappedInstance();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void populateBean(String beanName, DLPBeanDefinition beanDefinition, DLPBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrappedInstance();
//        System.out.println(JSONObject.toJSON(beanWrapper));
        Class<?> clazz = beanWrapper.getWrappedClass();
        //判断只有加了注解的类，才执行依赖注入
        if(!(clazz.isAnnotationPresent(DLPController.class) || clazz.isAnnotationPresent(DLPService.class))){
            return;
        }
        //获得所有的fields
        Field[] fields = clazz.getDeclaredFields();
        for(Field field:fields){
            if(!field.isAnnotationPresent(DLPAutowired.class)){
                continue;
            }
            DLPAutowired autowired = field.getAnnotation(DLPAutowired.class); //简化只判断autowired
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)){
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true); //强制访问
            try {
                //为什么这里会有null 接口的
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){continue;}
                DLPBeanWrapper bw = (DLPBeanWrapper)this.factoryBeanInstanceCache.get(autowiredBeanName);

                field.set(instance,bw.getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    private Object instantiateBean(String beanName, DLPBeanDefinition beanDefinition) {
        //1、拿到要实例化的对象的类名

        String className = beanDefinition.getBeanName();
        //2、反射实例化，得到一个对象
        Object instance = new Object();
        Class<?> clazz = null;
        try{
            //默认单例的了
            if(this.factoryBeanInstanceCache.containsKey(className)){
                instance = this.factoryBeanInstanceCache.get(className);
                clazz = Class.forName(className);
            }else{
                clazz = Class.forName(className);
                instance = clazz.newInstance();
                DLPAdvisedSupport config = instantionAopConfig(beanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);
                if(config.pointCutMatch()){ //用类名匹配pointCut规则，创建代理对象
                    instance = createProxy(config).getProxy();
                }
//                this.factoryBeanInstanceCache.put(className, instance);
                this.factoryBeanInstanceCache.put(beanDefinition.getFactoryBeanName(), instance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //3、把这个对象封装到beanwrapper中  factoryBeanInstanceCache
        DLPBeanWrapper beanWrapper = new DLPBeanWrapper(instance);
        beanWrapper.setWrappedClass(clazz);
        //4、把beanWrapper存在ioc容器，判断单例，如果单例，存singletonObjects
        return beanWrapper;
    }

    private DLPAopProxy createProxy(DLPAdvisedSupport config) {
        Class target = config.getTargetClass();
        if(target.getInterfaces().length>0){
            return new DLPJdkDynamicAopProxy(config);
        }
        return new DLPCglibAopProxy(config);
    }

    private DLPAdvisedSupport instantionAopConfig(DLPBeanDefinition beanDefinition) {
        DLPAopConfig config = new DLPAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new DLPAdvisedSupport(config);
    }

    /**
     * 提供给dispatcher用，不能直接给map 最少知道原则
     * @return
     */
    public String[] getBeanDefinitionNames(){
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount(){
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }
}
