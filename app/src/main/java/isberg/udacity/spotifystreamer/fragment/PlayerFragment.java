package isberg.udacity.spotifystreamer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;

public class PlayerFragment extends Fragment {

    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }
}
