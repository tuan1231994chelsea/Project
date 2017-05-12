package tuan.anh.giang.project.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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

import tuan.anh.giang.core.utils.KeyboardUtils;
import tuan.anh.giang.core.utils.SharedPrefsHelper;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.utils.ErrorHandling;
import tuan.anh.giang.project.utils.ValidationUtils;

import static tuan.anh.giang.project.activities.MainActivity.currentBackendlessUser;

/**
 * Created by GIANG ANH TUAN on 23/04/2017.
 */

public class LoginActivity extends BaseActivity {
    Context context;
    EditText userName, password;
    TextView register, forgotPassWord;
    Button submit, loginWithFB;

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
        register = (TextView) findViewById(R.id.tv_register);
        forgotPassWord = (TextView) findViewById(R.id.tv_forgotPassword);
        submit = (Button) findViewById(R.id.bt_submit);
        loginWithFB = (Button) findViewById(R.id.bt_loginWithFB);
    }

    private void setOnClick() {
        forgotPassWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogForgotPassword();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpActivity.start(view.getContext());
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnteredUserNameValid()) {
                    hideKeyboard();
                    login(userName.getText().toString().trim(), password.getText().toString().trim());
//                    startSignUpNewUser(createUserWithEnteredData());
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

    private void login(String username, String password) {
        Backendless.UserService.login(username, password, new AsyncCallback<BackendlessUser>() {
            public void handleResponse(BackendlessUser user) {
                // user has been logged in
                if (sharedPrefsHelper.hasBELUser()) {
                    BackendlessUser oldBELUser = sharedPrefsHelper.getBELUser();
                    if(!user.getEmail().equals(oldBELUser.getEmail())){
                        deleteAndSaveNewBackendUser(user);
                        deleteQBUser();
                    }
                } else {
                    sharedPrefsHelper.saveBELUser(user);
                }
                Log.d("myapp", "login bel user thanh cong");
                MainActivity.start(context, false);
                finish();

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
    private void showDialogForgotPassword(){
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_forgot_password);
        final EditText edUserName = (EditText) dialog.findViewById(R.id.user_name);
        Button btSendEmail = (Button) dialog.findViewById(R.id.bt_send_email);

        edUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                edUserName.setError(null);
            }
        });

        btSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = edUserName.getText().toString().trim();
                if(userName.equals("")){
                    edUserName.setError(getString(R.string.fill_out_user_name));
                }else{
                    Backendless.UserService.restorePassword(userName,
                            new AsyncCallback<Void>() {
                                public void handleResponse(Void response) {
                                    // Backendless has completed the operation - an email has been sent to the user
                                    hideProgressDialog();
                                    showNotifyDialog("Change password", getString(R.string.Email_sent),R.drawable.success);
                                    dialog.dismiss();
                                }

                                public void handleFault(BackendlessFault fault) {
                                    // password revovery failed, to get the error code call fault.getCode()
                                    Log.d("error", fault.getMessage());
                                    showNotifyDialog("Change password", "Changing password have not succeed",R.drawable.error);
                                    dialog.dismiss();
                                }
                            });
                }
            }
        });
        dialog.show();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    private void deleteAndSaveNewBackendUser(BackendlessUser currentBackendlessUser) {
        sharedPrefsHelper.removeBELUser();
        sharedPrefsHelper.saveBELUser(currentBackendlessUser);
    }
    private void deleteQBUser(){
        sharedPrefsHelper.removeQbUser();
    }



}
