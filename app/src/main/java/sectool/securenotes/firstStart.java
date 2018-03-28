package sectool.securenotes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import sectool.securenot.R;

public class firstStart extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.appName);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_first_start, container, false);
            TextView head = rootView.findViewById(R.id.textHead);
            TextView info = rootView.findViewById(R.id.txtInfo);
            ImageView img = rootView.findViewById(R.id.imageView);
            Button btn = rootView.findViewById(R.id.button);
            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    head.setText(R.string.txtHello);
                    info.setText(R.string.fsInfoInfo);
                    img.setImageResource(R.drawable.note);
                    btn.setVisibility(View.GONE);
                    return rootView;
                case 2:
                    head.setText(R.string.txtSec);
                    info.setText(R.string.fsInfoSec);
                    img.setImageResource(R.drawable.encrypt);
                    btn.setVisibility(View.GONE);
                    return rootView;
                case 3:
                    head.setText(R.string.txtGogGoGo);
                    info.setText(R.string.fsInfoStart);
                    img.setImageResource(R.drawable.start);
                    btn.setVisibility(View.VISIBLE);
                    return rootView;
                default:
                    return rootView;
            }
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public void clickStart(View view){
        startActivity(new Intent(this, setPass.class));
    }
}
