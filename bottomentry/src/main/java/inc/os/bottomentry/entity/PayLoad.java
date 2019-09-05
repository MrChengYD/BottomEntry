package inc.os.bottomentry.entity;

import java.security.PrivilegedAction;
import java.util.List;

import inc.os.bottomentry.PAYLOADTYPE;
import lombok.Data;

@Data
public class PayLoad {
    /**  **/
    private PAYLOADTYPE type;
    private List<String> filePathList;
    private long voiceRecordTime;
    private String textContain;
    private List<String> emojis;
}
