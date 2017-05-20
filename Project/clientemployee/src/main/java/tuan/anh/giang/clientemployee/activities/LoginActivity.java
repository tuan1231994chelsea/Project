package tuan.anh.giang.clientemployee.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.quickblox.auth.session.QBSessionManager;

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.utils.ErrorHandling;
import tuan.anh.giang.clientemployee.utils.ValidationUtils;
import tuan.anh.giang.clientemployee.utils.chat.ChatHelper;
import tuan.anh.giang.core.utils.KeyboardUtils;
import tuan.anh.giang.core.utils.SharedPrefsHelper;

public class LoginActivity extends BaseActivity {
    Context context;
    EditText userName, password;
    Button submit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        this.context = this;
        findViewById();
        setOnClick();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.root_view_login_activity);
    }

    private void findViewById() {
        userName = (EditText) findViewById(R.id.user_name);
        password = (EditText) findViewById(R.id.password);
        submit = (Button) findViewById(R.id.bt_submit);
    }

    private void setOnClick() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnteredUserNameValid()) {
                    hideKeyboard();
                    login(userName.getText().toString().trim(), password.getText().toString().trim());
                }
            }
        });
    }

    private boolean isEnteredUserNameValid() {
        return ValidationUtils.isUserNameValid(this, userName);
    }

    private void hideKeyboard() {
        KeyboardUtils.hideKeyboard(userName);
        KeyboardUtils.hideKeyboard(password);
    }

    private void login(String username, String pw) {
        Backendless.UserService.login(username, pw, new AsyncCallback<BackendlessUser>() {
            public void handleResponse(BackendlessUser user) {
                Log.d("myapp", "login bel user thanh cong");
                boolean isEmployee = (boolean) user.getProperty(getString(R.string.is_employee));
                if (!isEmployee) {
                    Backendless.UserService.logout(new AsyncCallback<Void>() {
                        @Override
                        public void handleResponse(Void response) {
                            // user has been logged out.
                            hideProgressDialog();
                            showNotifyDialog("", "User is not employee, please login again", R.drawable.error);
                            userName.setText("");
                            password.setText("");
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.d("error", fault.getMessage());
                        }
                    });
                } else {
                    MainActivity.start(context, false);
                    finish();
                }
            }

            public void handleFault(BackendlessFault fault) {
                // login failed, to get the error code call fault.getCode()
                //3087 loi email chua confirm
                if (fault.getCode().equals("Server.Processing")) {
                    MainActivity.start(context, false);
                    finish();
                    return;
                }
                ErrorHandling.BackendlessErrorCode(context, fault.getCode());
                Log.d("error myapp", fault.getCode());
            }
        }, true);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }


}
