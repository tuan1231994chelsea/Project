package tuan.anh.giang.project.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by GIANG ANH TUAN on 23/04/2017.
 */

public class MyTextWatcher implements TextWatcher {
    EditText editText;

    public MyTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        editText.setError(null);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
