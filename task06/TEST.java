import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by Admin on 2021/5/6.
 */
public class TEST {
    public static void main(String[] args) {
        String time = String.valueOf(System.currentTimeMillis() / 1000 + 600);// +600代表600秒后地址失效
        String md5 = Base64.encodeBase64URLSafeString(DigestUtils.md5("secret_key"+time+"/test.mp4"));
        System.out.println("http://192.168.1.9/test.mp4?md5=" + md5 + "&expires=" + time);
}
}