package tuan.anh.giang.clientemployee.adapters;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.BackendlessUser;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.utils.Consts;
import tuan.anh.giang.core.utils.ResourceUtils;
import tuan.anh.giang.core.utils.UiUtils;


public class EmployeeAdapter extends ArrayAdapter<BackendlessUser> {
    Activity context;
    BackendlessUser selectedItem;
    @LayoutRes
    int resource;
    @NonNull
    ArrayList<BackendlessUser> objects;
    private SelectedItemsCountsChangedListener selectedItemsCountChangedListener;

    public EmployeeAdapter(@NonNull Activity context, @LayoutRes int resource, @NonNull ArrayList<BackendlessUser> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
        selectedItem = null;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        convertView = inflater.inflate(R.layout.item_employee_list, null);

        ImageView imgEmployee = (ImageView) convertView.findViewById(R.id.img_employee);
        TextView tvFullName = (TextView) convertView.findViewById(R.id.tv_full_name);

        BackendlessUser user = objects.get(position);

        if ( selectedItem!=null && selectedItem.getObjectId().toString().equals(user.getObjectId().toString())) {
            convertView.setBackgroundResource(R.color.background_color_selected_user_item);
            imgEmployee.setBackgroundDrawable(
                    UiUtils.getColoredCircleDrawable(ResourceUtils.getColor(R.color.icon_background_color_selected_user)));
            imgEmployee.setImageResource(R.drawable.ic_checkmark);
        } else {
            convertView.setBackgroundResource(R.color.background_color_normal_user_item);
            imgEmployee.setBackgroundDrawable(UiUtils.getColorCircleDrawable((Integer) user.getProperty(context.getString(R.string.id_qb))));
            imgEmployee.setImageResource(R.drawable.employee);
        }

        tvFullName.setText(user.getProperty(context.getString(R.string.full_name)).toString());


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSelection(position);
                selectedItemsCountChangedListener.onCountSelectedItemsChanged(selectedItem);
            }
        });

        return convertView;
    }

    public void toggleSelection(int position) {
        BackendlessUser item = getItem(position);
        toggleSelection(item);
    }

    public void toggleSelection(BackendlessUser item) {
        if (selectedItem !=null && selectedItem.getObjectId().toString().equals(item.getObjectId().toString())) {
            selectedItem = null;
        } else {
            selectedItem = item;
        }
        notifyDataSetChanged();
    }

    public void selectItem(int position) {
        BackendlessUser item = getItem(position);
        selectItem(item);
    }

    public void selectItem(BackendlessUser item) {
        if (selectedItem.getObjectId().toString().equals(item.getObjectId().toString())) {
            return;
        }
        selectedItem = item;
        notifyDataSetChanged();
    }

    public BackendlessUser getSelectedItem() {
        return selectedItem;
    }
    public QBUser getSelectedQBUser(){
        BackendlessUser backendlessUser = getSelectedItem();
        QBUser qbUser = new QBUser((String) backendlessUser.getProperty(context.getString(R.string.login)), Consts.DEFAULT_USER_PASSWORD);
        qbUser.setId((Integer) backendlessUser.getProperty(context.getString(R.string.id_qb)));
        qbUser.setFullName((String) backendlessUser.getProperty(context.getString(R.string.full_name)));
        StringifyArrayList<String> tags = new StringifyArrayList<>();
        tags.add((String) backendlessUser.getProperty(context.getString(R.string.tags)));
        qbUser.setTags(tags);
        return qbUser;
    }

    protected boolean isItemSelected(int position) {
        return selectedItem.getObjectId().toString().equals(getItem(position).getObjectId().toString());
    }

    public void clearSelection() {
        selectedItem = null;
        notifyDataSetChanged();
    }

    public void setSelectedItemsCountsChangedListener(SelectedItemsCountsChangedListener selectedItemsCountsChanged) {
        if (selectedItemsCountsChanged != null) {
            this.selectedItemsCountChangedListener = selectedItemsCountsChanged;
        }
    }

    public interface SelectedItemsCountsChangedListener {
        void onCountSelectedItemsChanged(BackendlessUser item);
    }
}
