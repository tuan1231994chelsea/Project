package tuan.anh.giang.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.backendless.BackendlessUser;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import tuan.anh.giang.core.CoreApp;

public class SharedPrefsHelper {
    private static final String SHARED_PREFS_NAME = "qb";

    private static final String QB_USER_ID = "qb_user_id";
    private static final String QB_USER_LOGIN = "qb_user_login";
    private static final String QB_USER_PASSWORD = "qb_user_password";
    private static final String QB_USER_FULL_NAME = "qb_user_full_name";
    private static final String QB_USER_TAGS = "qb_user_tags";

    private static final String BEL_USER_NAME="bel_user_name";
    private static final String BEL_USER_PASSWORD="bel_user_password";
    private static final String BEL_USER_EMAIL="bel_user_email";
    private static final String BEL_USER_FULL_NAME="bel_user_full_name";
    private static final String BEL_USER_IS_EMPLOYEE="bel_user_is_employee";
    private static final String BEL_USER_IS_ONLINE="bel_user_is_online";
    private static final String BEL_USER_TAGS="bel_user_tags";



    private static SharedPrefsHelper instance;

    private SharedPreferences sharedPreferences;

    public static synchronized SharedPrefsHelper getInstance() {
        if (instance == null) {
            instance = new SharedPrefsHelper();
        }

        return instance;
    }

    private SharedPrefsHelper() {
        instance = this;
        sharedPreferences = CoreApp.getInstance().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void delete(String key) {
        if (sharedPreferences.contains(key)) {
            getEditor().remove(key).commit();
        }
    }

    public void save(String key, Object value) {
        SharedPreferences.Editor editor = getEditor();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Enum) {
            editor.putString(key, value.toString());
        } else if (value != null) {
            throw new RuntimeException("Attempting to save non-supported preference");
        }

        editor.commit();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) sharedPreferences.getAll().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defValue) {
        T returnValue = (T) sharedPreferences.getAll().get(key);
        return returnValue == null ? defValue : returnValue;
    }

    public boolean has(String key) {
        return sharedPreferences.contains(key);
    }


    public void saveQbUser(QBUser qbUser) {
        save(QB_USER_ID, qbUser.getId());
        save(QB_USER_LOGIN, qbUser.getLogin());
        save(QB_USER_PASSWORD, qbUser.getPassword());
        save(QB_USER_FULL_NAME, qbUser.getFullName());
        save(QB_USER_TAGS, qbUser.getTags().getItemsAsString());
    }

    public void removeQbUser() {
        delete(QB_USER_ID);
        delete(QB_USER_LOGIN);
        delete(QB_USER_PASSWORD);
        delete(QB_USER_FULL_NAME);
        delete(QB_USER_TAGS);
    }

    public QBUser getQbUser() {
        if (hasQbUser()) {
            Integer id = get(QB_USER_ID);
            String login = get(QB_USER_LOGIN);
            String password = get(QB_USER_PASSWORD);
            String fullName = get(QB_USER_FULL_NAME);
            String tagsInString = get(QB_USER_TAGS);

            StringifyArrayList<String> tags = null;

            if (tagsInString != null) {
                tags = new StringifyArrayList<>();
                tags.add(tagsInString.split(","));
            }

            QBUser user = new QBUser(login, password);
            user.setId(id);
            user.setFullName(fullName);
            user.setTags(tags);
            return user;
        } else {
            return null;
        }
    }
    public boolean hasBELUser(){
        return has(BEL_USER_NAME);
    }
    public  void saveBELUser(BackendlessUser belUser){
        save(BEL_USER_NAME,belUser.getProperty("user_name"));
        save(BEL_USER_EMAIL,belUser.getProperty("email"));
        save(BEL_USER_FULL_NAME,belUser.getProperty("full_name"));
        save(BEL_USER_IS_EMPLOYEE,belUser.getProperty("is_employee"));
        save(BEL_USER_TAGS,belUser.getProperty("tags"));
    }
    public void removeBELUser(){
        delete(BEL_USER_NAME);
        delete(BEL_USER_EMAIL);
        delete(BEL_USER_FULL_NAME);
        delete(BEL_USER_IS_EMPLOYEE);
        delete(BEL_USER_TAGS);
    }
    public BackendlessUser getBELUser(){
        if(hasBELUser()){
            BackendlessUser belUser = new BackendlessUser();
            belUser.setProperty("user_name",get(BEL_USER_NAME));
            belUser.setProperty("email",get(BEL_USER_EMAIL));
            belUser.setProperty("full_name",get(BEL_USER_FULL_NAME));
            belUser.setProperty("is_employee",get(BEL_USER_IS_EMPLOYEE));
            belUser.setProperty("tags",get(BEL_USER_TAGS));
            return belUser;
        }
        else{
            return null;
        }
    }


    public boolean hasQbUser() {
        return has(QB_USER_LOGIN) && has(QB_USER_PASSWORD);
    }

    public void clearAllData(){
        SharedPreferences.Editor editor = getEditor();
        editor.clear().commit();
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }
}
