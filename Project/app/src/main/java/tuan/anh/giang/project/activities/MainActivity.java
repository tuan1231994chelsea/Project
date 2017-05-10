package tuan.anh.giang.project.activities;

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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.helper.Utils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import tuan.anh.giang.core.utils.SharedPrefsHelper;
import tuan.anh.giang.core.utils.Toaster;
import tuan.anh.giang.project.R;
import tuan.anh.giang.project.adapters.OpponentsAdapter;
import tuan.anh.giang.project.adapters.QuestionAdapter;
import tuan.anh.giang.project.db.QbUsersDbManager;
import tuan.anh.giang.project.entities.Answer;
import tuan.anh.giang.project.entities.Question;
import tuan.anh.giang.project.fragments.AnswerFragment;
import tuan.anh.giang.project.fragments.NewQuestionFragment;
import tuan.anh.giang.project.services.CallService;
import tuan.anh.giang.project.utils.Consts;
import tuan.anh.giang.project.utils.PermissionsChecker;
import tuan.anh.giang.project.utils.QBEntityCallbackImpl;
import tuan.anh.giang.project.utils.UsersUtils;
import tuan.anh.giang.project.utils.WebRtcSessionManager;

/**
 * Created by GIANG ANH TUAN on 17/04/2017.
 */

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);
    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsListView;
    private QBUser currentQBUser;
    private ArrayList<QBUser> currentOpponentsList;
    private QbUsersDbManager dbManager;
    private boolean isRunForCall;
    private WebRtcSessionManager webRtcSessionManager;
    private QBUser userForSave;
    private PermissionsChecker checker;
    private ArrayList<BackendlessUser> employeeList;
    public static DrawerLayout drawer;
    private ImageView imgMenu;
    public static BackendlessUser currentBackendlessUser;
    private ArrayList<Question> listOldQuestion;
    private ListView lvOldQuestion;
    private TextView tvNewQuestion;
    private QuestionAdapter questionAdapter;
    public static FragmentManager fragmentManager;
    public static MainActivity mainActivity;
    DataQueryBuilder queryQuestion;
    Fragment currentFragment;
    SwipeRefreshLayout refreshLayout;
    boolean isAllOfQuestion = false;


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
        listOldQuestion = new ArrayList<>();
        findViewById();
        onClick();
        getCurrentBELUser();


//        deleteAndSaveNewBackendUser();
//        getListEmployeeFromBEL();


    }

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
                                    // check sharepreferences haven't Backendless User -> save current BELUser
                                    Log.d("kiemtratime", "lay duoc current backendless user");
                                    if (!checkHasBELUser()) {
                                        sharedPrefsHelper.saveBELUser(currentBackendlessUser);
                                    }
                                    /** check sharepreferences haven't QuickBlox User -> Sign Up new Quickblox User by
                                     * current BEL User -> save QB User and sign in this user
                                     * else get QB user from sharepreferences and sign in this user
                                     **/
                                    if (!checkHasQbUser()) {
                                        startSignUpNewUser(createQBUserWithCurrentData(currentBackendlessUser));
                                    } else {
                                        signInCreatedUser(sharedPrefsHelper.getQbUser(), false);
                                    }
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
            // check sharepreferences haven't Backendless User -> save current BELUser
            if (!checkHasBELUser()) {
                sharedPrefsHelper.saveBELUser(currentBackendlessUser);
            }
            /** check sharepreferences haven't QuickBlox User -> Sign Up new Quickblox User by
             * current BEL User -> save QB User and sign in this user
             * else get QB user from sharepreferences and sign in this user
             **/
            if (!checkHasQbUser()) {
                startSignUpNewUser(createQBUserWithCurrentData(currentBackendlessUser));
            } else {
                signInCreatedUser(sharedPrefsHelper.getQbUser(), false);
            }
            getOldQuestionFirst();
        }
    }

    // first load old question, load first page
    private void getOldQuestionFirst() {
        isAllOfQuestion = false;
        queryQuestion.setWhereClause("user.objectId = '" + currentBackendlessUser.getObjectId() + "'");
        queryQuestion.setSortBy("created DESC");
        queryQuestion.setPageSize(10);
        Backendless.Data.of(Question.class).find(queryQuestion, new AsyncCallback<List<Question>>() {
            @Override
            public void handleResponse(List<Question> response) {
                if (response.size() < 10) {
                    isAllOfQuestion = true;
                }
                listOldQuestion.addAll(response);
                lvOldQuestion.post(new Runnable() {
                    @Override
                    public void run() {
                        questionAdapter.notifyDataSetChanged();
                    }
                });
                hideProgressDialog();
                Log.d("kiemtratime", "load xong questions");
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
        showProgressDialog(R.string.refreshing_your_question);
        queryQuestion = DataQueryBuilder.create();
        queryQuestion.setWhereClause("user.objectId = '" + currentBackendlessUser.getObjectId() + "'");
        queryQuestion.setSortBy("created DESC");
        queryQuestion.setPageSize(10);
        listOldQuestion.clear();
        Backendless.Data.of(Question.class).find(queryQuestion, new AsyncCallback<List<Question>>() {
            @Override
            public void handleResponse(List<Question> response) {
                if (response.size() < 10) {
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
                hideProgressDialog();
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
        showProgressDialog(R.string.loading_more_answer);
        queryQuestion.prepareNextPage();
        Backendless.Data.of(Question.class).find(queryQuestion, new AsyncCallback<List<Question>>() {
            @Override
            public void handleResponse(List<Question> response) {
                hideProgressDialog();
                if (response.size() != 0) {
                    if(response.size() < 10){
                        isAllOfQuestion = true;
                    }
                    listOldQuestion.addAll(response);
                    lvOldQuestion.post(new Runnable() {
                        @Override
                        public void run() {
                            questionAdapter.notifyDataSetChanged();
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
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateOldQuestion();
            }
        });
        tvNewQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentFragment = new NewQuestionFragment();
                fragmentManager.beginTransaction().replace(R.id.root_view_main_activity, currentFragment)
                        .addToBackStack(null)
                        .commit();

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

                currentFragment = new AnswerFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("question", listOldQuestion.get(i));
                currentFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.root_view_main_activity, currentFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

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
        lvOldQuestion.setVerticalScrollBarEnabled(false);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipetop);
        questionAdapter = new QuestionAdapter(mainActivity, R.layout.item_list_question, listOldQuestion);
        lvOldQuestion.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                if(!isAllOfQuestion && listOldQuestion.size() != 0){
                    if(((listOldQuestion.size()-1) == lastVisibleItem))
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
        // demo voi so nhan vien duoi 100
//        queryBuilder.setPageSize( 100 ).setOffset( 0 );
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

    private void deleteAndSaveNewBackendUser() {
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        sharedPrefsHelper.removeBELUser();
        sharedPrefsHelper.saveBELUser(currentBackendlessUser);
    }

    private boolean checkHasBELUser() {
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        if (!sharedPrefsHelper.hasBELUser()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * check if hasQb user in sharepreferances -> return true
     * else sign up new QbUser by current backendless user and login then
     *
     * @return
     */
    private boolean checkHasQbUser() {
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        if (!sharedPrefsHelper.hasQbUser()) {
            return false;
        } else {
            return true;
        }
    }

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
            qbUser.setLogin(getCurrentDeviceId());
            qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
            qbUser.setTags(userTags);
        }
        return qbUser;
    }

    // user quickblox
    private void startSignUpNewUser(final QBUser newUser) {
        showProgressDialog(R.string.dlg_creating_new_user);
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
                            signInCreatedUser(newUser, true);
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
                if (deleteCurrentUser) {
                    removeAllUserData(result);
                } else {

//                    startOpponentsActivity();
                }
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
                saveUserData(userForSave);
                signInCreatedUser(userForSave, false);
            } else {
                Toaster.longToast(getString(R.string.login_chat_login_error) + errorMessage);
            }
        }
    }

    public void showListEmployee() {
        OpponentsActivity.start(mainActivity, false);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_user_info:
                break;
            case R.id.nav_video_setting:
                break;
            case R.id.nav_rate:

                break;
            case R.id.nav_feedback:

                break;
            case R.id.nav_share:
                break;
            case R.id.nav_list_employee:
                showListEmployee();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fragmentManager.popBackStack();
            if (currentFragment instanceof NewQuestionFragment) {
                if (((NewQuestionFragment) currentFragment).isUpdateMain) {
                    updateOldQuestion();
                }
            } else if (currentFragment instanceof AnswerFragment) {
                if (((AnswerFragment) currentFragment).isUpdateMain) {
                    updateOldQuestion();
                }
            }

        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
            super.onBackPressed();
        }
    }

}
