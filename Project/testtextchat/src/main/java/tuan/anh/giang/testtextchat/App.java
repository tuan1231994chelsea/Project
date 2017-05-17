package tuan.anh.giang.testtextchat;



import java.io.IOException;

import tuan.anh.giang.core.CoreApp;
import tuan.anh.giang.core.utils.ActivityLifecycle;
import tuan.anh.giang.testtextchat.models.SampleConfigs;
import tuan.anh.giang.testtextchat.utils.Consts;
import tuan.anh.giang.testtextchat.utils.configs.ConfigUtils;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();
    private static SampleConfigs sampleConfigs;

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycle.init(this);
        initSampleConfigs();
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