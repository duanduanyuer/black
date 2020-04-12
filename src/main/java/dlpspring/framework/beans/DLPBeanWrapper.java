package dlpspring.framework.beans;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/11
 **/
public class DLPBeanWrapper {

    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public DLPBeanWrapper(Object wrappedInstance) {
        if(wrappedInstance instanceof DLPBeanWrapper){
            this.wrappedClass = ((DLPBeanWrapper) wrappedInstance).wrappedClass;
            this.wrappedInstance = ((DLPBeanWrapper) wrappedInstance).wrappedInstance;
        }else{
            this.wrappedInstance = wrappedInstance;
        }
    }

    /**
     * 是单例 直接返回
     * @return
     */
    public Object getWrappedInstance(){
        return this.wrappedInstance;
    }

    /**
     * 不是单例每次new一个
     * @return
     */
    public Class<?> getWrappedClass(){
        return this.wrappedClass;
    }

    public void setWrappedClass(Class<?> wrappedClass) {
        this.wrappedClass = wrappedClass;
    }
}
