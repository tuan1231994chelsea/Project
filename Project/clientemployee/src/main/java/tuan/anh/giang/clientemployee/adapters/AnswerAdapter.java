package tuan.anh.giang.clientemployee.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.activities.AttachmentImageActivity;
import tuan.anh.giang.clientemployee.entities.Answer;
import tuan.anh.giang.core.utils.ResourceUtils;


public class AnswerAdapter extends ArrayAdapter<Answer> {
    Activity context;
    int resource;
    ArrayList<Answer> objects;

    public AnswerAdapter(Activity context, int resource, ArrayList<Answer> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        View row = inflater.inflate(R.layout.item_list_answer, null);

        ImageView imgUser = (ImageView) row.findViewById(R.id.img_user);
        final ImageView imgImageAnswer = (ImageView) row.findViewById(R.id.img_image_answer);
        TextView tvFullName = (TextView) row.findViewById(R.id.tv_full_name);
        TextView tvAnswer = (TextView) row.findViewById(R.id.tv_answer);
        TextView tvCreated = (TextView) row.findViewById(R.id.tv_created);

        final Answer answer = objects.get(position);
        // reponse tra ve khong co user
        if (answer != null) {
            if ((Boolean) answer.getUser().getProperty(context.getString(R.string.is_employee))) {
                imgUser.setImageResource(R.drawable.employee);
            } else {
                imgUser.setImageResource(R.drawable.account_circle);
                imgUser.setColorFilter(ContextCompat.getColor(context, R.color.colorFB));
            }
            if (!(answer.getImage() == null || answer.getImage().equals(""))) {
//                Picasso.with(context).load(answer.getImage()).into(new Target() {
//                    @Override
//                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                        int width = bitmap.getWidth();
//                        int height = bitmap.getHeight();
//                        float proportion = width / height;
//                        if (bitmap != null) {
//                            int heightImage = (int) (180 / proportion);
//                            Picasso.with(context)
//                                    .load(answer.getImage())
//                                    .noPlaceholder()
//                                    .centerCrop()
//                                    .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(heightImage))
//                                    .into((imgImageAnswer));
//                        } else {
//                            Picasso.with(context)
//                                    .load(answer.getImage())
//                                    .noPlaceholder()
//                                    .centerCrop()
//                                    .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(120))
//                                    .into((imgImageAnswer));
//                        }
//                    }
//
//                    @Override
//                    public void onBitmapFailed(Drawable errorDrawable) {
//
//                    }
//
//                    @Override
//                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//                        //Here you should place a loading gif in the ImageView to
//                        //while image is being obtained.
//                    }
//                });
                Picasso.with(context)
                        .load(answer.getImage())
                        .noPlaceholder()
                        .centerCrop()
                        .resize(ResourceUtils.dpToPx(180), ResourceUtils.dpToPx(120))
                        .into((imgImageAnswer));
            }
            tvFullName.setText(answer.getUser().getProperty(context.getString(R.string.full_name)).toString());
            if(answer.getContent_answer().equals("")){
                tvAnswer.setVisibility(View.GONE);
            }else{
                tvAnswer.setText(answer.getContent_answer());
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            tvCreated.setText("● " + sdf.format(answer.getCreated()));
            imgImageAnswer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(answer.getImage() == null || answer.getImage().equals(""))) {
                        AttachmentImageActivity.start(context, answer.getImage());
                    }
                }
            });
        }

        return row;
    }

    public void setListAnswer(ArrayList<Answer> listAnswer) {
        this.objects = listAnswer;
    }

    @Override
    public int getCount() {
        return objects.size();
    }
}
