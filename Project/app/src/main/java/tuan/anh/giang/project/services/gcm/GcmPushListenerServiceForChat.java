package tuan.anh.giang.project.services.gcm;

import tuan.anh.giang.core.gcm.CoreGcmPushListenerService;
import tuan.anh.giang.core.utils.NotificationUtils;
import tuan.anh.giang.core.utils.ResourceUtils;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.activities.SplashActivity;

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
