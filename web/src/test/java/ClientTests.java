import com.alibaba.fastjson.JSON;
import com.ch.cloud.sso.tools.JwtTokenTool;
import com.ch.cloud.sso.utils.CaptchaUtils;
import com.ch.cloud.sso.utils.JwtUtils;
import com.ch.cloud.sso.utils.SlideCaptchaUtil;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author 01370603
 */
public class ClientTests {

    Object o;

    @Test
    public void test() {
        String str = new BCryptPasswordEncoder().encode("secret");

        System.out.println(str);
    }

    @Test
    public void token() {
        String s = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0MDEiLCJleHAiOjE1OTM0NDcwOTQsInVzZXJJZCI6bnVsbCwicm9sZUlkIjowLCJjcmVhdGVkIjoxNTkzNDQ1Mjk0NzE4fQ.82dR0S5zdM8uOI1elHWtTGhBwV0Ac8vuP09OI2I_DjSFIqIRKk1NHgZkYXGtQ4eDCyRLvugFd7RgGEzSyjUYIw";
        JwtTokenTool tokenTool = new JwtTokenTool();
//        tokenTool.setSecret("eyJleHAiOjE1NDMyMDUyODUsInN1YiI6ImFkbWluIiwiY3Jl");
        o = tokenTool.getUserInfoFromToken(s);
        System.out.println(tokenTool.isTokenExpired(s));
    }

    @Test
    public void captcha() {
       o =  CaptchaUtils.selectSlideVerificationCode();
//        o = SlideCaptchaUtil.build();
        System.out.println(JSON.toJSONString(o));
    }
}
