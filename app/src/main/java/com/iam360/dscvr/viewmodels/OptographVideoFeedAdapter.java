package com.iam360.dscvr.viewmodels;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.iam360.dscvr.AAFeedItemBinding;
import com.iam360.dscvr.BR;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.opengl.Optograph2DCubeView;
import com.iam360.dscvr.sensors.CombinedMotionManager;
import com.iam360.dscvr.sensors.GestureDetectors;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.CameraUtils;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.RFC3339DateFormatter;
import com.iam360.dscvr.views.activity.MainActivity;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class OptographVideoFeedAdapter extends RecyclerView.Adapter<OptographVideoFeedAdapter.OptographHolder> implements Optograph2DCubeView.OnScrollLockListener {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    private static final float ITEM_WIDTH = Constants.getInstance().getDisplayMetrics().widthPixels;
    private static final float DENSITY = Constants.getInstance().getDisplayMetrics().density;
    private List<Optograph> optographs;

    private Cache cache;
    private Context context;
    private DBHelper mydb;
    private Optograph2DCubeView.OnScrollLockListener scrollLock;
    private boolean isFullscreen;

    public OptographVideoFeedAdapter(Context context) {
        this.context = context;
        this.optographs = new ArrayList<>();

        cache = Cache.open();
        mydb = new DBHelper(context);
    }

    @Override
    public OptographHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.aa_feed_item, parent, false);
        return new OptographHolder(view, context);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);

        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(recyclerView);
//        snappyRecyclerView = (SnappyRecyclerView) recyclerView;
    }

    @Override
    public void onBindViewHolder(OptographHolder holder, int position) {
        Optograph optograph = optographs.get(position);


        holder.bindingHeader.optograph2dview.setSensorMode(CombinedMotionManager.GYRO_MODE);
        holder.bindingHeader.optograph2dview.addScrollLockListener(this);
        holder.bindingHeader.optograph2dview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                    if (context instanceof MainActivity) {
                        toggleFullScreen(holder, ((MainActivity) context).isFullScreenMode);

                        ((MainActivity) context).toggleFeedFullScreen();
                        scrollLock.lock();
                    }
                }

                return holder.bindingHeader.optograph2dview.getOnTouchListener().onTouch(v, event);
            }
        });

        holder.bindingHeader.moreButton.setOnClickListener(v -> showDelete(holder, position));
        holder.bindingHeader.vrButton.setOnClickListener(v -> startVRMode());

        holder.getBinding().setVariable(BR.optograph, optograph);
        holder.getBinding().executePendingBindings();

    }

    private void startVRMode() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(R.string.dialog_vrmode_explanation));
        builder.setCancelable(true);

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDelete(OptographHolder holder, int position) {

        PopupMenu popup = new PopupMenu(context, holder.bindingHeader.moreButton);
        popup.getMenuInflater().inflate(R.menu.opto_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                deleteOptograph(optographs.get(position));
                return true;
            }
        });

        popup.show();//showing popup menu
    }

    private void toggleFullScreen(OptographHolder holder, boolean isFullScreenMode) {

        if (isFullScreenMode) {
            holder.bindingHeader.profileBar.setVisibility(View.VISIBLE);
            if (scrollLock != null) scrollLock.release();
        } else {
            holder.bindingHeader.profileBar.setVisibility(View.GONE);
            if (scrollLock != null) scrollLock.lock();
        }
    }

    public ArrayList<Optograph> getNextOptographList(int position, int count) {
        int optoListCount = optographs.size();
        count = (count < optoListCount) ? count : optoListCount;

        ArrayList<Optograph> optographList = new ArrayList<Optograph>();

        for (int i = 0; i < count; i++) {
            optographList.add(optographs.get((position) % optoListCount));
            position++;
        }

        return optographList;
    }

    @Override
    public int getItemCount() {
        return optographs.size();
    }

    public void addItem(Optograph optograph) {
        if (optograph == null) {
            return;
        }
        // skip if optograph is already in list
        if (optographs.contains(optograph)) {
            return;
        }

        DateTime created_at = optograph.getCreated_atDateTime();

        if (optographs.isEmpty()) {
            optographs.add(optographs.size(), optograph);
            notifyItemInserted(optographs.size() - 1);
            return;
        }

        // if optograph is oldest, simply append to list
        if (created_at != null && created_at.isBefore(getOldest().getCreated_atDateTime())) {
            optographs.add(optograph);
            notifyDataSetChanged();
            Timber.d("added optopgraph at the end");
            return;
        }

        // find correct position of optograph
        for (int i = 0; i < optographs.size(); i++) {
            Optograph current = optographs.get(i);
            if (created_at != null && created_at.isAfter(current.getCreated_atDateTime())) {
                optographs.add(i, optograph);
                notifyItemInserted(i);
                Timber.d("added optopgraph at " + i);
                return;
            }
        }
    }

    private void deleteOptograph(Optograph optograph) {
        deleteOptographFromPhone(optograph.getId());
        mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_DELETED_AT, RFC3339DateFormatter.toRFC3339String(DateTime.now()));
        mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_TEXT, "deleted");

        deleteOptographFromPhone(optograph.getId());
        refreshAfterDelete(optograph.getId());
    }

    private void deleteOptographFromPhone(String id) {
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + id);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    for (File file1 : file.listFiles()) {
                        boolean result = file1.delete();
                    }
                    boolean result = file.delete();
                } else {
                    // ignore
                }
            }
            boolean result = dir.delete();
        }
    }

    public Optograph get(int position) {
        return optographs.get(position);
    }

    public Optograph getOldest() {
        return get(getItemCount() - 1);
    }

    public boolean isEmpty() {
        return optographs.isEmpty();
    }

    public List<Optograph> getOptographs() {
        return this.optographs;
    }

    public void refreshAfterDelete(String id) {

        for (Optograph opto : optographs) {
            if (opto != null && opto.getId().equals(id)) {
                int position = optographs.indexOf(opto);
                optographs.remove(opto);
                notifyItemRemoved(position);
                return;
            }
        }
    }

    public void setOnClickListener(Optograph2DCubeView.OnScrollLockListener scrollLock) {
        this.scrollLock = scrollLock;
    }

    @Override
    public void lock() {
        scrollLock.lock();
    }

    @Override
    public void release() {
        if (context instanceof MainActivity &&  !((MainActivity) context).isFullScreenMode){
            scrollLock.release();
        }
    }

    public static class OptographHolder extends RecyclerView.ViewHolder {
        private AAFeedItemBinding bindingHeader;

        public OptographHolder(View rowView, Context context) {
            super(rowView);
            this.bindingHeader = DataBindingUtil.bind(rowView);
        }

        public AAFeedItemBinding getBinding() {
            return bindingHeader;
        }
    }

}