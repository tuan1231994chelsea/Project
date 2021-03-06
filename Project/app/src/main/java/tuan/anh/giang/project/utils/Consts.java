package tuan.anh.giang.project.utils;

import android.Manifest;

import tuan.anh.giang.core.utils.ResourceUtils;
import tuan.anh.giang.project.R;


public interface Consts {
    String SAMPLE_CONFIG_FILE_NAME = "sample_config.json";

    int PREFERRED_IMAGE_SIZE_PREVIEW = ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size);
    int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);
    String QB_USER_PASSWORD = "qb_user_password";

    String QB_CONFIG_FILE_NAME = "qb_config.json";

    String DEFAULT_USER_PASSWORD = "a12345678";

    String VERSION_NUMBER = "1.0";

    int CALL_ACTIVITY_CLOSE = 1000;

    int ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS = 422;
    int ERR_MSG_DELETING_HTTP_STATUS = 401;

    //CALL ACTIVITY CLOSE REASONS
    int CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001;
    String WIFI_DISABLED = "wifi_disabled";

    String OPPONENTS = "opponents";
    String CONFERENCE_TYPE = "conference_type";
    String EXTRA_TAG = "currentRoomName";
    int MAX_OPPONENTS_COUNT = 6;

    String PREF_CURREN_ROOM_NAME = "current_room_name";
    String PREF_CURRENT_TOKEN = "current_token";
    String PREF_TOKEN_EXPIRATION_DATE = "token_expiration_date";

    String EXTRA_QB_USER = "qb_user";

    String EXTRA_USER_ID = "user_id";
    String EXTRA_USER_LOGIN = "user_login";
    String EXTRA_USER_PASSWORD = "user_password";
    String EXTRA_PENDING_INTENT = "pending_Intent";

    String EXTRA_CONTEXT = "context";
    String EXTRA_OPPONENTS_LIST = "opponents_list";
    String EXTRA_CONFERENCE_TYPE = "conference_type";
    String EXTRA_IS_INCOMING_CALL = "conversation_reason";

    String EXTRA_LOGIN_RESULT = "login_result";
    String EXTRA_LOGIN_ERROR_MESSAGE = "login_error_message";
    int EXTRA_LOGIN_RESULT_CODE = 1002;

    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    String[] PERMISSIONS_SEND_IMAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    String EXTRA_COMMAND_TO_SERVICE = "command_for_service";
    int COMMAND_NOT_FOUND = 0;
    int COMMAND_LOGIN = 1;
    int COMMAND_LOGOUT = 2;
    String EXTRA_IS_STARTED_FOR_CALL = "isRunForCall";
    String ALREADY_LOGGED_IN = "You have already logged in chat";
    String COUNTER_QUESTION="counter_question";
    String COUNTER_ANSWER ="counter_answer";
    int WAIT_EMPLOYEE_REPLY = 0;
    int WAIT_USER_REPLY =1;
    int USER_LEAVE_QUESTION = 2;
    String LOAD_QUESTION_BY_STATUS = "load_question_by_status";
    String LOAD_ALL_QUESTION ="";
    String CONDITION_WAIT_EMPLOYEE ="and status =0";
    String CONDITION_WAIT_USER ="and status =1";
    String CONDITION_USER_LEAVE_QUESTION ="and status =2";

    enum StartConversationReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE
    }
}
