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
        System.out.println(applicationContext.getBean("dlpService"));
        BaseService service = (BaseService)applicationContext.getBean(BaseService.class);
        System.out.printf(service.hello("hello world!!!"));
    }
}
