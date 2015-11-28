package co.optonaut.optonaut.views;

import android.databinding.DataBindingUtil;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.viewmodels.OptographAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivityFragment extends Fragment {
    private OptographAdapter adapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recList = (RecyclerView) view.findViewById(R.id.optographFeed);
        // our children have fixed size
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        adapter = new OptographAdapter();
        recList.setAdapter(adapter);
    }

    public void refreshFeed() {
        ApiConsumer apiConsumer = new ApiConsumer();
        try {
            apiConsumer.getOptographs(new Callback<List<Optograph>>() {
                @Override
                public void onResponse(Response<List<Optograph>> response, Retrofit retrofit) {
                    List<Optograph> optographs = response.body();
                    adapter.addItems(optographs);
                }

                @Override
                public void onFailure(Throwable t) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
