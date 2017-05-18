package tuan.anh.giang.project.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tuan.anh.giang.project.R;
import tuan.anh.giang.project.entities.Answer;

/**
 * Created by GIANG ANH TUAN on 04/05/2017.
 */

public class AnswerAdapter extends ArrayAdapter<Answer> {
    Activity context;
    int resource;
    ArrayList<Answer> objects;
    public AnswerAdapter(Activity context, int resource, ArrayList<Answer> objects) {
        super(context, resource, objects);
        this.context=context;
        this.resource = resource;
        this.objects=objects;
    }

    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        View row = inflater.inflate(R.layout.item_list_answer,null);

        ImageView imgUser = (ImageView) row.findViewById(R.id.img_user);
        TextView tvFullName = (TextView) row.findViewById(R.id.tv_full_name);
        TextView tvAnswer = (TextView) row.findViewById(R.id.tv_answer);
        TextView tvCreated = (TextView) row.findViewById(R.id.tv_created);

        Answer answer = objects.get(position);
        // reponse tra ve khong co user
        if (answer != null){
            if((Boolean) answer.getUser().getProperty(context.getString(R.string.is_employee))){
                imgUser.setImageResource(R.drawable.employee);
            }else{
                imgUser.setImageResource(R.drawable.account_circle);
                imgUser.setColorFilter(ContextCompat.getColor(context,R.color.colorFB));
            }
            tvFullName.setText(answer.getUser().getProperty(context.getString(R.string.full_name)).toString());
            tvAnswer.setText(answer.getContent_answer());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            tvCreated.setText("● "+sdf.format(answer.getCreated()));
        }
        return row;
    }
    public void setListAnswer(ArrayList<Answer> listAnswer){
        this.objects = listAnswer;
    }

    @Override
    public int getCount() {
        return objects.size();
    }
}
