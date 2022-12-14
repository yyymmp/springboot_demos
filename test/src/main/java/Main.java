import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Date;

/**
 * @author jlz
 * @date 2022年09月05日 22:16
 */
public class Main {

    public static void main(String[] args) {
        Date date = new Date();
        Date date1 = DateUtils.addMinutes(date, 2);
        long l = (date1.getTime() - date.getTime()) / (60 * 1000);
        System.out.println(l);

    }
}
