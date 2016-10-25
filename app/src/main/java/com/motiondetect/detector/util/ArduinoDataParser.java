package com.motiondetect.detector.util;

import com.motiondetect.detector.model.MotionModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ArduinoDataParser {
    private static final String ENCODING = "UTF-8";

    private Reader mStreamReader;

    public ArduinoDataParser(InputStream input) throws IOException {
        mStreamReader = new InputStreamReader(input, ENCODING);
    }

    public  List<MotionModel> parseJsonStream() throws IOException {
        String jsonStr = getJsonStringFromStream();
        int size = 0;
        List<Float> data = new ArrayList<>();

        try {
            JSONObject obj = new JSONObject(jsonStr);
            size = obj.getInt("size");

            String dataStr = obj.getString("data");
            String[] strs = dataStr.substring(1, dataStr.length() - 1).split(",");
            for (String str : strs) {
                data.add(parseWithDefault(str, 0f));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return assembleMotionModel(size, data);
    }

    private float parseWithDefault(String str, float defValue) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    private String getJsonStringFromStream() throws IOException {
        char[] buffer = new char[512];
        int strSize = 0;
        int i;
        while ((i = mStreamReader.read()) != -1 && strSize < 512) {
            char c = (char) i;
            buffer[strSize++] = c;
            if (c == '}') {
                break;
            }
        }

        return new String(buffer, 0, strSize);
    }

    private List<MotionModel> assembleMotionModel(int size, List<Float> data) {
        List<MotionModel> result = new ArrayList<>();
        if (size > 0 && data != null) {
            final int perDataSize = data.size() / size;
            for (int i = 0; i < size; i++) {
                MotionModel model = new MotionModel();
                final int base = i * perDataSize;

                model.timestamp = data.get(base).longValue();
                model.accX = data.get(base + 1);
                model.accY = data.get(base + 2);
                model.accZ = data.get(base + 3);
                model.gyroX = data.get(base + 4);
                model.gyroY = data.get(base + 5);
                model.gyroZ = data.get(base + 6);
                result.add(model);
            }
        }
        return result;
    }
}
