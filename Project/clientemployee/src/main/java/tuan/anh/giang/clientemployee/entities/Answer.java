package tuan.anh.giang.clientemployee.entities;

import com.backendless.BackendlessUser;

import java.io.Serializable;
import java.util.Date;

import weborb.service.ExcludeProperty;

/**
 * Created by GIANG ANH TUAN on 04/05/2017.
 */

@ExcludeProperty( propertyName = "serialVersionUID" )
public class Answer implements Serializable{
    private Date created;
    private Date updated;
    private String objectId;
    private String content_answer;
    private String image;
    private BackendlessUser user;

    public Answer() {
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

    public String getContent_answer() {
        return content_answer;
    }

    public void setContent_answer(String content_answer) {
        this.content_answer = content_answer;
    }

    public BackendlessUser getUser() {
        return user;
    }

    public void setUser(BackendlessUser user) {
        this.user = user;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
