package com.iam360.iam360.viewmodels;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.util.CameraUtils;
import rx.Observable;

/**
 * @author Nilan Marktanner
 * @date 2016-02-13
 */
public class LocalOptographManager {
    public static Observable<Optograph> getOptographs() {
        return Observable.from(listLocalOptographs());
    }

    private static List<Optograph> listLocalOptographs() {
        List<Optograph> optographs = new LinkedList<>();
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    // create new optograph
                    optographs.add(new Optograph(file.getName()));
                } else {
                    // ignore
                }
            }
        }

        return optographs;
    }
}
