import com.ch.cloud.sso.tools.JwtTokenTool;
import com.ch.cloud.sso.utils.JwtUtils;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author 01370603
 */
public class ClientTests {

    @Test
    public void test() {
        String str = new BCryptPasswordEncoder().encode("secret");

        System.out.println(str);
    }

    @Test
    public void token() {
        String s = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE1NzUwMTgwOTEsInN1YiI6InRlc3QiLCJjcmVhdGVkIjoxNTc0OTMxNjkxNzEyfQ.YtN34sHM3AZ0vlZRGYa7wsU6pUTEEKTbLTDAZHS20bIAdPI8MZh16DdugBpBRj7TGETNvfWZ2ZF_QnNn-vqAZA";
        JwtTokenTool tokenTool = new JwtTokenTool();
//        tokenTool.setSecret("eyJleHAiOjE1NDMyMDUyODUsInN1YiI6ImFkbWluIiwiY3Jl");
        System.out.println(tokenTool.isTokenExpired(s));
    }
}
