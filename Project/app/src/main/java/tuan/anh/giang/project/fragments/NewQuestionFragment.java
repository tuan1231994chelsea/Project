package tuan.anh.giang.project.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.Files;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import tuan.anh.giang.core.utils.ConnectivityUtils;
import tuan.anh.giang.core.utils.ImageUtils;
import tuan.anh.giang.core.utils.KeyboardUtils;
import tuan.anh.giang.core.utils.imagepick.OnImagePickedListener;
import tuan.anh.giang.core.utils.imagepick.fragment.ImageSourcePickDialogFragment;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.activities.AttachmentImageActivity;
import tuan.anh.giang.project.activities.PermissionsActivity;
import tuan.anh.giang.project.entities.Answer;
import tuan.anh.giang.project.entities.Question;
import tuan.anh.giang.project.utils.Consts;
import tuan.anh.giang.project.utils.PermissionsChecker;

import static android.app.Activity.RESULT_OK;
import static tuan.anh.giang.core.utils.ImageUtils.CAMERA_REQUEST_CODE;
import static tuan.anh.giang.core.utils.ImageUtils.GALLERY_REQUEST_CODE;
import static tuan.anh.giang.project.activities.MainActivity.mainActivity;


public class NewQuestionFragment extends Fragment {
    View view;
    ImageView imgBack, imgUpload;
    LinearLayout layoutUpload;
    EditText edContentQuestion;
    Button btnSubmit;
    public boolean isUpdateMain = false;
    private ProgressDialog progressDialog;
    final int REQUEST_IMAGE = 1094;
    Bitmap bitmap;
    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA = 1;
    PermissionsChecker checker;
    Uri currentUri;
    String remotePath = "ImageFolder";
    String remoteName = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checker = new PermissionsChecker(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_question, container, false);
        findViewById();
        onClick();
        return view;
    }

    private void onClick() {
        imgUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttachmentImageActivity.start(getContext(), currentUri.toString());
            }
        });
        layoutUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checker.lacksPermissions(Consts.PERMISSIONS_SEND_IMAGE)) {
                    PermissionsActivity.startActivity(getActivity(), false, Consts.PERMISSIONS_SEND_IMAGE);
                }
                showDialogChooseImage();
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        edContentQuestion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    edContentQuestion.setBackgroundResource(R.drawable.border_content_question1);
                }
                return false;
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityUtils.isNetworkConnected()) {
                    // quality = 50
                    showProgressDialog(R.string.loading);
                    Backendless.Files.Android.upload(bitmap,
                            Bitmap.CompressFormat.JPEG,
                            50,
                            remoteName,
                            remotePath,
                            new AsyncCallback<BackendlessFile>() {
                                @Override
                                public void handleResponse(BackendlessFile response) {
                                    // success upload file
                                    edContentQuestion.setBackgroundResource(R.drawable.border_content_question);
                                    KeyboardUtils.hideKeyboard(edContentQuestion);
                                    String contentQuestion = edContentQuestion.getText().toString().trim();
                                    if (contentQuestion.equals("")) {
                                        edContentQuestion.setError(getString(R.string.fill_out_content_question));
                                    } else {
                                        final ArrayList<BackendlessUser> listUser = new ArrayList<BackendlessUser>();
                                        listUser.add(mainActivity.currentBackendlessUser);
                                        final Question question = new Question();
                                        question.setUser(mainActivity.currentBackendlessUser);
                                        question.setContent(contentQuestion);
                                        question.setStatus(0);
                                        question.setImage(response.getFileURL());
                                        //save new question
                                        Backendless.Persistence.save(question, new AsyncCallback<Question>() {
                                            public void handleResponse(Question response) {
                                                // save new question success
                                                // add relation with Users table
                                                Backendless.Persistence.of(Question.class).addRelation(question, getString(R.string.user), listUser,
                                                        new AsyncCallback<Integer>() {
                                                            @Override
                                                            public void handleResponse(Integer response) {
                                                                isUpdateMain = true;
                                                                hideProgressDialog();
                                                                getActivity().onBackPressed();
                                                            }

                                                            @Override
                                                            public void handleFault(BackendlessFault fault) {
                                                                hideProgressDialog();
                                                            }
                                                        });

                                            }

                                            public void handleFault(BackendlessFault fault) {
                                                hideProgressDialog();
                                                // an error has occurred, the error code can be retrieved with fault.getCode()
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    // fault
                                    Log.d("upload", "fault " + fault.getMessage());
                                }
                            });
                } else {
                    hideProgressDialog();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle("")
                            .setMessage(getActivity().getString(R.string.no_internet_connection))
                            .create()
                            .show();
                }
            }
        });

    }

    private void showDialogChooseImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(tuan.anh.giang.core.R.string.dlg_choose_image_from);
        builder.setItems(tuan.anh.giang.core.R.array.dlg_image_pick, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case POSITION_GALLERY:
                        ImageUtils.startImagePicker(NewQuestionFragment.this);
                        break;
                    case POSITION_CAMERA:
                        ImageUtils.startCameraForResult(NewQuestionFragment.this);
                        break;
                }
            }
        });
        builder.show();
    }

    void showProgressDialog(@StringRes int messageId) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            // Disable the back button
            DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            };
            progressDialog.setOnKeyListener(keyListener);
        }
        progressDialog.setMessage(getString(messageId));

        progressDialog.show();

    }

    void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void findViewById() {
        imgBack = (ImageView) view.findViewById(R.id.img_back);
        imgUpload = (ImageView) view.findViewById(R.id.img_upload);
        edContentQuestion = (EditText) view.findViewById(R.id.ed_content_question);
        btnSubmit = (Button) view.findViewById(R.id.btn_submit);
        layoutUpload = (LinearLayout) view.findViewById(R.id.layout_image);
        edContentQuestion.addTextChangedListener(new FragmentNewQuestionEditTextWatcher(edContentQuestion));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                currentUri = data.getData();
                remoteName = getFileName(currentUri);
                Picasso.with(getContext())
                        .load(currentUri)
                        .noPlaceholder()
                        .centerCrop()
                        .resize(200, 180)
                        .into((imgUpload));
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), currentUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                if (data == null || data.getData() == null) {
                    data = new Intent();
                    data.setData(Uri.fromFile(ImageUtils.getLastUsedCameraFile()));
                }
                currentUri = data.getData();
                remoteName = getFileName(currentUri);
                Picasso.with(getContext())
                        .load(currentUri)
                        .noPlaceholder()
                        .centerCrop()
                        .resize(200, 180)
                        .into((imgUpload));
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), currentUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    private class FragmentNewQuestionEditTextWatcher implements TextWatcher {
        private EditText editText;

        private FragmentNewQuestionEditTextWatcher(EditText editText) {
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
