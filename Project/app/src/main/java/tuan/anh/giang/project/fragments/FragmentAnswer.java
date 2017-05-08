package tuan.anh.giang.project.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.LoadRelationsQueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tuan.anh.giang.project.R;
import tuan.anh.giang.project.adapters.AnswerAdapter;
import tuan.anh.giang.project.adapters.QuestionAdapter;
import tuan.anh.giang.project.entities.Answer;
import tuan.anh.giang.project.entities.Question;

import static tuan.anh.giang.project.activities.MainActivity.mainActivity;

/**
 * Created by GIANG ANH TUAN on 04/05/2017.
 */

public class FragmentAnswer extends Fragment {
    View view;
    private ProgressDialog progressDialog;
    Question question;
    TextView tvFullName, tvQuestion, tvCreated,tvReply;
    EditText edReply;
    ListView lvAnswer;
    String fullName = "";
    ArrayList<Answer> listAnswer;
    AnswerAdapter answerAdapter;
    DataQueryBuilder queryAnswer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProgressDialog(R.string.loading_answer);
        listAnswer = new ArrayList<>();
        question = (Question) getArguments().getSerializable("question");
        fullName = getArguments().getString("full_name");
        String whereclause = "Question[answers].objectId = '" + question.getObjectId() + "'";
        queryAnswer = DataQueryBuilder.create();
        queryAnswer.setWhereClause(whereclause);
        queryAnswer.setSortBy("created ASC");
        queryAnswer.setPageSize(50);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_answer, container, false);
        findViewById();
        onClick();
        getAllListAnswer();
        return view;
    }

    private void onClick() {
//        edReply.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    v.clearFocus();
//                    tvReply.setVisibility(View.GONE);
//                }
//                return false;
//            }
//        });
//        edReply.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                tvReply.setVisibility(View.VISIBLE);
//            }
//        });
    }


    private void getAllListAnswer() {
        Log.d("kiemtratime", "bat dau get list answer");


        Backendless.Data.of(Answer.class).find(queryAnswer, new AsyncCallback<List<Answer>>() {
            @Override
            public void handleResponse(List<Answer> response) {
                if (response.size() == 0) {
                    answerAdapter = new AnswerAdapter(getActivity(), R.layout.item_list_answer, listAnswer);
                    lvAnswer.setAdapter(answerAdapter);
                    answerAdapter.notifyDataSetChanged();
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

    private void findViewById() {
        tvFullName = (TextView) view.findViewById(R.id.tv_full_name);
        tvQuestion = (TextView) view.findViewById(R.id.tv_question);
        tvCreated = (TextView) view.findViewById(R.id.tv_created);
        lvAnswer = (ListView) view.findViewById(R.id.lv_answer);
        tvReply= (TextView) view.findViewById(R.id.tv_reply);
        edReply = (EditText) view.findViewById(R.id.ed_reply);
        lvAnswer.setVerticalScrollBarEnabled(false);
        tvFullName.setText(fullName);
        tvQuestion.setText(question.getContent());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        tvCreated.setText("● " + sdf.format(question.getCreated()));
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
