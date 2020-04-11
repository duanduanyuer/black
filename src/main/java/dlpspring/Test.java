package dlpspring;

import dlpspring.framework.context.DLPApplicationContext;
import dlpspring.mymvc.service.BaseService;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/9
 **/
public class Test {
    public static void main(String[] args) {
        DLPApplicationContext applicationContext = new DLPApplicationContext("classpath:application.properties");
//        BaseService service = (BaseService)applicationContext.getBean(BaseService.class); //按照类型获取
        BaseService service = (BaseService)applicationContext.getBean("dlpService"); //按照名字获取
        System.out.printf(service.hello("hello spring!!!"));
    }
}
