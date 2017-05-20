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

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.entities.Question;


public class QuestionAdapter extends ArrayAdapter<Question> {
    Activity context;
    int resource;
    ArrayList<Question> objects;

    public QuestionAdapter(Activity context, int resource, ArrayList<Question> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        View row = inflater.inflate(R.layout.item_list_question, null);

        ImageView imgUser = (ImageView) row.findViewById(R.id.img_user);
        ImageView imgReply = (ImageView) row.findViewById(R.id.img_reply);
        TextView tvCreated = (TextView) row.findViewById(R.id.tv_created);
        TextView tvContent = (TextView) row.findViewById(R.id.tv_content);

        Question question = this.objects.get(position);
        if(question !=null){
            if (question.getStatus() == 0) {
                imgReply.setColorFilter(ContextCompat.getColor(context, R.color.red));
            } else if (question.getStatus() == 1) {
                imgReply.setColorFilter(ContextCompat.getColor(context, R.color.colorFB));
            }else{
                imgReply.setImageResource(R.drawable.success);
                imgReply.setColorFilter(ContextCompat.getColor(context, R.color.action_button_color));
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            tvCreated.setText("● " + sdf.format(question.getCreated()));
            tvContent.setText(question.getContent());
        }
        return row;
    }
}
