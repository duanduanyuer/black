package dlpspring.framework.context;

import dlpspring.framework.annotation.DLPAutowired;
import dlpspring.framework.annotation.DLPController;
import dlpspring.framework.annotation.DLPService;
import dlpspring.framework.beans.DLPBeanFactory;
import dlpspring.framework.beans.DLPBeanWrapper;
import dlpspring.framework.beans.config.DLPBeanDefinition;
import dlpspring.framework.beans.support.DLPBeanDefinitionReader;
import dlpspring.framework.beans.support.DLPDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
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
    private Map<String, DLPBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public DLPApplicationContext(String... locations) {
        this.locations = locations;
        refresh();
    }

    @Override
    public void refresh() {
        //IOC初始化步骤
        //1、定位 配置文件
        reader = new DLPBeanDefinitionReader(locations);
        //2、加载配置文件，扫描相关的类，封装成BeanDefinition
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
            getBean(beanName);
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
    @Override
    public Object getBean(String beanName) {
        DLPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //doCreateBean 为什么先初始化后注入？解决循环依赖
        //instantiateBean 初始化实体对象 beanName,beanDefinition
        DLPBeanWrapper beanWrapper = instantiateBean(beanName, beanDefinition);
        //把beanWrapper保存到ioc容器中
//        if(factoryBeanInstanceCache.containsKey(beanName)){
//            throw new Exception("The" + beanName + "is exists!");
//
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);
        //populateBean 注入 把bd转换成beanWrapper 缓存
        populateBean(beanName, new DLPBeanDefinition(), beanWrapper);
        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    private void populateBean(String beanName, DLPBeanDefinition beanDefinition, DLPBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrappedInstance();
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
            System.out.println("autowiredBeanName==="+autowiredBeanName);
            try {
                //为什么这里会有null 接口的
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){continue;}
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    private DLPBeanWrapper instantiateBean(String beanName, DLPBeanDefinition beanDefinition) {
        //1、拿到要实例化的对象的类名

        String className = beanDefinition.getBeanName();
        //2、反射实例化，得到一个对象
        Object instance = new Object();
        Class<?> clazz = null;
        try{
            //默认单例的了
            if(this.singletonObjects.containsKey(className)){
                instance = this.singletonObjects.get(className);
                clazz = Class.forName(className);
            }else{
                clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.singletonObjects.put(className, instance);
                this.singletonObjects.put(beanDefinition.getFactoryBeanName(), instance);
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
}
