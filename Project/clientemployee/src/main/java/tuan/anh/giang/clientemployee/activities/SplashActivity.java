package tuan.anh.giang.clientemployee.activities;

import android.os.Bundle;

import com.backendless.persistence.local.UserTokenStorageFactory;
import com.quickblox.users.model.QBUser;

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.services.CallService;
import tuan.anh.giang.core.ui.activity.CoreSplashActivity;
import tuan.anh.giang.core.utils.SharedPrefsHelper;


public class SplashActivity extends CoreSplashActivity {
        private SharedPrefsHelper sharedPrefsHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(checkConfigsWithSnackebarError()){
            String userToken = UserTokenStorageFactory.instance().getStorage().get();
            sharedPrefsHelper=SharedPrefsHelper.getInstance();
            if (userToken != null && !userToken.equals("")) {
                // backendlessUser  login is available, skip the login activity/login form
                startOpponentsActivity();
            }else{
                proceedToTheNextActivityWithDelay();
            }
        }

    }

    private void startOpponentsActivity() {
        MainActivity.start(SplashActivity.this, false);
        finish();
    }
    protected void startLoginService(QBUser qbUser) {
        CallService.start(this, qbUser);
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        LoginActivity.start(this);
        finish();
    }

}
