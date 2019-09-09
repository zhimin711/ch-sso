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
}
