package tuan.anh.giang.project.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.quickblox.users.model.QBUser;

import static tuan.anh.giang.project.activities.MainActivity.currentBackendlessUser;
import static tuan.anh.giang.project.activities.MainActivity.mainActivity;

import tuan.anh.giang.core.utils.ConnectivityUtils;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.utils.ErrorHandling;


public class UserInforFragment extends Fragment {
    View view;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder alertDialog;
    EditText edFullName, edEmail;
    ImageView imgSend;
    Button btnUpdateInfor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_infor, container, false);
        findViewById();
        onClick();
        return view;
    }

    private void findViewById() {
        edFullName = (EditText) view.findViewById(R.id.ed_fullname);
        edEmail = (EditText) view.findViewById(R.id.ed_email);
        btnUpdateInfor = (Button) view.findViewById(R.id.btn_update_infor);
        imgSend = (ImageView) view.findViewById(R.id.img_send);
        edFullName.setText(currentBackendlessUser.getProperty(getString(R.string.full_name)).toString());
        edEmail.setText(currentBackendlessUser.getEmail());
        edEmail.addTextChangedListener(new UserInforEditTextWatcher(edEmail));
        edFullName.addTextChangedListener(new UserInforEditTextWatcher(edFullName));
        edEmail.setFocusable(false);
        edFullName.setFocusable(false);
    }

    private void onClick() {
        // change password by send email

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityUtils.isNetworkConnected()) {
                    showProgressDialog("Updating password");
                    Backendless.UserService.restorePassword(currentBackendlessUser.getProperty(getString(R.string.user_name)).toString(),
                            new AsyncCallback<Void>() {
                                public void handleResponse(Void response) {
                                    // Backendless has completed the operation - an email has been sent to the user
                                    hideProgressDialog();
                                    showNotifyDialog("Change password", getString(R.string.Email_sent), R.drawable.success);
                                }

                                public void handleFault(BackendlessFault fault) {
                                    // password revovery failed, to get the error code call fault.getCode()
                                    hideProgressDialog();
                                    Log.d("error", fault.getMessage());
                                }
                            });
                } else {
                    hideProgressDialog();
                    showNotifyDialog("", getString(R.string.no_internet_connection), R.drawable.error);
                }
            }
        });
        btnUpdateInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityUtils.isNetworkConnected()) {
                    if (btnUpdateInfor.getText().equals(getString(R.string.Edit_Infor))) {
                        // thuc hien sua thong tin
                        btnUpdateInfor.setText(getString(R.string.Complete_Update_Infor));
                        edEmail.setFocusableInTouchMode(true);
                        edEmail.setFocusable(true);
                        edFullName.setFocusableInTouchMode(true);
                        edFullName.setFocusable(true);
                        edFullName.requestFocus();
                    } else {
                        if (checkFillOut()) {
                            edEmail.setFocusable(false);
                            edFullName.setFocusable(false);
                            BackendlessUser user = currentBackendlessUser;
                            user.setEmail(edEmail.getText().toString());
                            user.setProperty(getString(R.string.full_name), edFullName.getText().toString());
                            showProgressDialog(R.string.updating);
                            Backendless.Data.of(BackendlessUser.class).save(user, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser response) {
                                    hideProgressDialog();
                                    Backendless.UserService.setCurrentUser(response);
                                    currentBackendlessUser = response;
                                    showNotifyDialog("Update Information", "Your information has been updated", R.drawable.success);
                                    updateUserInDb();
                                    btnUpdateInfor.setText(getString(R.string.Edit_Infor));
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    hideProgressDialog();
                                    ErrorHandling.BackendlessErrorCode(getActivity(), fault.getCode());
                                }
                            });
                        }
                    }
                } else {
                    hideProgressDialog();
                    showNotifyDialog("", getString(R.string.no_internet_connection), R.drawable.error);
                }
            }
        });
    }

    private void updateUserInDb() {
        // update bel user
        mainActivity.sharedPrefsHelper.save("bel_user_full_name", currentBackendlessUser.getProperty(getString(R.string.full_name)));
        mainActivity.sharedPrefsHelper.save("bel_user_email", currentBackendlessUser.getProperty(getString(R.string.email)));
        // update Qb user
        mainActivity.sharedPrefsHelper.save("qb_user_full_name", currentBackendlessUser.getProperty(getString(R.string.full_name)));

    }

    private boolean checkFillOut() {
        if (edEmail.getText().toString().equals("")) {
            edEmail.setError(getString(R.string.fill_out_email));
            return false;
        }
        if (edFullName.getText().toString().equals("")) {
            edFullName.setError(getString(R.string.fill_out_full_name));
            return false;
        }
        return true;
    }

    void showProgressDialog(@StringRes int messageId) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        progressDialog.setMessage(getString(messageId));

        progressDialog.show();

    }

    void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        progressDialog.setMessage(message);

        progressDialog.show();

    }


    void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void showNotifyDialog(String titlte, String message, int icon) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(getActivity());
        }
        alertDialog.setTitle(titlte)
                .setIcon(icon)
                .setMessage(message)
                .create()
                .show();
    }

    private class UserInforEditTextWatcher implements TextWatcher {
        private EditText editText;

        private UserInforEditTextWatcher(EditText editText) {
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
