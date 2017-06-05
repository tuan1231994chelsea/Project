package tuan.anh.giang.clientemployee.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.activities.AttachmentImageActivity;
import tuan.anh.giang.clientemployee.activities.ChatActivity;
import tuan.anh.giang.clientemployee.activities.MainActivity;
import tuan.anh.giang.clientemployee.activities.PermissionsActivity;
import tuan.anh.giang.clientemployee.adapters.AnswerAdapter;
import tuan.anh.giang.clientemployee.entities.Answer;
import tuan.anh.giang.clientemployee.entities.Question;
import tuan.anh.giang.clientemployee.utils.Consts;
import tuan.anh.giang.clientemployee.utils.PermissionsChecker;
import tuan.anh.giang.clientemployee.utils.chat.ChatHelper;
import tuan.anh.giang.clientemployee.view.NonScrollableListview;
import tuan.anh.giang.core.utils.ConnectivityUtils;
import tuan.anh.giang.core.utils.ImageUtils;
import tuan.anh.giang.core.utils.KeyboardUtils;
import tuan.anh.giang.core.utils.ResourceUtils;
import tuan.anh.giang.core.utils.SharedPrefsHelper;

import static android.app.Activity.RESULT_OK;
import static tuan.anh.giang.clientemployee.activities.MainActivity.currentBackendlessUser;
import static tuan.anh.giang.core.utils.ImageUtils.CAMERA_REQUEST_CODE;
import static tuan.anh.giang.core.utils.ImageUtils.GALLERY_REQUEST_CODE;


public class AnswerFragment extends Fragment {
    View view;
    private ProgressDialog progressDialog;
    Question question;
    TextView tvFullName, tvQuestion, tvCreated, tvMoreAnswer, tvStatusQuestion;
    EditText edReply;
    LinearLayout layoutMoreAnswer;
    ImageView imgBack, imgMoreAnswer, imgUser, imgSend, imgQuestion, imgReply, imgPreview,imgRemovePreview;
    NonScrollableListview lvAnswer;
    ArrayList<Answer> listAnswer;
    ArrayList<Answer> listLessAnswer;
    AnswerAdapter answerAdapter;
    DataQueryBuilder queryAnswer;
    public boolean isUpdateMain = false;
    int checkHideProgress = 0;
    String fullName = "";
    PermissionsChecker checker;
    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA = 1;
    Bitmap bitmap;
    Uri currentUri;
    String remotePath = "ImageFolder";
    String remoteName = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProgressDialog(R.string.loading_answer);
        checker = new PermissionsChecker(getContext());
        listAnswer = new ArrayList<>();
        listLessAnswer = new ArrayList<>();
        question = (Question) getArguments().getSerializable("question");
        fullName = getArguments().getString("FullName");
        String whereclause = "Question[answers].objectId = '" + question.getObjectId() + "'";
        queryAnswer = DataQueryBuilder.create();
        queryAnswer.setWhereClause(whereclause);
        queryAnswer.setSortBy("created ASC");
        queryAnswer.setPageSize(30);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_answer, container, false);
        findViewById();
        onClick();
        getAllListAnswer();
        return view;
    }

    private void findViewById() {
        imgBack = (ImageView) view.findViewById(R.id.img_back);
        imgUser = (ImageView) view.findViewById(R.id.img_user);
        tvFullName = (TextView) view.findViewById(R.id.tv_full_name);
        tvQuestion = (TextView) view.findViewById(R.id.tv_question);
        tvCreated = (TextView) view.findViewById(R.id.tv_created);
        lvAnswer = (NonScrollableListview) view.findViewById(R.id.lv_answer);
        imgSend = (ImageView) view.findViewById(R.id.img_send);
        imgQuestion = (ImageView) view.findViewById(R.id.img_question);
        imgReply = (ImageView) view.findViewById(R.id.img_reply);
        imgPreview = (ImageView) view.findViewById(R.id.img_preview);
        imgRemovePreview = (ImageView) view.findViewById(R.id.img_remove_preview);
        edReply = (EditText) view.findViewById(R.id.ed_reply);
        layoutMoreAnswer = (LinearLayout) view.findViewById(R.id.layout_more_answer);
        tvMoreAnswer = (TextView) view.findViewById(R.id.tv_more_answer);
        tvStatusQuestion = (TextView) view.findViewById(R.id.tv_status_question);
        imgMoreAnswer = (ImageView) view.findViewById(R.id.img_more_answer);
        edReply.addTextChangedListener(new FragmentAnswerEditTextWatcher(edReply));
        lvAnswer.setVerticalScrollBarEnabled(false);
        tvFullName.setText(fullName);
        tvQuestion.setText(question.getContent());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        tvCreated.setText("● " + sdf.format(question.getCreated()));
        answerAdapter = new AnswerAdapter(getActivity(), R.layout.item_list_answer, listAnswer);
        lvAnswer.setAdapter(answerAdapter);
        // chua tra loi mau do, tra loi roi mau xanh, nguoi dung leave mau lam
        if (question.getStatus() == Consts.WAIT_EMPLOYEE_REPLY) {
            tvStatusQuestion.setBackground(getActivity().getResources().getDrawable(R.drawable.status_question_0));
        }
        if (question.getStatus() == Consts.WAIT_USER_REPLY) {
            tvStatusQuestion.setBackground(getActivity().getResources().getDrawable(R.drawable.status_question_1));
        }
        if (question.getStatus() == Consts.USER_LEAVE_QUESTION) {
            tvStatusQuestion.setBackground(getActivity().getResources().getDrawable(R.drawable.status_question_2));
        }
        if (!(question.getImage() == null || question.getImage().equals(""))) {
            Picasso.with(getContext())
                    .load(question.getImage())
                    .noPlaceholder()
                    .centerCrop()
                    .resize(ResourceUtils.dpToPx(330), ResourceUtils.dpToPx(250))
                    .into((imgQuestion));
        }


    }

    private void onClick() {
        imgRemovePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgPreview.setImageResource(0);
                bitmap = null;
                imgRemovePreview.setVisibility(View.GONE);
            }
        });
        imgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    AttachmentImageActivity.start(getContext(), currentUri.toString());
                }
            }
        });
        imgReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checker.lacksPermissions(Consts.PERMISSIONS_SEND_IMAGE)) {
                    PermissionsActivity.startActivity(getActivity(), false, Consts.PERMISSIONS_SEND_IMAGE);
                }
                showDialogChooseImage();
            }
        });
        imgQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(question.getImage() == null || question.getImage().equals(""))) {
                    AttachmentImageActivity.start(getContext(), question.getImage());
                }
            }
        });
        tvStatusQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogStatusQuestion();
            }
        });
        // có hiện click là có nhiều hơn 10 answers
        layoutMoreAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tv = tvMoreAnswer.getText().toString();
                if (tv.equals(getString(R.string.view_all_answer))) {
                    tvMoreAnswer.setText(getString(R.string.view_less_answer));
                    lvAnswer.post(new Runnable() {
                        @Override
                        public void run() {
                            answerAdapter.setListAnswer(listAnswer);
                            answerAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    tvMoreAnswer.setText(getString(R.string.view_all_answer));
                    answerAdapter.setListAnswer(listLessAnswer);
                    lvAnswer.post(new Runnable() {
                        @Override
                        public void run() {
                            answerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        // add new answer
        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityUtils.isNetworkConnected()) {
                    if (bitmap != null) {
                        showProgressDialog(R.string.loading);
                        remoteName = String.valueOf(System.currentTimeMillis());
                        remoteName = remoteName + ".jpg";
                        Backendless.Files.Android.upload(bitmap,
                                Bitmap.CompressFormat.JPEG,
                                50,
                                remoteName,
                                remotePath,
                                new AsyncCallback<BackendlessFile>() {

                                    @Override
                                    public void handleResponse(BackendlessFile response) {
                                        bitmap = null;
                                        imgPreview.setImageResource(0);
                                        imgRemovePreview.setVisibility(View.GONE);
                                        final Answer answer = new Answer();
                                        KeyboardUtils.hideKeyboard(edReply);
                                        String content = edReply.getText().toString().trim();
                                        answer.setContent_answer(content);
                                        answer.setImage(response.getFileURL());
                                        answer.setUser(currentBackendlessUser);
                                        final ArrayList<Answer> listAdd = new ArrayList<Answer>();
                                        listAdd.add(answer);
                                        final ArrayList<BackendlessUser> listUser = new ArrayList<BackendlessUser>();
                                        listUser.add(currentBackendlessUser);
                                        // save new answer
                                        Backendless.Persistence.save(answer, new AsyncCallback<Answer>() {
                                            public void handleResponse(Answer response) {
                                                // save new answer success
                                                // list add response thay vi answer la vi answer khong cos trung created => crash
                                                // add response vi tra ve roi nen co created nhung khong co user -> phai add
                                                response.setUser(currentBackendlessUser);
                                                listAnswer.add(response);
                                                listLessAnswer.clear();
                                                if (listAnswer.size() >= 10) {
                                                    for (int i = listAnswer.size() - 10; i < listAnswer.size(); i++) {
                                                        listLessAnswer.add(listAnswer.get(i));
                                                    }
                                                }
                                                lvAnswer.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        answerAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                                edReply.clearFocus();
                                                edReply.setText("");
                                                scrollMyListViewToBottom();
                                                Log.d("kiemtra", "so answers duoc add: " + response.getContent_answer());
                                                // add relation with Question table
                                                Backendless.Persistence.of(Question.class).addRelation(question, getString(R.string.answers), listAdd,
                                                        new AsyncCallback<Integer>() {
                                                            @Override
                                                            public void handleResponse(Integer response) {
                                                                Log.d("kiemtra", "add relation with question table " + response.toString());
                                                                // add relation with Users table
                                                                Backendless.Persistence.of(Answer.class).addRelation(answer, getString(R.string.user), listUser,
                                                                        new AsyncCallback<Integer>() {
                                                                            @Override
                                                                            public void handleResponse(Integer response) {
                                                                                Log.d("kiemtra ", "add relation with users table" + response.toString());
//                                                            updateListAnswer();
                                                                                checkHideProgress++;
                                                                                checkHideProgressDialog();

                                                                            }

                                                                            @Override
                                                                            public void handleFault(BackendlessFault fault) {
                                                                                Log.d("kiemtra", "add relation with question table " + fault.getMessage());
                                                                            }
                                                                        });
                                                            }

                                                            @Override
                                                            public void handleFault(BackendlessFault fault) {

                                                            }
                                                        });
                                                //update object question

                                                question.setStatus(Consts.WAIT_USER_REPLY);
                                                Backendless.Persistence.of(Question.class).save(question, new AsyncCallback<Question>() {
                                                    @Override
                                                    public void handleResponse(Question response) {
                                                        // set is_reply = true success
                                                        isUpdateMain = true;
                                                        checkHideProgress++;
                                                        checkHideProgressDialog();
                                                        tvStatusQuestion.setBackground(getActivity().getResources().getDrawable(R.drawable.status_question_1));
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {

                                                    }
                                                });


                                            }

                                            public void handleFault(BackendlessFault fault) {
                                                // an error has occurred, the error code can be retrieved with fault.getCode()
                                            }
                                        });

                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Log.d("upload", "fault " + fault.getMessage());
                                    }
                                });
                    } else {
                        bitmap = null;
                        imgPreview.setImageResource(0);
                        imgRemovePreview.setVisibility(View.GONE);
                        final Answer answer = new Answer();
                        KeyboardUtils.hideKeyboard(edReply);
                        String content = edReply.getText().toString().trim();
                        if (content.equals("")) {
                            edReply.setError(getString(R.string.fill_out_content_answer));
                        } else {
                            showProgressDialog(R.string.loading);
                            answer.setContent_answer(content);
                            answer.setUser(currentBackendlessUser);
                            final ArrayList<Answer> listAdd = new ArrayList<Answer>();
                            listAdd.add(answer);
                            final ArrayList<BackendlessUser> listUser = new ArrayList<BackendlessUser>();
                            listUser.add(currentBackendlessUser);
                            // save new answer
                            Backendless.Persistence.save(answer, new AsyncCallback<Answer>() {
                                public void handleResponse(Answer response) {
                                    // save new answer success
                                    // list add response thay vi answer la vi answer khong cos trung created => crash
                                    // add response vi tra ve roi nen co created nhung khong co user -> phai add
                                    response.setUser(currentBackendlessUser);
                                    listAnswer.add(response);
                                    listLessAnswer.clear();
                                    if (listAnswer.size() >= 10) {
                                        for (int i = listAnswer.size() - 10; i < listAnswer.size(); i++) {
                                            listLessAnswer.add(listAnswer.get(i));
                                        }
                                    }
                                    lvAnswer.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            answerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    edReply.clearFocus();
                                    edReply.setText("");
                                    scrollMyListViewToBottom();
                                    Log.d("kiemtra", "so answers duoc add: " + response.getContent_answer());
                                    // add relation with Question table
                                    Backendless.Persistence.of(Question.class).addRelation(question, getString(R.string.answers), listAdd,
                                            new AsyncCallback<Integer>() {
                                                @Override
                                                public void handleResponse(Integer response) {
                                                    Log.d("kiemtra", "add relation with question table " + response.toString());
                                                    // add relation with Users table
                                                    Backendless.Persistence.of(Answer.class).addRelation(answer, getString(R.string.user), listUser,
                                                            new AsyncCallback<Integer>() {
                                                                @Override
                                                                public void handleResponse(Integer response) {
                                                                    Log.d("kiemtra ", "add relation with users table" + response.toString());
//                                                            updateListAnswer();
                                                                    checkHideProgress++;
                                                                    checkHideProgressDialog();

                                                                }

                                                                @Override
                                                                public void handleFault(BackendlessFault fault) {
                                                                    Log.d("kiemtra", "add relation with question table " + fault.getMessage());
                                                                }
                                                            });
                                                }

                                                @Override
                                                public void handleFault(BackendlessFault fault) {

                                                }
                                            });
                                    //update object question

                                    question.setStatus(Consts.WAIT_USER_REPLY);
                                    Backendless.Persistence.of(Question.class).save(question, new AsyncCallback<Question>() {
                                        @Override
                                        public void handleResponse(Question response) {
                                            // set is_reply = true success
                                            isUpdateMain = true;
                                            checkHideProgress++;
                                            checkHideProgressDialog();
                                            tvStatusQuestion.setBackground(getActivity().getResources().getDrawable(R.drawable.status_question_1));
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault fault) {

                                        }
                                    });


                                }

                                public void handleFault(BackendlessFault fault) {
                                    // an error has occurred, the error code can be retrieved with fault.getCode()
                                }
                            });
                        }
                    }
                } else {
                    hideProgressDialog();
                    showAlertNoInternet();
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
                        ImageUtils.startImagePicker(AnswerFragment.this);
                        break;
                    case POSITION_CAMERA:
                        ImageUtils.startCameraForResult(AnswerFragment.this);
                        break;
                }
            }
        });
        builder.show();
    }

    private void checkHideProgressDialog() {
        if (checkHideProgress == 2) {
            checkHideProgress = 0;
            hideProgressDialog();
        }
    }

    private void showAlertNoInternet() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("")
                .setMessage(getActivity().getString(R.string.no_internet_connection))
                .create()
                .show();
    }

    private void startChatWithEmployee(QBUser employee) {
        ChatHelper.getInstance().createDialogWithSelectedUser(employee,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        ChatActivity.start(getActivity(), qbChatDialog);
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
    }


    private void showDialogStatusQuestion() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_status_question);

        final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
        RadioButton rbReplied = (RadioButton) dialog.findViewById(R.id.rb_replied);
        RadioButton rbUnReplied = (RadioButton) dialog.findViewById(R.id.rb_unreplied);
        RadioButton rbAnswered = (RadioButton) dialog.findViewById(R.id.rb_answered);
        Button submit = (Button) dialog.findViewById(R.id.btn_submit);
        int status = question.getStatus();
        if (status == Consts.WAIT_EMPLOYEE_REPLY) {
            rbUnReplied.setChecked(true);
        } else if (status == Consts.WAIT_USER_REPLY) {
            rbReplied.setChecked(true);
        } else {
            rbAnswered.setChecked(true);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idChecked = radioGroup.getCheckedRadioButtonId();
                switch (idChecked) {
                    case R.id.rb_replied:
                        updateQuestionStatus(Consts.WAIT_USER_REPLY);
                        dialog.dismiss();
                        break;
                    case R.id.rb_unreplied:
                        updateQuestionStatus(Consts.WAIT_EMPLOYEE_REPLY);
                        dialog.dismiss();
                        break;
                    case R.id.rb_answered:
                        updateQuestionStatus(Consts.USER_LEAVE_QUESTION);
                        dialog.dismiss();
                        break;
                }
            }
        });
        dialog.show();
    }

    private void updateQuestionStatus(final int status) {
        showProgressDialog(R.string.loading);
        question.setStatus(status);
        Backendless.Persistence.of(Question.class).save(question, new AsyncCallback<Question>() {
            @Override
            public void handleResponse(Question response) {
                // set is_reply = true success
                isUpdateMain = true;
                if (status == Consts.WAIT_EMPLOYEE_REPLY) {
                    tvStatusQuestion.setBackground(getActivity().getResources().getDrawable(R.drawable.status_question_0));
                }
                if (status == Consts.WAIT_USER_REPLY) {
                    tvStatusQuestion.setBackground(getActivity().getResources().getDrawable(R.drawable.status_question_1));
                }
                if (status == Consts.USER_LEAVE_QUESTION) {
                    tvStatusQuestion.setBackground(getActivity().getResources().getDrawable(R.drawable.status_question_2));
                }
                hideProgressDialog();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                hideProgressDialog();
            }
        });

    }

    private void updateListAnswer() {
        String whereclause = "Question[answers].objectId = '" + question.getObjectId() + "'";
        queryAnswer = DataQueryBuilder.create();
        queryAnswer.setWhereClause(whereclause);
        queryAnswer.setSortBy("created ASC");
        queryAnswer.setPageSize(10);
        listAnswer.clear();
        getAllListAnswer();
    }


    private void getAllListAnswer() {
        Log.d("kiemtratime", "bat dau get list answer");
        Backendless.Data.of(Answer.class).find(queryAnswer, new AsyncCallback<List<Answer>>() {
            @Override
            public void handleResponse(List<Answer> response) {
                if (response.size() == 0) {
                    if (listAnswer.size() > 10) {
                        layoutMoreAnswer.setVisibility(View.VISIBLE);
                        for (int i = listAnswer.size() - 10; i < listAnswer.size(); i++) {
                            listLessAnswer.add(listAnswer.get(i));
                        }
                        answerAdapter.setListAnswer(listLessAnswer);
                    } else {
                        answerAdapter.setListAnswer(listAnswer);
                        layoutMoreAnswer.setVisibility(View.GONE);
                    }
                    lvAnswer.post(new Runnable() {
                        @Override
                        public void run() {
                            answerAdapter.notifyDataSetChanged();
                        }
                    });
                    scrollMyListViewToBottom();
                    hideProgressDialog();
                    Log.d("kiemtratime", "xong list answer");
                } else {
                    listAnswer.addAll(response);
                    queryAnswer.prepareNextPage();
                    getAllListAnswer();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });

    }


    private void scrollMyListViewToBottom() {
        lvAnswer.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                lvAnswer.setSelection(answerAdapter.getCount() - 1);
            }
        });
    }

    void showProgressDialog(@StringRes int messageId) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            // Disable the back button
        }

        progressDialog.setMessage(getString(messageId));

        progressDialog.show();

    }

    void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                currentUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), currentUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imgRemovePreview.setVisibility(View.VISIBLE);
                Picasso.with(getContext())
                        .load(currentUri)
                        .noPlaceholder()
                        .centerCrop()
                        .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(120))
                        .into((imgPreview));
//                if(bitmap != null){
//                    float proportion = bitmap.getWidth()/bitmap.getHeight();
//                    int height = (int) (180/proportion);
//                    Picasso.with(getContext())
//                            .load(currentUri)
//                            .noPlaceholder()
//                            .centerCrop()
//                            .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(height))
//                            .into((imgPreview));
//                }else{
//                    Picasso.with(getContext())
//                            .load(currentUri)
//                            .noPlaceholder()
//                            .centerCrop()
//                            .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(120))
//                            .into((imgPreview));
//                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                if (data == null || data.getData() == null) {
                    data = new Intent();
                    data.setData(Uri.fromFile(ImageUtils.getLastUsedCameraFile()));
                }
                currentUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), currentUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imgRemovePreview.setVisibility(View.VISIBLE);
                Picasso.with(getContext())
                        .load(currentUri)
                        .noPlaceholder()
                        .centerCrop()
                        .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(120))
                        .into((imgPreview));
//                if(bitmap != null){
//                    float proportion = bitmap.getWidth()/bitmap.getHeight();
//                    int height = (int) (180/proportion);
//                    Picasso.with(getContext())
//                            .load(currentUri)
//                            .noPlaceholder()
//                            .centerCrop()
//                            .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(height))
//                            .into((imgPreview));
//                }else{
//                    Picasso.with(getContext())
//                            .load(currentUri)
//                            .noPlaceholder()
//                            .centerCrop()
//                            .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(120))
//                            .into((imgPreview));
//                }
            }
        }
    }

    private class FragmentAnswerEditTextWatcher implements TextWatcher {
        private EditText editText;

        private FragmentAnswerEditTextWatcher(EditText editText) {
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
