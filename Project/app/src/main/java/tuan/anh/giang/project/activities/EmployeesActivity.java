package tuan.anh.giang.project.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.List;

import tuan.anh.giang.core.utils.ConnectivityUtils;
import tuan.anh.giang.core.utils.Toaster;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.adapters.EmployeeAdapter;
import tuan.anh.giang.project.services.CallService;
import tuan.anh.giang.project.utils.CollectionsUtils;
import tuan.anh.giang.project.utils.Consts;
import tuan.anh.giang.project.utils.PermissionsChecker;
import tuan.anh.giang.project.utils.PushNotificationSender;
import tuan.anh.giang.project.utils.WebRtcSessionManager;
import tuan.anh.giang.project.utils.chat.ChatHelper;


public class EmployeesActivity extends BaseActivity {
    public static final String EXTRA_QB_USERS = "qb_users";
    private static final String EXTRA_QB_DIALOG = "qb_dialog";
    ImageView imgMessage, imgVideoCall, imgPhoneCall, imgBack;
    ListView lvEmployee;
    SwipeRefreshLayout refreshLayout;
    ArrayList<BackendlessUser> listEmployee;
    EmployeeAdapter employeeAdapter;
    DataQueryBuilder dataQueryBuilder;
    BackendlessUser currentBELUser;
    QBUser currentQBUser;
    private boolean isRunForCall;
    private WebRtcSessionManager webRtcSessionManager;
    private PermissionsChecker checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);
        dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setPageSize(20);
        dataQueryBuilder.setWhereClause(getString(R.string.is_employee) + " = true and " + getString(R.string.is_online) + "= true");
        currentBELUser = Backendless.UserService.CurrentUser();
        initFields();
        findViewById();
        onClick();
        updateListEmployees();
        if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
            CallActivity.start(EmployeesActivity.this, true);
        }

        checker = new PermissionsChecker(getApplicationContext());
    }

    public static void start(Context context, boolean isRunForCall) {
        Intent intent = new Intent(context, EmployeesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Consts.EXTRA_IS_STARTED_FOR_CALL, isRunForCall);
        context.startActivity(intent);
    }

    public static void startForResult(Activity activity, int code) {
        startForResult(activity, code, null);
    }

    public static void startForResult(Activity activity, int code, QBChatDialog dialog) {
        Intent intent = new Intent(activity, EmployeesActivity.class);
        intent.putExtra(EXTRA_QB_DIALOG, dialog);
        activity.startActivityForResult(intent, code);
    }

    private void findViewById() {
        lvEmployee = (ListView) findViewById(R.id.lv_employee);
        imgMessage = (ImageView) findViewById(R.id.img_message);
        imgVideoCall = (ImageView) findViewById(R.id.img_videocall);
        imgPhoneCall = (ImageView) findViewById(R.id.img_phonecall);
        imgBack = (ImageView) findViewById(R.id.img_back);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshing_list_employees);
        refreshLayout.setColorSchemeResources(R.color.fb_color);
        listEmployee = new ArrayList<>();
        employeeAdapter = new EmployeeAdapter(this, R.layout.item_employee_list, listEmployee);
        employeeAdapter.setSelectedItemsCountsChangedListener(new EmployeeAdapter.SelectedItemsCountsChangedListener() {
            @Override
            public void onCountSelectedItemsChanged(BackendlessUser item) {

            }
        });
        lvEmployee.setAdapter(employeeAdapter);
    }

    private void onClick() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        imgMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (employeeAdapter.getSelectedItem() != null) {
                    startChatWithEmployee();
                } else {
                    showNotifyDialog("", "Please, choose one participant", R.drawable.error);
                }

            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // update list employees
                updateListEmployees();
            }
        });
        imgVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (employeeAdapter.getSelectedItem() != null) {
                    if (isLoggedInChat()) {
                        startCall(true);
                    }
                    if (checker.lacksPermissions(Consts.PERMISSIONS)) {
                        startPermissionsActivity(false);
                    }
                } else {
                    showNotifyDialog("", "Please, choose one participant", R.drawable.error);
                }
            }
        });
        imgPhoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (employeeAdapter.getSelectedItem() != null) {
                    if (isLoggedInChat()) {
                        startCall(false);
                    }
                    if (checker.lacksPermissions(Consts.PERMISSIONS[1])) {
                        startPermissionsActivity(true);
                    }
                } else {
                    showNotifyDialog("", "Please, choose one participant", R.drawable.error);
                }

            }
        });

    }

    private void startChatWithEmployee() {
        if (ConnectivityUtils.isNetworkConnected()) {
            ChatHelper.getInstance().createDialogWithSelectedUser(employeeAdapter.getSelectedQBUser(),
                    new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                            ChatActivity.start(EmployeesActivity.this, qbChatDialog);
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });
        } else {
            showNotifyDialog("", getString(R.string.no_internet_connection), R.drawable.error);
        }

    }

    private void passResultToCallerActivity() {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>();
        selectedUsers.add(currentQBUser);
        BackendlessUser participantBELUser = employeeAdapter.getSelectedItem();
        QBUser participantQBUser = new QBUser((String) participantBELUser.getProperty(getString(R.string.login)), Consts.DEFAULT_USER_PASSWORD);
        participantQBUser.setId((Integer) participantBELUser.getProperty(getString(R.string.id_qb)));
        participantQBUser.setFullName((String) participantBELUser.getProperty(getString(R.string.full_name)));
        StringifyArrayList<String> tags = new StringifyArrayList<>();
        tags.add((String) participantBELUser.getProperty(getString(R.string.tags)));
        participantQBUser.setTags(tags);
        selectedUsers.add(participantQBUser);
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        setResult(RESULT_OK, result);
        finish();
    }

    private boolean isLoggedInChat() {
        if (!QBChatService.getInstance().isLoggedIn()) {
            Toaster.shortToast(R.string.dlg_signal_error);
            tryReLoginToChat();
            return false;
        }
        return true;
    }

    private void tryReLoginToChat() {
        if (sharedPrefsHelper.hasQbUser()) {
            QBUser qbUser = sharedPrefsHelper.getQbUser();
            CallService.start(this, qbUser);
        }
    }

    private void startCall(boolean isVideoCall) {
        ArrayList<Integer> opponentsList = CollectionsUtils.getIdSelectedEmployee(employeeAdapter.getSelectedItem());
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());

        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);

        WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);

        PushNotificationSender.sendPushMessage(opponentsList, (String) currentBELUser.getProperty(getString(R.string.full_name)));

        CallActivity.start(this, false);
    }

    private void updateListEmployees() {
        refreshLayout.setRefreshing(true);
        listEmployee.clear();
        dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setPageSize(20);
        dataQueryBuilder.setWhereClause(getString(R.string.is_employee) + " = true and " + getString(R.string.is_online) + "= true");
        employeeAdapter.clearSelection();
        getListEmployees();
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(this, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private void initFields() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isRunForCall = extras.getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
        }
        currentQBUser = sharedPrefsHelper.getQbUser();
        webRtcSessionManager = WebRtcSessionManager.getInstance(getApplicationContext());
    }

    private void getListEmployees() {
        if (ConnectivityUtils.isNetworkConnected()) {
            // create lai dataquerybuilder truoc roi chay de quy
            Backendless.Data.of(BackendlessUser.class).find(dataQueryBuilder, new AsyncCallback<List<BackendlessUser>>() {
                @Override
                public void handleResponse(List<BackendlessUser> response) {
                    if (response.size() != 0) {
                        listEmployee.addAll(response);
                        dataQueryBuilder.prepareNextPage();
                        getListEmployees();
                    } else {
                        lvEmployee.post(new Runnable() {
                            @Override
                            public void run() {
                                employeeAdapter.notifyDataSetChanged();
                            }
                        });
                        refreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    refreshLayout.setRefreshing(false);
                }
            });
        } else {
            refreshLayout.setRefreshing(false);
            showNotifyDialog("", getString(R.string.no_internet_connection), R.drawable.error);
        }

    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.list_employees);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lvEmployee.post(new Runnable() {
            @Override
            public void run() {
                employeeAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            isRunForCall = intent.getExtras().getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
            if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
                CallActivity.start(EmployeesActivity.this, true);
            }
        }
    }
}
