package com.example.planets;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.drawer.WearableActionDrawerView;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;
import androidx.wear.widget.drawer.WearableNavigationDrawerView.WearableNavigationDrawerAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        AmbientModeSupport.AmbientCallbackProvider,
        MenuItem.OnMenuItemClickListener,
        WearableNavigationDrawerView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private WearableNavigationDrawerView mWearableNavigationDrawer;
    private WearableActionDrawerView mWearableActionDrawer;

    private ArrayList<Planet> mSolarSystem;
    private int mSelectedPlanet;

    private PlanetFragment mPlanetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_main);

        // Enables Ambient mode.
        AmbientModeSupport.attach(this);

        mSolarSystem = initializeSolarSystem();
        mSelectedPlanet = 0;

        // Initialize content to first planet.
        mPlanetFragment = new PlanetFragment();
        Bundle args = new Bundle();

        int imageId = getResources().getIdentifier(mSolarSystem.get(mSelectedPlanet).getImage(),
                "drawable", getPackageName());


        args.putInt(PlanetFragment.ARG_PLANET_IMAGE_ID, imageId);
        mPlanetFragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, mPlanetFragment).commit();


        // Top Navigation Drawer
        mWearableNavigationDrawer = findViewById(R.id.top_navigation_drawer);
        mWearableNavigationDrawer.setAdapter(new NavigationAdapter(this));
        // Peeks navigation drawer on the top.
        mWearableNavigationDrawer.getController().peekDrawer();
        mWearableNavigationDrawer.addOnItemSelectedListener(this);

        // Bottom Action Drawer
        mWearableActionDrawer = findViewById(R.id.bottom_action_drawer);
        // Peeks action drawer on the bottom.
        mWearableActionDrawer.getController().peekDrawer();
        mWearableActionDrawer.setOnMenuItemClickListener(this);

        /* Action Drawer Tip: If you only have a single action for your Action Drawer, you can use a
         * (custom) View to peek on top of the content by calling
         * mWearableActionDrawer.setPeekContent(View). Make sure you set a click listener to handle
         * a user clicking on your View.
         */
    }

    private ArrayList<Planet> initializeSolarSystem() {
        ArrayList<Planet> solarSystem = new ArrayList<>();
        String[] planetArrayNames = getResources().getStringArray(R.array.planets_array_names);

        for (String planet : planetArrayNames) {
            int planetResourceId = getResources().getIdentifier(planet, "array", getPackageName());
            String[] planetInformation = getResources().getStringArray(planetResourceId);

            solarSystem.add(new Planet(
                    planetInformation[0],   // Name
                    planetInformation[1],   // Navigation icon
                    planetInformation[2],   // Image icon
                    planetInformation[3],   // Moons
                    planetInformation[4],   // Volume
                    planetInformation[5])); // Surface area
        }

        return solarSystem;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        Log.d(TAG, "onMenuItemClick(): " + menuItem);

        final int itemId = menuItem.getItemId();

        String toastMessage = "";

        switch (itemId) {
            case R.id.menu_planet_name:
                toastMessage = mSolarSystem.get(mSelectedPlanet).getName();
                break;
            case R.id.menu_number_of_moons:
                toastMessage = mSolarSystem.get(mSelectedPlanet).getMoons();
                break;
            case R.id.menu_volume:
                toastMessage = mSolarSystem.get(mSelectedPlanet).getVolume();
                break;
            case R.id.menu_surface_area:
                toastMessage = mSolarSystem.get(mSelectedPlanet).getSurfaceArea();
                break;
        }

        mWearableActionDrawer.getController().closeDrawer();

        if (toastMessage.length() > 0) {
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    toastMessage,
                    Toast.LENGTH_SHORT);
            toast.show();
            return true;
        } else {
            return false;
        }
    }

    // Updates content when user changes between items in the navigation drawer.
    @Override
    public void onItemSelected(int position) {
        Log.d(TAG, "WearableNavigationDrawerView triggered onItemSelected(): " + position);
        mSelectedPlanet = position;

        String selectedPlanetImage = mSolarSystem.get(mSelectedPlanet).getImage();
        int drawableId =
                getResources().getIdentifier(selectedPlanetImage, "drawable", getPackageName());
        mPlanetFragment.updatePlanet(drawableId);
    }

    private final class NavigationAdapter extends WearableNavigationDrawerAdapter {

        private final Context mContext;

        /* package */ NavigationAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mSolarSystem.size();
        }

        @Override
        public String getItemText(int pos) {
            return mSolarSystem.get(pos).getName();
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            String navigationIcon = mSolarSystem.get(pos).getNavigationIcon();

            int drawableNavigationIconId =
                    getResources().getIdentifier(navigationIcon, "drawable", getPackageName());

            return mContext.getDrawable(drawableNavigationIconId);
        }
    }

    /**
     * Fragment that appears in the "content_frame", just shows the currently selected planet.
     */
    public static class PlanetFragment extends Fragment {
        /* package */ static final String ARG_PLANET_IMAGE_ID = "planet_image_id";

        private ImageView mImageView;
        private ColorFilter mImageViewColorFilter;

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_planet, container, false);

            mImageView = rootView.findViewById(R.id.image);

            int imageIdToLoad = getArguments() != null
                    ? getArguments().getInt(ARG_PLANET_IMAGE_ID)
                    : 0;
            mImageView.setImageResource(imageIdToLoad);
            mImageViewColorFilter = mImageView.getColorFilter();

            return rootView;
        }

        /* package */ void updatePlanet(int imageId) {
            mImageView.setImageResource(imageId);
        }

        /* package */ void onEnterAmbientInFragment(Bundle ambientDetails) {
            Log.d(TAG, "PlanetFragment.onEnterAmbient() " + ambientDetails);

            // Convert image to grayscale for ambient mode.
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);

            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            mImageView.setColorFilter(filter);
        }

        /**
         * Restores the UI to active (non-ambient) mode.
         */
        /* package */ void onExitAmbientInFragment() {
            Log.d(TAG, "PlanetFragment.onExitAmbient()");

            mImageView.setColorFilter(mImageViewColorFilter);
        }
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /**
         * Prepares the UI for ambient mode.
         */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
            Log.d(TAG, "onEnterAmbient() " + ambientDetails);

            mPlanetFragment.onEnterAmbientInFragment(ambientDetails);
            mWearableNavigationDrawer.getController().closeDrawer();
            mWearableActionDrawer.getController().closeDrawer();
        }

        /**
         * Restores the UI to active (non-ambient) mode.
         */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
            Log.d(TAG, "onExitAmbient()");

            mPlanetFragment.onExitAmbientInFragment();
            mWearableActionDrawer.getController().peekDrawer();
        }
    }
}