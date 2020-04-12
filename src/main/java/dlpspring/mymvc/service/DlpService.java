package dlpspring.mymvc.service;

import dlpspring.framework.annotation.DLPService;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/4
 **/
@DLPService("dlpService")
public class DlpService implements BaseService {

    @Override
    public String hello(String word) {
        System.out.println("lalala");
        return "hello spring!!!--"+word;
    }
}
