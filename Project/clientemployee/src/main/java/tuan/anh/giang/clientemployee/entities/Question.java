package tuan.anh.giang.clientemployee.entities;

import com.backendless.BackendlessUser;

import java.io.Serializable;
import java.util.Date;

import weborb.service.ExcludeProperty;

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
    /*
    status = 0 => chờ nv trả lời, phản hồi
    status =1 => chờ người dùng phản hồi
    status = 2=> người dùng leave question
     */

    private int status;
    private Date created;
    private Date updated;
    @ExcludeProperty( propertyName = "serialVersionUID" )
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
