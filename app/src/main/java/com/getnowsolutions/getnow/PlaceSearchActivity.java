package com.getnowsolutions.getnow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.getnowsolutions.getnow.adapters.PlaceListAdapter;
import com.getnowsolutions.getnow.handlers.networkhandlers.APIClient;
import com.getnowsolutions.getnow.handlers.networkhandlers.APIInterface;
import com.getnowsolutions.getnow.handlers.networkhandlers.GoogleRestClient;
import com.getnowsolutions.getnow.interfaces.ListInteractionListner;
import com.getnowsolutions.getnow.models.Place;
import com.getnowsolutions.getnow.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceSearchActivity extends AppCompatActivity implements ListInteractionListner{

    @BindView(R.id.addressText)
    EditText addressText;
    @BindView(R.id.historyRecyclerView)
    RecyclerView historyRecyclerView;
    @BindView(R.id.placesRecyclerView)
    RecyclerView placesRecyclerView;
    private APIInterface apiInterface;
    ArrayList<Place> placesList;
    PlaceListAdapter placeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_search);
        ButterKnife.bind(this);
        apiInterface = GoogleRestClient.getGooglePlaceClient().create(APIInterface.class);
        placesList = new ArrayList<>();
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        placeListAdapter = new PlaceListAdapter(placesList,this);
        placesRecyclerView.setAdapter(placeListAdapter);
        listners();
    }

    void listners() {
        addressText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 2) {
                    getPlaces(s.toString());
                }
            }
        });
    }

    void getPlaces(String query) {
        Call<JsonObject> call = apiInterface.getPlaces(getIntent().getStringExtra("loc"),
                query, Constants.GOOGLE_PLACE_API_KEY,"geocode","100000");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.body().get("status").getAsString().equals("OK")) {
                        JsonArray result = response.body().getAsJsonArray("predictions");
                        placesList.clear();
                        for (int i = 0; i < result.size(); i++) {
                            JsonObject placeObj = result.get(i).getAsJsonObject();
                            Place place = new Place(placeObj.get("description").getAsString());
                            place.setPlaceId(placeObj.get("place_id").getAsString());
                            placesList.add(place);
                        }
                        placesRecyclerView.setVisibility(View.VISIBLE);
                        placeListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("RESP", "failde");
            }
        });
    }

    @Override
    public void onListClicked(Object root) {
        final Place place = (Place) root;
        Call<JsonObject> call = apiInterface.getPlaceDetails(place.placeId,Constants.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.body().get("status").getAsString().equals("OK")) {
                        JsonObject result = response.body().getAsJsonObject("result").getAsJsonObject("geometry").getAsJsonObject("location");
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("lat",result.get("lat").getAsDouble());
                        returnIntent.putExtra("lng",result.get("lng").getAsDouble());
                        returnIntent.putExtra("place",place.placeAddress);
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("RESP", "failde");
            }
        });
    }
}
