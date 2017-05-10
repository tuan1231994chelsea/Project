package tuan.anh.giang.project.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.ArrayList;

import tuan.anh.giang.core.utils.KeyboardUtils;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.entities.Answer;
import tuan.anh.giang.project.entities.Question;

import static tuan.anh.giang.project.activities.MainActivity.mainActivity;

/**
 * Created by GIANG ANH TUAN on 08/05/2017.
 */

public class NewQuestionFragment extends Fragment {
    View view;
    ImageView imgBack;
    EditText edContentQuestion;
    Button btnSubmit;
    public boolean isUpdateMain = false;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_question, container, false);
        findViewById();
        onClick();
        return view;
    }

    private void onClick() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        edContentQuestion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    edContentQuestion.setBackgroundResource(R.drawable.border_content_question1);
                }
                return false;
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edContentQuestion.setBackgroundResource(R.drawable.border_content_question);
                KeyboardUtils.hideKeyboard(edContentQuestion);
                String contentQuestion = edContentQuestion.getText().toString().trim();
                if(contentQuestion.equals("")){
                    edContentQuestion.setError(getString(R.string.fill_out_content_question));
                }else{
                    final ArrayList<BackendlessUser> listUser = new ArrayList<BackendlessUser>();
                    listUser.add(mainActivity.currentBackendlessUser);
                    final Question question = new Question();
                    question.setUser(mainActivity.currentBackendlessUser);
                    question.setContent(contentQuestion);
                    question.setIs_reply(false);
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
                                            getActivity().onBackPressed();
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

    private void findViewById() {
        imgBack = (ImageView) view.findViewById(R.id.img_back);
        edContentQuestion = (EditText) view.findViewById(R.id.ed_content_question);
        btnSubmit = (Button) view.findViewById(R.id.btn_submit);
        edContentQuestion.addTextChangedListener(new FragmentNewQuestionEditTextWatcher(edContentQuestion));
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
