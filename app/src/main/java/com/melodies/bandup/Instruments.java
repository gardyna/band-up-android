package com.melodies.bandup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Instruments extends AppCompatActivity {
    private String url = "https://band-up-server.herokuapp.com/instruments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruments);

        final GridView gridView = (GridView)findViewById(R.id.instrumentGridView);
        JSONArray req = new JSONArray();

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET, url, req,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<ListAdapter.DoubleListItem> list = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject instrument = response.getJSONObject(i);
                                ListAdapter.DoubleListItem myInst = new ListAdapter.DoubleListItem(instrument.getInt("order"), instrument.getString("name"));
                                list.add(myInst);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        gridView.setAdapter(new ListAdapter(getBaseContext(), list));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListAdapter.DoubleListItem inst = (ListAdapter.DoubleListItem)parent.getAdapter().getItem(0);
                ImageView itemSelected = (ImageView) view.findViewById(R.id.itemSelected);

                // TODO: Find a better solution
                if (itemSelected.getVisibility() == view.VISIBLE) {
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shrink);
                    itemSelected.startAnimation(animation);
                    itemSelected.setVisibility(view.INVISIBLE);
                    inst.isSelected = false;

                } else {
                    itemSelected.setVisibility(view.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pop);
                    itemSelected.startAnimation(animation);
                    inst.isSelected = true;
                }
            }
        });
    }
    public void onClickNext (View v) {
        final Button btnGoToInstruments = (Button) findViewById(R.id.btnNext);
        if (v.getId() == R.id.btnNext) {
            Intent toInstrumentsIntent = new Intent(Instruments.this, Genres.class);
            Instruments.this.startActivity(toInstrumentsIntent);
        }
    }
}