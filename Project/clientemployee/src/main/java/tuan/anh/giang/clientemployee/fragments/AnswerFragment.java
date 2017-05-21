package tuan.anh.giang.clientemployee.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
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
import com.backendless.persistence.DataQueryBuilder;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.activities.ChatActivity;
import tuan.anh.giang.clientemployee.activities.MainActivity;
import tuan.anh.giang.clientemployee.adapters.AnswerAdapter;
import tuan.anh.giang.clientemployee.entities.Answer;
import tuan.anh.giang.clientemployee.entities.Question;
import tuan.anh.giang.clientemployee.utils.Consts;
import tuan.anh.giang.clientemployee.utils.chat.ChatHelper;
import tuan.anh.giang.core.utils.KeyboardUtils;
import tuan.anh.giang.core.utils.SharedPrefsHelper;

import static tuan.anh.giang.clientemployee.activities.MainActivity.currentBackendlessUser;


public class AnswerFragment extends Fragment {
    View view;
    private ProgressDialog progressDialog;
    Question question;
    TextView tvFullName, tvQuestion, tvCreated, tvMoreAnswer,tvStatusQuestion;
    EditText edReply;
    LinearLayout layoutMoreAnswer;
    ImageView imgBack, imgMoreAnswer, imgUser, imgSend;
    ListView lvAnswer;
    ArrayList<Answer> listAnswer;
    ArrayList<Answer> listLessAnswer;
    AnswerAdapter answerAdapter;
    DataQueryBuilder queryAnswer;
    public boolean isUpdateMain = false;
    int checkHideProgress = 0;
    String fullName= "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProgressDialog(R.string.loading_answer);
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
        lvAnswer = (ListView) view.findViewById(R.id.lv_answer);
        imgSend = (ImageView) view.findViewById(R.id.img_send);
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
        if(question.getStatus() == Consts.WAIT_EMPLOYEE_REPLY){
            tvStatusQuestion.setBackgroundColor(getActivity().getResources().getColor(R.color.red));
        }if(question.getStatus() ==Consts.WAIT_USER_REPLY){
            tvStatusQuestion.setBackgroundColor(getActivity().getResources().getColor(R.color.colorFB));
        }if (question.getStatus() == Consts.USER_LEAVE_QUESTION){
            tvStatusQuestion.setBackgroundColor(getActivity().getResources().getColor(R.color.press_action_button_color));
        }


    }

    private void onClick() {
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
                final Answer answer = new Answer();
                KeyboardUtils.hideKeyboard(edReply);
                String content = edReply.getText().toString().trim();
                if (content.equals("")) {
                    edReply.setError(getString(R.string.fill_out_content_answer));
                } else {
                    answer.setContent_answer(content);
                    answer.setUser(currentBackendlessUser);
                    final ArrayList<Answer> listAdd = new ArrayList<Answer>();
                    listAdd.add(answer);
                    final ArrayList<BackendlessUser> listUser = new ArrayList<BackendlessUser>();
                    listUser.add(currentBackendlessUser);
                    showProgressDialog(R.string.loading);
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
                                    tvStatusQuestion.setBackgroundColor(getActivity().getResources().getColor(R.color.colorFB));
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
        });

    }
    private void checkHideProgressDialog() {
        if (checkHideProgress == 2) {
            checkHideProgress = 0;
            hideProgressDialog();
        }
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
    private void updateQuestionStatus(final int status){
        showProgressDialog(R.string.loading);
        question.setStatus(status);
        Backendless.Persistence.of(Question.class).save(question, new AsyncCallback<Question>() {
            @Override
            public void handleResponse(Question response) {
                // set is_reply = true success
                isUpdateMain = true;
                if(status == Consts.WAIT_EMPLOYEE_REPLY){
                    tvStatusQuestion.setBackgroundColor(getActivity().getResources().getColor(R.color.red));
                }if(status ==Consts.WAIT_USER_REPLY){
                    tvStatusQuestion.setBackgroundColor(getActivity().getResources().getColor(R.color.colorFB));
                }if (status == Consts.USER_LEAVE_QUESTION){
                    tvStatusQuestion.setBackgroundColor(getActivity().getResources().getColor(R.color.press_action_button_color));
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
//    @Override
//    public void onResume() {
//        super.onResume();

//        view.setFocusableInTouchMode(true);
//        view.requestFocus();
//        view.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
//                    // handle back button's click listener
//                    return true;
//                }
//                return false;
//            }
//        });

//    }


}
