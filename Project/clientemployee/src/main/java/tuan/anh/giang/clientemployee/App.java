package tuan.anh.giang.clientemployee;


import com.backendless.Backendless;

import java.io.IOException;

import tuan.anh.giang.core.CoreApp;
import tuan.anh.giang.core.utils.ActivityLifecycle;
import tuan.anh.giang.project.db.Defaults;
import tuan.anh.giang.project.models.SampleConfigs;
import tuan.anh.giang.project.util.QBResRequestExecutor;
import tuan.anh.giang.project.utils.Consts;
import tuan.anh.giang.project.utils.configs.ConfigUtils;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();
    private static SampleConfigs sampleConfigs;

    private static App instance;
    private QBResRequestExecutor qbResRequestExecutor;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycle.init(this);
        initSampleConfigs();
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
    private void initSampleConfigs() {
        try {
            sampleConfigs = ConfigUtils.getSampleConfigs(Consts.SAMPLE_CONFIG_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SampleConfigs getSampleConfigs() {
        return sampleConfigs;
    }
}
