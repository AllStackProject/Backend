package app.allstackproject.privideo.controller.test.seohyun;

import app.allstackproject.privideo.common.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seohyun")
public class TestController {

    @GetMapping("/test/get")
    public BaseResponse<String> getTest() {
        return new BaseResponse<>("인증 성공");
    }
}
