package lib.time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampService {
    public static String nowDate() {
        LocalDateTime dt = LocalDateTime.now();
        return dt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
    public static String nowTime() {
        LocalDateTime dt = LocalDateTime.now();
        return dt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
