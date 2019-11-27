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
        String s = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE1NzQ4MjI4OTYsInN1YiI6ImFkbWluIiwiY3JlYXRlZCI6MTU3NDczNjQ5NjE5Nn0.BJecFfjp2xG9Ds8C9myPuIeWL8BzP0YM3Pe_sLiRqMKYu-R-b5HteFGI312gPEx-IZfoBmFKb8b7pQ1tg0FBnA";
        JwtTokenTool tokenTool = new JwtTokenTool();
        tokenTool.setSecret("eyJleHAiOjE1NDMyMDUyODUsInN1YiI6ImFkbWluIiwiY3Jl");
        System.out.println( tokenTool.isTokenExpired(s));
    }
}
