package dlpspring.framework.aop.config;

import lombok.Data;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/12
 **/
@Data
public class DLPAopConfig {
    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String AspectAfterThrow;
    private String aspectAfterThrowingName;
}
