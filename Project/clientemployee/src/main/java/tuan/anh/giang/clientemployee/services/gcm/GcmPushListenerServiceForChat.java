package tuan.anh.giang.clientemployee.services.gcm;

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.activities.SplashActivity;
import tuan.anh.giang.core.gcm.CoreGcmPushListenerService;
import tuan.anh.giang.core.utils.NotificationUtils;
import tuan.anh.giang.core.utils.ResourceUtils;


/**
 * Created by GIANG ANH TUAN on 17/05/2017.
 */

public class GcmPushListenerServiceForChat extends CoreGcmPushListenerService {
    private static final int NOTIFICATION_ID = 1;
    @Override
    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_launcher, NOTIFICATION_ID);
    }
}
