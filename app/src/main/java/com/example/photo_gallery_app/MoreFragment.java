package com.example.photo_gallery_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MoreFragment extends Fragment {
    MainActivity main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!(getActivity() instanceof MainCallbacks)) {
            throw new IllegalStateException( "Activity must implement MainCallbacks");
        }
        main = (MainActivity) getActivity(); // use this reference to invoke main callbacks
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout layout_more = (LinearLayout) inflater.inflate(R.layout.fragment_more, container, false);

        TextView txtChangePass = (TextView) layout_more.findViewById(R.id.txtChangePass);
        TextView txtLanguage = (TextView) layout_more.findViewById(R.id.txtLanguage);
        TextView txtAbout = (TextView) layout_more.findViewById(R.id.txtAbout);

        txtAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.onMsgFromFragToMain("MORE-FRAG", "ABOUT");
            }
        });

        return layout_more;
    }
}