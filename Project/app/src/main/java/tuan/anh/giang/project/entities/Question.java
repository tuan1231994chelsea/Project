package tuan.anh.giang.project.entities;

import  com.backendless.BackendlessUser;
import weborb.service.ExcludeProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by GIANG ANH TUAN on 29/04/2017.
 */

// có thể loại bỏ thuộc tính bằng lênh dưới. => có thể thêm thuộc tinh
    // numberOfAnswer xong Exclude nó đi.=> lúc add new Question
    // console không thêm 1 trường mới numberOfQuestion nữa
    // mà mình có thể gán thêm vào để sử dụng.
@ExcludeProperty( propertyName = "_reply" )
public class Question implements Serializable{
    private String objectId;
    private BackendlessUser user;
    private String content;
    private boolean is_reply;
    private Date created;
    private Date updated;
    @ExcludeProperty( propertyName="_reply" )

    public Question() {
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public BackendlessUser getUser() {
        return user;
    }

    public void setUser(BackendlessUser user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getIs_reply() {
        return is_reply;
    }

    public void setIs_reply(boolean is_reply) {
        this.is_reply = is_reply;
    }

}
