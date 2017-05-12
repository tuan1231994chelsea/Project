package tuan.anh.giang.project.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import tuan.anh.giang.project.R;
import tuan.anh.giang.project.db.Defaults;
import tuan.anh.giang.project.utils.ErrorHandling;
import tuan.anh.giang.project.utils.ValidationUtils;

/**
 * Created by GIANG ANH TUAN on 21/04/2017.
 */

public class SignUpActivity extends BaseActivity {
    EditText userName, passWord, confirmPassword, email, fullName;
    ImageView img_back;
    Button signUp;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        context = SignUpActivity.this;
        findViewById();
        onClick();

    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.root_view_signup_activity);
    }

    public static void start(Context context){
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    private void onClick() {
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (checkFillOutSignUp() && isEnteredUserNameValid() && checkPasswordAndConfirmPassword()) {
                    BackendlessUser user = new BackendlessUser();
                    user.setProperty(getResources().getString(R.string.user_name), userName.getText().toString().trim());
                    user.setPassword(passWord.getText().toString().trim());
                    user.setProperty("email", email.getText().toString().trim());
                    user.setProperty("full_name", fullName.getText().toString().trim());
                    Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                        public void handleResponse(BackendlessUser registeredUser) {
                            // user has been registered and now can login
                            Toast.makeText(view.getContext(), getResources().getString(R.string.register_success), Toast.LENGTH_LONG).show();
                            LoginActivity.start(view.getContext());
//                            finish();
                        }

                        public void handleFault(BackendlessFault fault) {
                            // an error has occurred, the error code can be retrieved with fault.getCode()
                            ErrorHandling.BackendlessErrorCode(view.getContext(), fault.getCode());
                            Log.d("Error ", fault.getCode());
                        }
                    });
                }
            }
        });
    }

    private void findViewById() {
        img_back = (ImageView) findViewById(R.id.img_back);
        userName = (EditText) findViewById(R.id.user_name);
        passWord = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);
        email = (EditText) findViewById(R.id.email);
        fullName = (EditText) findViewById(R.id.full_name);
        signUp = (Button) findViewById(R.id.bt_signup);

        userName.addTextChangedListener(new SignUpEditTextWatcher(userName));
        passWord.addTextChangedListener(new SignUpEditTextWatcher(passWord));
        confirmPassword.addTextChangedListener(new SignUpEditTextWatcher(confirmPassword));
        email.addTextChangedListener(new SignUpEditTextWatcher(email));
        fullName.addTextChangedListener(new SignUpEditTextWatcher(fullName));

    }

    private boolean checkFillOutSignUp() {
        if (userName.getText().toString().equals("")) {
            userName.setError(getResources().getString(R.string.fill_out_user_name));
            return false;
        }
        if (passWord.getText().toString().equals("")) {
            passWord.setError(getResources().getString(R.string.fill_out_password));
            return false;
        }
        if (confirmPassword.getText().toString().equals("")) {
            confirmPassword.setError(getResources().getString(R.string.fill_out_confirm_password));
            return false;
        }
        if (email.getText().toString().equals("")) {
            email.setError(getResources().getString(R.string.fill_out_email));
            return false;
        }
        if (fullName.getText().toString().equals("")) {
            fullName.setError(getResources().getString(R.string.fill_out_full_name));
            return false;
        }
        return true;
    }

    private boolean isEnteredUserNameValid() {
        return ValidationUtils.isUserNameValid(this, userName);
    }

    private boolean checkPasswordAndConfirmPassword() {
        if (passWord.getText().toString().trim().equals(confirmPassword.getText().toString().trim())) {
            return true;
        }
        confirmPassword.setError(getResources().getString(R.string.pls_check_confirm_password));
        return false;
    }

    private class SignUpEditTextWatcher implements TextWatcher {
        private EditText editText;

        private SignUpEditTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            editText.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}
