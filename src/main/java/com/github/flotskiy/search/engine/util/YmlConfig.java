package com.github.flotskiy.search.engine.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class YmlConfig {

    private static final Map<String, String> SITES = new HashMap<>();
    private static final String CONNECT_USERAGENT;
    private static final String CONNECT_REFERRER;

    static {
        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("application.yml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Map<String, Object> properties = yaml.load(inputStream);
        JSONObject jsonObject = new JSONObject(properties);

        JSONArray array = jsonObject.getJSONArray("sites");
        for (int i = 0; i < array.length(); i++) {
            String key = array.getJSONObject(i).getString("name");
            String value = array.getJSONObject(i).getString("url");
            SITES.put(key, value);
        }

        CONNECT_USERAGENT = (String) jsonObject.getJSONObject("connect").get("useragent");
        CONNECT_REFERRER = (String) jsonObject.getJSONObject("connect").get("referrer");
    }

    public static Map<String, String> getSites() {
        return SITES;
    }

    public static String getConnectUseragent() {
        return CONNECT_USERAGENT;
    }

    public static String getConnectReferrer() {
        return CONNECT_REFERRER;
    }
}
