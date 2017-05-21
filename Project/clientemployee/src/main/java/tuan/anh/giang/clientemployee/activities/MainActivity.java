package tuan.anh.giang.clientemployee.activities;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.helper.Utils;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import tuan.anh.giang.clientemployee.R;
import tuan.anh.giang.clientemployee.adapters.OpponentsAdapter;
import tuan.anh.giang.clientemployee.adapters.QuestionAdapter;
import tuan.anh.giang.clientemployee.db.QbUsersDbManager;
import tuan.anh.giang.clientemployee.entities.Question;
import tuan.anh.giang.clientemployee.fragments.AnswerFragment;
import tuan.anh.giang.clientemployee.fragments.UserInforFragment;
import tuan.anh.giang.clientemployee.services.CallService;
import tuan.anh.giang.clientemployee.utils.Consts;
import tuan.anh.giang.clientemployee.utils.PermissionsChecker;
import tuan.anh.giang.clientemployee.utils.QBEntityCallbackImpl;
import tuan.anh.giang.clientemployee.utils.UsersUtils;
import tuan.anh.giang.clientemployee.utils.WebRtcSessionManager;
import tuan.anh.giang.clientemployee.utils.chat.ChatHelper;
import tuan.anh.giang.clientemployee.utils.qb.QbDialogHolder;
import tuan.anh.giang.core.utils.SharedPrefsHelper;
import tuan.anh.giang.core.utils.Toaster;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);
    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsListView;
    private ArrayList<QBUser> currentOpponentsList;
    private QbUsersDbManager dbManager;
//    private boolean isRunForCall;
//    private WebRtcSessionManager webRtcSessionManager;
    private QBUser userForSave;
    private PermissionsChecker checker;
    private ArrayList<BackendlessUser> employeeList;
    public static DrawerLayout drawer;
    private ImageView imgMenu;
    public static BackendlessUser currentBackendlessUser;
    public static QBUser currentQBUser;
    private ArrayList<Question> listOldQuestion;
    private ListView lvOldQuestion;
    private TextView tvNewQuestion, tvTitle;
    private QuestionAdapter questionAdapter;
    public static FragmentManager fragmentManager;
    public static MainActivity mainActivity;
    DataQueryBuilder queryQuestion;
    Fragment currentFragment;
    SwipeRefreshLayout refreshLayout;
    boolean isAllOfQuestion = false;
    FloatingActionButton oldChat,conditionLoad;
    String loadQuestionByStatus = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        Log.d("kiemtratime", "bat dau mainactivity");
        fragmentManager = getSupportFragmentManager();
        queryQuestion = DataQueryBuilder.create();
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        // load all String ="", status =0 -> String = "and status =0" ...
        loadQuestionByStatus = sharedPrefsHelper.getConditionLoadQuestion();
        listOldQuestion = new ArrayList<>();
//        initFields();
        findViewById();
        onClick();
        getCurrentBELUser();
//        if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
//            CallActivity.start(MainActivity.this, true);
//        }
    }
//    private void initFields() {
//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {
//            isRunForCall = extras.getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
//        }
//        webRtcSessionManager = WebRtcSessionManager.getInstance(getApplicationContext());
//    }

    private void getCurrentBELUser() {
        showProgressDialog(R.string.loading);
        currentBackendlessUser = Backendless.UserService.CurrentUser();
        if (currentBackendlessUser == null) {
            Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
                @Override
                public void handleResponse(Boolean response) {
                    if (response && Backendless.UserService.CurrentUser() == null) {
                        String currentUserId = Backendless.UserService.loggedInUser();
                        if (!currentUserId.equals("")) {
                            Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser response) {
                                    Backendless.UserService.setCurrentUser(response);
                                    currentBackendlessUser = response;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tvTitle.setText("Hello " + currentBackendlessUser.getProperty(getString(R.string.full_name)).toString());
                                        }
                                    });
                                    String loginQBUser = (String) currentBackendlessUser.getProperty(getString(R.string.login));
                                    currentQBUser = new QBUser(loginQBUser, Consts.DEFAULT_USER_PASSWORD);
                                    signInCreatedUser(currentQBUser, false);
                                    getOldQuestionFirst();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });
        } else {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTitle.setText("Hello " + currentBackendlessUser.getProperty(getString(R.string.full_name)).toString());
                }
            });
            String loginQBUser = (String) currentBackendlessUser.getProperty(getString(R.string.login));
            currentQBUser = new QBUser(loginQBUser, Consts.DEFAULT_USER_PASSWORD);
            signInCreatedUser(currentQBUser, false);
            getOldQuestionFirst();
        }
    }

    // first load old question, load first page
    private void getOldQuestionFirst() {
        isAllOfQuestion = false;
        // status question = 0 => cho nhan vien tra loi
        // = 1=> cho nguoi dung phan hoi
        // = 2 => nguoi dung leave question
        loadQuestionByStatus = sharedPrefsHelper.getConditionLoadQuestion();
        queryQuestion.setWhereClause(loadQuestionByStatus);
        queryQuestion.setSortBy("created DESC");
        queryQuestion.setPageSize(20);
        Backendless.Data.of(Question.class).find(queryQuestion, new AsyncCallback<List<Question>>() {
            @Override
            public void handleResponse(List<Question> response) {
                if (response.size() < 20) {
                    isAllOfQuestion = true;
                }
                listOldQuestion.addAll(response);
                lvOldQuestion.post(new Runnable() {
                    @Override
                    public void run() {
                        questionAdapter.notifyDataSetChanged();
                        hideProgressDialog();
                    }
                });

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("myapp", fault.getMessage());
            }
        });
    }


    /**
     * update first page question
     */
    private void updateOldQuestion() {
        isAllOfQuestion = false;
        refreshLayout.setRefreshing(true);
        queryQuestion = DataQueryBuilder.create();
        loadQuestionByStatus = sharedPrefsHelper.getConditionLoadQuestion();
        queryQuestion.setWhereClause(loadQuestionByStatus);
        queryQuestion.setSortBy("created DESC");
        queryQuestion.setPageSize(20);
        listOldQuestion.clear();
        Backendless.Data.of(Question.class).find(queryQuestion, new AsyncCallback<List<Question>>() {
            @Override
            public void handleResponse(List<Question> response) {
                if (response.size() < 20) {
                    isAllOfQuestion = true;
                }
                listOldQuestion.addAll(response);
                lvOldQuestion.post(new Runnable() {
                    @Override
                    public void run() {
                        questionAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }
                });

                Log.d("kiemtratime", "load xong questions");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("myapp", fault.getMessage());
            }
        });
    }

    // load more 1 page question
    private void loadMoreQuestion() {
        showProgressDialog(R.string.loading_more_questions);
        loadQuestionByStatus = sharedPrefsHelper.getConditionLoadQuestion();
        queryQuestion.setWhereClause(loadQuestionByStatus);
        queryQuestion.prepareNextPage();
        Backendless.Data.of(Question.class).find(queryQuestion, new AsyncCallback<List<Question>>() {
            @Override
            public void handleResponse(List<Question> response) {
                if (response.size() != 0) {
                    if (response.size() < 20) {
                        isAllOfQuestion = true;
                    }
                    listOldQuestion.addAll(response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            questionAdapter.notifyDataSetChanged();
                            hideProgressDialog();
                        }
                    });
                } else {
                    isAllOfQuestion = true;
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("myapp", fault.getMessage());
            }
        });
    }


    private void onClick() {
        conditionLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show dialog set
                showDialogConditionLoad();
            }
        });
        oldChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogsActivity.start(MainActivity.this);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateOldQuestion();
            }
        });
        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (drawer.isDrawerVisible(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    } else {
                        drawer.openDrawer(GravityCompat.START);
                    }
                } catch (Exception e) {
                    drawer.openDrawer(Gravity.LEFT);
                }
            }
        });
        lvOldQuestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Question clickQuestion = listOldQuestion.get(i);
                currentFragment = new AnswerFragment();
                Bundle bundle = new Bundle();
                Question question = new Question();
                question.setStatus(clickQuestion.getStatus());
                question.setContent(clickQuestion.getContent());
                question.setCreated(clickQuestion.getCreated());
                question.setObjectId(clickQuestion.getObjectId());
                question.setUpdated(clickQuestion.getUpdated());
                bundle.putSerializable("question", question);
                bundle.putString("FullName", (String) clickQuestion.getUser().getProperty(getString(R.string.full_name)));
                currentFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.root_view_main_activity, currentFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }
    private void showDialogConditionLoad() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_condition_load_question);


        final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
        RadioButton rbReplied = (RadioButton) dialog.findViewById(R.id.rb_replied);
        RadioButton rbUnReplied = (RadioButton) dialog.findViewById(R.id.rb_unreplied);
        RadioButton rbAnswered = (RadioButton) dialog.findViewById(R.id.rb_answered);
        RadioButton rbLoadAll = (RadioButton) dialog.findViewById(R.id.rb_load_all);
        Button submit = (Button) dialog.findViewById(R.id.btn_submit);

        loadQuestionByStatus = sharedPrefsHelper.getConditionLoadQuestion();
        if (loadQuestionByStatus.equals(Consts.LOAD_ALL_QUESTION)) {
            rbLoadAll.setChecked(true);
        } else if (loadQuestionByStatus.equals(Consts.CONDITION_WAIT_EMPLOYEE)) {
            rbUnReplied.setChecked(true);
        } else if (loadQuestionByStatus.equals(Consts.CONDITION_WAIT_USER)) {
            rbReplied.setChecked(true);
        } else {
            rbAnswered.setChecked(true);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idChecked = radioGroup.getCheckedRadioButtonId();
                switch (idChecked) {
                    case R.id.rb_replied:
                        loadQuestionByStatus = Consts.CONDITION_WAIT_USER;
                        sharedPrefsHelper.save(Consts.LOAD_QUESTION_BY_STATUS, loadQuestionByStatus);
                        updateOldQuestion();
                        dialog.dismiss();
                        break;
                    case R.id.rb_unreplied:
                        loadQuestionByStatus = Consts.CONDITION_WAIT_EMPLOYEE;
                        sharedPrefsHelper.save(Consts.LOAD_QUESTION_BY_STATUS, loadQuestionByStatus);
                        updateOldQuestion();
                        dialog.dismiss();
                        break;
                    case R.id.rb_answered:
                        loadQuestionByStatus = Consts.CONDITION_USER_LEAVE_QUESTION;
                        sharedPrefsHelper.save(Consts.LOAD_QUESTION_BY_STATUS, loadQuestionByStatus);
                        updateOldQuestion();
                        dialog.dismiss();
                        break;
                    case R.id.rb_load_all:
                        loadQuestionByStatus = Consts.LOAD_ALL_QUESTION;
                        sharedPrefsHelper.save(Consts.LOAD_QUESTION_BY_STATUS, loadQuestionByStatus);
                        updateOldQuestion();
                        dialog.dismiss();
                        break;

                }
            }
        });
        dialog.show();
    }

    private void findViewById() {
        mainActivity = this;
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View v = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        imgMenu = (ImageView) findViewById(R.id.img_menu);
        lvOldQuestion = (ListView) findViewById(R.id.lv_old_question);
        tvNewQuestion = (TextView) findViewById(R.id.tv_new_question);
        tvTitle = (TextView) findViewById(R.id.toolbar_title);
        lvOldQuestion.setVerticalScrollBarEnabled(false);
        oldChat = (FloatingActionButton) findViewById(R.id.action_old_chat);
        conditionLoad = (FloatingActionButton) findViewById(R.id.action_condition_load);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipetop);
        refreshLayout.setColorSchemeResources(R.color.fb_color);
        questionAdapter = new QuestionAdapter(mainActivity, R.layout.item_list_question, listOldQuestion);
        lvOldQuestion.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                if (!isAllOfQuestion && listOldQuestion.size() != 0) {
                    if (((listOldQuestion.size() - 1) == lastVisibleItem))
                        loadMoreQuestion();
                }
            }
        });
        lvOldQuestion.setAdapter(questionAdapter);
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.root_view_main_activity);
    }

    private void getListEmployeeFromBEL() {
        if (employeeList == null) {
            employeeList = new ArrayList<>();
        }
        employeeList.clear();
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("is_employee = true");
        Backendless.Data.of(BackendlessUser.class).find(queryBuilder, new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> users) {
                Log.d("myapp", "get list employee success");
                for (BackendlessUser user : users) {
                    employeeList.add(user);
                }
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Log.d("myapp", "loi get list employee");
            }
        });

    }

//    private boolean checkHasBELUser() {
//        if (sharedPrefsHelper == null) {
//            sharedPrefsHelper = SharedPrefsHelper.getInstance();
//        }
//        if (!sharedPrefsHelper.hasBELUser()) {
//            return false;
//        } else {
//            return true;
//        }
//    }

    /**
     * check if hasQb user in sharepreferances -> return true
     * else sign up new QbUser by current backendless user and login then
     *
     * @return
     */
//    private boolean checkHasQbUser() {
//        if (sharedPrefsHelper == null) {
//            sharedPrefsHelper = SharedPrefsHelper.getInstance();
//        }
//        if (!sharedPrefsHelper.hasQbUser()) {
//            return false;
//        } else {
//            return true;
//        }
//    }
    public static void start(Context context, boolean isRunForCall) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Consts.EXTRA_IS_STARTED_FOR_CALL, isRunForCall);
        context.startActivity(intent);
    }

    private QBUser createQBUserWithCurrentData(BackendlessUser user) {
        QBUser qbUser = null;
        String userName = user.getProperty(getString(R.string.user_name)).toString();
        String fullName = user.getProperty(getString(R.string.full_name)).toString();
        if (!TextUtils.isEmpty(userName)) {
            StringifyArrayList<String> userTags = new StringifyArrayList<>();
            userTags.add(userName);

            qbUser = new QBUser();
            qbUser.setFullName(fullName);
            qbUser.setLogin(userName);
            qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
            qbUser.setTags(userTags);
        }
        return qbUser;
    }

    // user quickblox
    private void startSignUpNewUser(final QBUser newUser) {
//        showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.d("myapp", "signUp qb user success");
                        Log.d("kiemtratime", "dang ky xong QB user");
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
//                            signInCreatedUser(newUser, true);
                            Log.d("myapp", "error signUp qb user ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS ");
                        } else {
                            hideProgressDialog();
                            Toaster.longToast(R.string.sign_up_error);
                        }
                    }
                }
        );
    }

    private void loginToChat(final QBUser qbUser) {
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
        userForSave = qbUser;
        startLoginService(qbUser);
    }

    private void startLoginService(QBUser qbUser) {
        Intent tempIntent = new Intent(this, CallService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(this, qbUser, pendingIntent);
    }

    private String getCurrentDeviceId() {
        return Utils.generateDeviceId(this);
    }

    private void saveUserData(QBUser qbUser) {
//        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        sharedPrefsHelper.save(Consts.PREF_CURREN_ROOM_NAME, qbUser.getTags().get(0));
        sharedPrefsHelper.saveQbUser(qbUser);
    }

    private void signInCreatedUser(final QBUser user, final boolean deleteCurrentUser) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                Log.d("myapp", "sign in thanh cong QbUser");
                Log.d("kiemtratime", "dang nhap QB user thanh cong");
                currentQBUser = result;
                currentQBUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
                startLoginService(currentQBUser);
                userForSave = currentQBUser;
                saveUserData(currentQBUser);
//                if (deleteCurrentUser) {
//                    removeAllUserData(result);
//                } else {
//
////                    startOpponentsActivity();
//                }
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

    private void removeAllUserData(final QBUser user) {
        requestExecutor.deleteCurrentUser(user.getId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                UsersUtils.removeUserData(getApplicationContext());
                startSignUpNewUser(createQBUserWithCurrentData(currentBackendlessUser));
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

    private boolean checkSignIn() {
        return QBSessionManager.getInstance().getSessionParameters() != null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        questionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
            hideProgressDialog();
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess) {
                Log.d("myapp", "login qb user to chat success");
                Log.d("kiemtratime", "login to chat thanh cong");
//                saveUserData(userForSave);
//                signInCreatedUser(currentQBUser, false);
            } else {
                Toaster.longToast(getString(R.string.login_chat_login_error) + errorMessage);
            }
        }
    }

    public void showListEmployee() {
        EmployeesActivity.start(mainActivity, false);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_user_info:
                showInfoUser();
                break;
            case R.id.nav_video_setting:
                showVideoSettings();
                break;
            case R.id.nav_rate:

                break;
            case R.id.nav_feedback:

                break;
            case R.id.nav_share:
                break;
            case R.id.nav_logout:
                logOut();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showVideoSettings() {
        SettingsActivity.start(this);
    }

    private void showInfoUser() {
        currentFragment = new UserInforFragment();
        fragmentManager.beginTransaction().replace(R.id.root_view_main_activity, currentFragment)
                .addToBackStack(null)
                .commit();
    }

    private void logOut() {
        showProgressDialog(R.string.dlg_logout);
        logOutChat();
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                // user has been logged out.
                sharedPrefsHelper.removeBELUser();
                LoginActivity.start(mainActivity);
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("error", fault.getMessage());
            }
        });
    }
    // logout chat va video call
    private void logOutChat() {
        unsubscribeFromPushes();
        ChatHelper.getInstance().destroy();
        QbDialogHolder.getInstance().clear();
        startLogoutCommand();
        UsersUtils.removeUserData(getApplicationContext());
        final QBChatService chatService = QBChatService.getInstance();
        boolean login = QBSessionManager.getInstance().getSessionParameters() != null;
        boolean logintochat = chatService.isLoggedIn();

        if (logintochat) {
            //logout chat video chat
            chatService.logout(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    int i = 0;
                }

                @Override
                public void onError(QBResponseException e) {
                }
            });
        }

        if(login){
            // logout user
            QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    int i = 0;
                }

                @Override
                public void onError(QBResponseException e) {
                }
            });
        }
    }
    private void startLogoutCommand() {
        CallService.logout(this);
    }
    private void unsubscribeFromPushes() {
        SubscribeService.unSubscribeFromPushes(this);
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fragmentManager.popBackStack();
            if (currentFragment instanceof AnswerFragment) {
                if (((AnswerFragment) currentFragment).isUpdateMain) {
                    updateOldQuestion();
                }
            }
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
            super.onBackPressed();
        }
    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if (intent.getExtras() != null) {
//            isRunForCall = intent.getExtras().getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
//            if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
//                CallActivity.start(MainActivity.this, true);
//            }
//        }
//    }
}
