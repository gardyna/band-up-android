package com.melodies.bandup.MainScreenActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.melodies.bandup.DatabaseSingleton;
import com.melodies.bandup.R;
import com.melodies.bandup.helper_classes.User;
import com.melodies.bandup.listeners.BandUpErrorListener;
import com.melodies.bandup.listeners.BandUpResponseListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private User currentUser;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public UserDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserDetailsFragment newInstance(String param1, String param2) {
        UserDetailsFragment fragment = new UserDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private TextView txtName;
    private TextView txtInstruments;
    private TextView txtGenres;
    private TextView txtLstInstruments;
    private TextView txtLstGenres;
    private TextView txtAge;
    private TextView txtFavorite;
    private TextView txtPercentage;
    private TextView txtDistance;
    private TextView txtAboutMe;
    private Button   btnLike;
    private ListView lstInstruments;
    private ListView lstGenres;

    private ImageView ivUserProfileImage;

    private void initializeTextViews(View rootView) {
        ivUserProfileImage = (ImageView) rootView.findViewById(R.id.imgProfile);
        txtName            = (TextView)  rootView.findViewById(R.id.txtName);
        txtInstruments     = (TextView)  rootView.findViewById(R.id.txtInstrumentTitle);
        txtGenres          = (TextView)  rootView.findViewById(R.id.txtGenresTitle);
        txtLstInstruments  = (TextView)  rootView.findViewById(R.id.txtLstInstruments);
        txtLstGenres       = (TextView)  rootView.findViewById(R.id.txtLstGenres);
        txtDistance        = (TextView)  rootView.findViewById(R.id.txtDistance);
        txtPercentage      = (TextView)  rootView.findViewById(R.id.txtPercentage);
        txtAge             = (TextView)  rootView.findViewById(R.id.txtAge);
        txtFavorite        = (TextView)  rootView.findViewById(R.id.txtFavorite);
        txtAboutMe         = (TextView)  rootView.findViewById(R.id.txtAboutMe);
    }

    private void initializeButtons(View rootView) {
        btnLike = (Button) rootView.findViewById(R.id.btnLike);
    }

    private void setFonts() {
        txtName          .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams.ttf"));
        txtInstruments   .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams_bold.ttf"));
        txtGenres        .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams_bold.ttf"));
        txtLstInstruments.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams.ttf"));
        txtLstGenres     .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams.ttf"));
        txtDistance      .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams.ttf"));
        txtPercentage    .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams.ttf"));
        txtAge           .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams.ttf"));
        txtFavorite      .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams.ttf"));
        txtAboutMe       .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/caviar_dreams.ttf"));

        btnLike          .setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/master_of_break.ttf"));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Gets the user_id from userListFragment

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_user_details, container, false);

        initializeTextViews(rootView);
        initializeButtons(rootView);
        setFonts();
        System.out.println("CURRENTUSER");
        System.out.println(currentUser);
        String argumentUserID = getArguments().getString("user_id");
        if (currentUser == null || !currentUser.id.equals(argumentUserID)) {
            fetchCurrentUser(getArguments().getString("user_id"));
        } else {
            System.out.println(currentUser.id);
            displayUser(currentUser);
        }
        return rootView;
    }

    private void displayUser(User u) {
        if (u.imgURL != null) {
            Picasso.with(getActivity()).load(u.imgURL).into(ivUserProfileImage);
        }

        txtName.setText(u.name);
        txtAge.setText(String.format("%s %s", u.age, "years old"));
        txtFavorite.setText("Drums");
        txtPercentage.setText(u.percentage + "%");
        txtAboutMe.setText(u.aboutme);

        if (txtDistance == null) {
            txtDistance.setText(u.distance + " km away from you");
        } else {
            txtDistance.setText("-- km away from you");
        }

        for (int i = 0; i < u.genres.size(); i++) {
            txtLstGenres.append(u.genres.get(i) + "\n");
        }

        for (int i = 0; i < u.instruments.size(); i++) {
            txtLstInstruments.append(u.instruments.get(i) + "\n");
        }
    }

    // Request REAL user info from server
    public void fetchCurrentUser(String userid) {
        JSONObject user = new JSONObject();
        try {
            user.put("userId", userid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DatabaseSingleton.getInstance(getActivity()).getBandUpDatabase().getUserProfile(user, new BandUpResponseListener() {
            @Override
            public void onBandUpResponse(Object response) {
                JSONObject responseObj = null;
                if (response instanceof JSONObject) {
                    responseObj = (JSONObject) response;
                }
                if (response != null) {
                    // Binding View to real data
                    currentUser = new User();
                    try {
                        if (!responseObj.isNull("_id")) {
                            currentUser.id = responseObj.getString("_id");
                        }
                        if (!responseObj.isNull("username")) {
                            currentUser.name = responseObj.getString("username");
                        }
                        if (!responseObj.isNull("age")) {
                            currentUser.age = responseObj.getInt("age");
                        }

                        if (!responseObj.isNull("distance")) {
                            currentUser.distance = responseObj.getInt("distance");
                        } else {
                            currentUser.distance = null;
                        }

                        if (!responseObj.isNull("percentage")) {
                            currentUser.percentage = responseObj.getInt("percentage");
                        }

                        if (!responseObj.isNull("genres")) {
                            JSONArray genreArray = responseObj.getJSONArray("genres");
                            for (int i = 0; i < genreArray.length(); i++) {
                                currentUser.genres.add(genreArray.getString(i));
                            }
                        }

                        if (!responseObj.isNull("instruments")) {
                            JSONArray instrumentArray = responseObj.getJSONArray("instruments");
                            for (int i = 0; i < instrumentArray.length(); i++) {
                                currentUser.instruments.add(instrumentArray.getString(i));
                            }
                        }

                        if (!responseObj.isNull("aboutme")) {
                            currentUser.aboutme = responseObj.getString("aboutme");
                        }

                        if (!responseObj.isNull("image")) {
                            JSONObject imageObj = responseObj.getJSONObject("image");

                            if (!imageObj.isNull("url")) {
                                currentUser.imgURL = imageObj.getString("url");
                            }
                        }
                        displayUser(currentUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new BandUpErrorListener() {
            @Override
            public void onBandUpErrorResponse(VolleyError error) {
                System.out.println("ERROR");
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
