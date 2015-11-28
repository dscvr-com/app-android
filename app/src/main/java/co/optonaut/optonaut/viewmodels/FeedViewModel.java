package co.optonaut.optonaut.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.TextWatcher;

import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.model.Optograph;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class FeedViewModel extends BaseObservable {
    List<Optograph> optographs;

    public FeedViewModel(Optograph optograph) {
        optographs = new ArrayList<Optograph>();
        optographs.add(optograph);
    }

    @Bindable
    public String getStringRepresentation() {
        return optographs.toString();
    }

    @Bindable
    public int getLength() {
        return optographs.size();
    }
}
