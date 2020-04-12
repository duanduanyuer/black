package dlpspring.mymvc.controller;

import dlpspring.framework.annotation.DLPAutowired;
import dlpspring.framework.annotation.DLPController;
import dlpspring.framework.annotation.DLPRequestMapping;
import dlpspring.framework.annotation.DLPRequestParam;
import dlpspring.framework.webmvc.servlet.DLPModelAndView;
import dlpspring.mymvc.service.BaseService;
import dlpspring.mymvc.service.DlpService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description: TODO
 * @Author duanliping
 * @Date 2020/4/4
 **/
@DLPRequestMapping("/dlp")
@DLPController
public class DlpController {

    @DLPAutowired
    private BaseService dlpService;

    @DLPRequestMapping("/hello")
    public void hello(HttpServletRequest req, HttpServletResponse resp, @DLPRequestParam("word") String word){
        try {
            resp.getWriter().write(dlpService.hello(word));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DLPRequestMapping("/world")
    public DLPModelAndView world(HttpServletRequest req, HttpServletResponse resp, @DLPRequestParam("word") String word){
        try {
            return out(resp, dlpService.hello("happy!!!"+word));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private DLPModelAndView out(HttpServletResponse resp, String hello) {
        try{
            resp.getWriter().write(hello);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
