package tuan.anh.giang.project.utils.configs;

import com.google.gson.Gson;

import java.io.IOException;

import tuan.anh.giang.core.utils.configs.ConfigParser;
import tuan.anh.giang.core.utils.configs.CoreConfigUtils;
import tuan.anh.giang.project.models.SampleConfigs;


public class ConfigUtils extends CoreConfigUtils {

    public static SampleConfigs getSampleConfigs(String fileName) throws IOException {
        ConfigParser configParser = new ConfigParser();
        Gson gson = new Gson();
        return gson.fromJson(configParser.getConfigsAsJsonString(fileName), SampleConfigs.class);
    }
}
