package tuan.anh.giang.clientemployee.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacksImpl;

import tuan.anh.giang.clientemployee.activities.CallActivity;
import tuan.anh.giang.clientemployee.activities.ChatActivity;
import tuan.anh.giang.clientemployee.activities.EmployeesActivity;
import tuan.anh.giang.clientemployee.activities.MainActivity;

public class WebRtcSessionManager extends QBRTCClientSessionCallbacksImpl {
    private static final String TAG = WebRtcSessionManager.class.getSimpleName();

    private static WebRtcSessionManager instance;
    private Context context;

    private static QBRTCSession currentSession;

    private WebRtcSessionManager(Context context) {
        this.context = context;
    }

    public static WebRtcSessionManager getInstance(Context context){
        if (instance == null){
            instance = new WebRtcSessionManager(context);
        }

        return instance;
    }

    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(QBRTCSession qbCurrentSession) {
        currentSession = qbCurrentSession;
    }

    @Override
    public void onReceiveNewSession(QBRTCSession session) {
        Log.d(TAG, "onReceiveNewSession to WebRtcSessionManager");

        if (currentSession == null){
            setCurrentSession(session);
            Intent intent = new Intent(context, CallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Consts.EXTRA_IS_INCOMING_CALL, true);
            context.startActivity(intent);

        }
    }

    @Override
    public void onSessionClosed(QBRTCSession session) {
        Log.d(TAG, "onSessionClosed WebRtcSessionManager");

        if (session.equals(getCurrentSession())){
            setCurrentSession(null);
        }
    }
}
