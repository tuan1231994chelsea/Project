package tuan.anh.giang.project;


import com.backendless.Backendless;

import tuan.anh.giang.core.CoreApp;
import tuan.anh.giang.project.db.Defaults;
import tuan.anh.giang.project.util.QBResRequestExecutor;

public class App extends CoreApp {
    private static App instance;
    private QBResRequestExecutor qbResRequestExecutor;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Backendless.setUrl(Defaults.SERVER_URL);
        Backendless.initApp(getApplicationContext(), Defaults.APPLICATION_ID, Defaults.API_KEY);
        initApplication();
    }

    private void initApplication(){
        instance = this;
    }

    public synchronized QBResRequestExecutor getQbResRequestExecutor() {
        return qbResRequestExecutor == null
                ? qbResRequestExecutor = new QBResRequestExecutor()
                : qbResRequestExecutor;
    }
}
