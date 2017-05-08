package tuan.anh.giang.project.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import tuan.anh.giang.core.utils.KeyboardUtils;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.utils.ErrorHandling;
import tuan.anh.giang.project.utils.ValidationUtils;

/**
 * Created by GIANG ANH TUAN on 23/04/2017.
 */

public class LoginActivity extends BaseActivity{
    Context context;
    EditText userName, password;
    TextView register, forgotPassWord;
    Button submit, loginWithFB;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.context=LoginActivity.this;
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
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpActivity.start(view.getContext());
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnteredUserNameValid() ) {
                    hideKeyboard();
                    login(userName.getText().toString().trim(),password.getText().toString().trim());
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
    private void login(String username,String password) {
        Backendless.UserService.login(username, password, new AsyncCallback<BackendlessUser>() {
            public void handleResponse(BackendlessUser user) {
                // user has been logged in
                Log.d("myapp","login bel user thanh cong");
                MainActivity.start(context,false);
                finish();

            }
            public void handleFault(BackendlessFault fault) {
                // login failed, to get the error code call fault.getCode()
                //3087 loi email chua confirm
                if(fault.getCode().equals("Server.Processing")){
                    MainActivity.start(context,false);
                    finish();
                    return ;
                }
                ErrorHandling.BackendlessErrorCode(context,fault.getCode());
                Log.d("error myapp", fault.getCode());
            }
        }, true);
    }
    public static void start(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }


}
