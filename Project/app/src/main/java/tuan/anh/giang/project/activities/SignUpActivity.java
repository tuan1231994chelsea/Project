package tuan.anh.giang.project.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import tuan.anh.giang.core.utils.ConnectivityUtils;
import tuan.anh.giang.core.utils.Toaster;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.db.Defaults;
import tuan.anh.giang.project.utils.Consts;
import tuan.anh.giang.project.utils.ErrorHandling;
import tuan.anh.giang.project.utils.QBEntityCallbackImpl;
import tuan.anh.giang.project.utils.ValidationUtils;



public class SignUpActivity extends BaseActivity {
    EditText userName, passWord, confirmPassword, email, fullName;
    ImageView img_back;
    Button signUp;
    Context context;
    BackendlessUser currentBELUser;
    QBUser currentQBUser;

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
                    if(ConnectivityUtils.isNetworkConnected()){
                        BackendlessUser user = new BackendlessUser();
                        user.setProperty(getResources().getString(R.string.user_name), userName.getText().toString().trim());
                        user.setPassword(passWord.getText().toString().trim());
                        user.setProperty(getString(R.string.email), email.getText().toString().trim());
                        user.setProperty(getString(R.string.full_name), fullName.getText().toString().trim());
                        user.setProperty(getString(R.string.is_employee),false);
                        user.setProperty(getString(R.string.is_online),false);
                        showProgressDialog("Signing up");
                        Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                            public void handleResponse(BackendlessUser belUser) {
                                // user has been registered and now can login
                                currentBELUser = belUser;
                                startSignUpNewUser(createQBUserWithCurrentData(currentBELUser));

//                            finish();
                            }

                            public void handleFault(BackendlessFault fault) {
                                // an error has occurred, the error code can be retrieved with fault.getCode()
                                hideProgressDialog();
                                ErrorHandling.BackendlessErrorCode(view.getContext(), fault.getCode());
                                Log.d("Error ", fault.getCode());
                            }
                        });
                    }else{
                        hideProgressDialog();
                        showNotifyDialog("", getString(R.string.no_internet_connection), R.drawable.error);
                    }
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
    // user quickblox
    private void startSignUpNewUser(final QBUser newUser) {
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.d("myapp", "signUp qb user success");
                        // update cac truong QBUser cho BELUser
                        currentQBUser = result;
                        currentBELUser.setProperty(getString(R.string.id_qb),currentQBUser.getId());
                        currentBELUser.setProperty(getString(R.string.login),currentQBUser.getLogin());
                        currentBELUser.setProperty(getString(R.string.tags),currentQBUser.getLogin());
                        Backendless.Data.of(BackendlessUser.class).save(currentBELUser, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                // update thanh cong du lieu QbUser vao backendlessUser
                                hideProgressDialog();

                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignUpActivity.this);

                                alertDialog.setTitle("Sign up")
                                        .setIcon(R.drawable.success)
                                        .setMessage(getString(R.string.register_success))
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                LoginActivity.start(SignUpActivity.this);
                                            }
                                        })
                                        .create()
                                        .show();
//                                showNotifyDialog("Sign up",getString(R.string.register_success),R.drawable.success);
//                                LoginActivity.start(SignUpActivity.this);
//                                finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                hideProgressDialog();
                                showNotifyDialog("Sign up",fault.getMessage(),R.drawable.error);
                            }
                        });
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser, true);
                            Log.d("myapp", "error signUp qb user ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS ");
                        } else {
                            hideProgressDialog();
                            Toaster.longToast(R.string.sign_up_error);
                        }
                    }
                }
        );
    }
    private QBUser createQBUserWithCurrentData(BackendlessUser user) {
        QBUser qbUser = null;
        String userName = user.getProperty(getString(R.string.user_name)).toString();
        String fullName = user.getProperty(getString(R.string.full_name)).toString();
        if (!TextUtils.isEmpty(userName)) {
            StringifyArrayList<String> userTags = new StringifyArrayList<>();
            userTags.add(userName);

            qbUser = new QBUser();
            qbUser.setFullName(fullName);
            qbUser.setLogin(userName);
            qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
            qbUser.setTags(userTags);
        }
        return qbUser;
    }
    private void signInCreatedUser(final QBUser user, final boolean deleteCurrentUser) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                Log.d("myapp", "sign in thanh cong QbUser");
                Log.d("kiemtratime", "dang nhap QB user thanh cong");
                if (deleteCurrentUser) {

                } else {

//                    startOpponentsActivity();
                }
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                Toaster.longToast(R.string.sign_up_error);
            }
        });
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
