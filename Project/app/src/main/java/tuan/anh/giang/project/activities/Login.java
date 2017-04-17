package tuan.anh.giang.project.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.quickblox.auth.session.QBSessionManager;

import tuan.anh.giang.project.R;

/**
 * Created by GIANG ANH TUAN on 14/04/2017.
 */

public class Login extends AppCompatActivity{
    EditText userName,password;
    TextView register,forgotPassWord;
    Button submit,loginWithFB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userName = (EditText) findViewById(R.id.user_name);
        password= (EditText) findViewById(R.id.password);
        register= (TextView) findViewById(R.id.tv_register);
        forgotPassWord= (TextView) findViewById(R.id.tv_forgotPassword);
        submit = (Button) findViewById(R.id.bt_submit);
        loginWithFB = (Button) findViewById(R.id.bt_loginWithFB);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    protected boolean checkSignIn() {
        return QBSessionManager.getInstance().getSessionParameters() != null;
    }

}
