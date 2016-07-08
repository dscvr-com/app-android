package com.iam360.iam360.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.views.dialogs.NetworkProblemDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Mariel on 6/7/2016.
 */
public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.search_box) EditText searchBox;
    @Bind(R.id.clear_search) ImageButton clearSearch;
    @Bind(R.id.search_icon) ImageButton searchIcon;
    @Bind(R.id.cancel_search) TextView cancelSearch;
    @Bind(R.id.search_progress) ProgressBar searchProgress;
    @Bind(R.id.no_result_text) TextView noResultText;
    @Bind(R.id.person_list) SnappyRecyclerView personList;

    private AutoCompleteSearchAdapter mAdapter;
    private String newText;
    private String searchValue;

    protected ApiConsumer apiConsumer;
    protected Cache cache;

    private NetworkProblemDialog networkProblemDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cache = Cache.open();
        apiConsumer = new ApiConsumer(null);

        setContentView(R.layout.activity_search);

        ButterKnife.bind(this, this);

        networkProblemDialog = new NetworkProblemDialog();

        clearSearch.setVisibility(View.INVISIBLE);
        noResultText.setVisibility(View.GONE);
        searchProgress.setVisibility(View.GONE);

        searchBox.setOnClickListener(this);
        clearSearch.setOnClickListener(this);
        searchIcon.setOnClickListener(this);
        cancelSearch.setOnClickListener(this);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    newText = s.toString();
                    clearSearch.setVisibility(View.VISIBLE);
                    if (s.toString().length() != 0) {
                        newText = s.toString();
                        loadSuggestion(newText);
                        searchValue = newText;
                    } else if (s.toString().isEmpty()) {
                        newText = "";
                    }
                } else {
                    newText = "";
                    clearSearch.setVisibility(View.GONE);
                    noResultText.setVisibility(View.GONE);
                    searchProgressVisibility(View.GONE);
                    mAdapter.setObjects(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mAdapter = new AutoCompleteSearchAdapter(this);
        personList.setAdapter(mAdapter);
        personList.setLayoutManager(llm);
    }

    public void searchProgressVisibility(int visibility) {
        searchProgress.setVisibility(visibility);
    }

    private void loadSuggestion(String search) {
        personList.setVisibility(View.INVISIBLE);
        noResultText.setVisibility(View.GONE);
        searchProgress.setVisibility(View.VISIBLE);
//        apiConsumer.getSearchResult(search)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnCompleted(() -> personList.setVisibility(View.VISIBLE))
//                .onErrorReturn(throwable -> {
//                    checkItem();
//                    if (networkProblemDialog.isAdded() || throwable.getMessage().contains("iterable must not be null"))
//                        return null;
//                    networkProblemDialog.show(getSupportFragmentManager(), "networkProblemDialog");
//                    return null;
//                })
//                .subscribe(mAdapter::addItem);
        apiConsumer.getSearchResultCall(search, new Callback<Person>() {
            @Override
            public void onResponse(Response<Person> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body()!=null) {
                    Person person = response.body();
                    if(person.getId().isEmpty()) {
                        mAdapter.setObjects(new ArrayList<>());
                    } else {
                        List<Person> personList = new ArrayList<Person>();
                        personList.add(person);
                        mAdapter.setObjects(personList);
                    }
                } else {
                    mAdapter.setObjects(new ArrayList<>());
                }
                checkItem();
            }

            @Override
            public void onFailure(Throwable t) {
                mAdapter.setObjects(new ArrayList<>());
                checkItem();
                if (networkProblemDialog.isAdded() || t.getMessage().contains("iterable must not be null")) return;
                networkProblemDialog.show(getSupportFragmentManager(), "networkProblemDialog");
            }
        });
    }

    private void checkItem() {
        if (mAdapter.getItemCount()==0) {
            noResultText.setVisibility(View.VISIBLE);
            personList.setVisibility(View.GONE);
            searchProgressVisibility(View.GONE);
        } else {
            personList.setVisibility(View.VISIBLE);
            searchProgressVisibility(View.GONE);
            noResultText.setVisibility(View.GONE);
        }
        if (newText.isEmpty()) {
            clearSearch.setVisibility(View.GONE);
            noResultText.setVisibility(View.GONE);
            searchProgressVisibility(View.GONE);
            mAdapter.setObjects(new ArrayList<>());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_box:
                break;
            case R.id.clear_search:
                searchBox.setText("");
                checkItem();
                break;
            case R.id.cancel_search:
                finish();
                break;
            case R.id.search_icon:
                break;
            default:
                break;
        }
    }
}
