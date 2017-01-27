package com.iam360.dscvr.removed_social.viewmodels;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.util.CameraUtils;
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
            if (files == null) return optographs;
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    if (file.listFiles().length==3) {
                        // create new optograph
                        Optograph optograph = new Optograph(file.getName());
                        optograph.setIs_local(true);
                        optograph.setShould_be_published(true);
                        optographs.add(optograph);
                    }
                } else {
                    // ignore
                }
            }
        }

        return optographs;
    }
}
