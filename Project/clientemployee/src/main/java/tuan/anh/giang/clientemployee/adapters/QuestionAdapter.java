package tuan.anh.giang.clientemployee.adapters;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import tuan.anh.giang.project.R;
import tuan.anh.giang.project.entities.Question;

/**
 * Created by GIANG ANH TUAN on 03/05/2017.
 */

public class QuestionAdapter extends ArrayAdapter<Question> {
    Activity context;
    int resource;
    ArrayList<Question> objects;
//    private OnScrollToItemQuestionListener listener;

    public QuestionAdapter( Activity context,  int resource,  ArrayList<Question> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource =resource;
        this.objects=objects;
    }

    @Override
    public View getView(int position,  View convertView,  ViewGroup parent) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        View row = inflater.inflate(R.layout.item_list_question,null);

        ImageView imgUser = (ImageView) row.findViewById(R.id.img_user);
        ImageView imgReply = (ImageView) row.findViewById(R.id.img_reply);
        TextView tvCreated = (TextView) row.findViewById(R.id.tv_created);
        TextView tvContent = (TextView) row.findViewById(R.id.tv_content);

        Question question = this.objects.get(position);
        if(question.getIs_reply()){
            imgReply.setColorFilter(ContextCompat.getColor(context,R.color.red));
        }else{
            imgReply.setColorFilter(ContextCompat.getColor(context,R.color.colorFB));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        tvCreated.setText("● "+sdf.format(question.getCreated()));
        tvContent.setText(question.getContent());
//        if (listener != null)
//            listener.onScrollToItem(position);
        return row;
    }
//    public interface OnScrollToItemQuestionListener {
//        void onScrollToItem(int position);
//    }
//
//    public void setOnScrollToItemListener(OnScrollToItemQuestionListener listener) {
//        this.listener = listener;
//    }
}
