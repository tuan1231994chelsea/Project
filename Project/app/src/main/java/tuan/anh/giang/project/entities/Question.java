package tuan.anh.giang.project.entities;

import com.backendless.BackendlessUser;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by GIANG ANH TUAN on 29/04/2017.
 */

public class Question implements Serializable{
    private int number_of_answer;
    private String objectId;
    private BackendlessUser user;
    private String content;
    private boolean is_reply;
    private Date created;
    private Date updated;

    public Question() {
    }


    public int getNumber_of_answer() {
        return number_of_answer;
    }

    public void setNumber_of_answer(int number_of_answer) {
        this.number_of_answer = number_of_answer;
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

    public boolean is_reply() {
        return is_reply;
    }

    public void setIs_reply(boolean is_reply) {
        this.is_reply = is_reply;
    }

}
