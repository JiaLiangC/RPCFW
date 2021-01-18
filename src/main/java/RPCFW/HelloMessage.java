package RPCFW;

import java.io.Serializable;

public class HelloMessage implements Serializable {
    private String msg;
    private String desc;
    private static final long serialVersionUID = 1L;


    public HelloMessage(String msg, String desc) {
        this.msg = msg;
        this.desc = desc;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
