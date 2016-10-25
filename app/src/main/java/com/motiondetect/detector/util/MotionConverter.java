package com.motiondetect.detector.util;

import com.motiondetect.detector.model.MotionModel;
import com.motiondetect.detectorjni.model.Motion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapping the MotionModel class to the Motion class in the library module.
 */
public abstract class MotionConverter {
    public static Motion convert(MotionModel src) {
        if (src == null) {
            throw new IllegalArgumentException("Convert null MotionModel object");
        }

        Motion motion = new Motion();
        Field[] fields = src.getClass().getDeclaredFields();
        for (Field srcField : fields) {
            srcField.setAccessible(true);
            try {
                Field targetField = motion.getClass().getDeclaredField(srcField.getName());
                targetField.setAccessible(true);
                targetField.set(motion, srcField.get(src));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        return motion;
    }

    public static List<Motion> convert(List<MotionModel> src) {
        if (src == null) {
            throw new IllegalArgumentException("Convert null MotionModel List object");
        }

        List<Motion> results = new ArrayList<>();
        for (MotionModel model : src) {
            results.add(convert(model));
        }
        return results;
    }
}
