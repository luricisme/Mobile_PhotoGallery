package com.example.photo_gallery_app;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MoreFragment extends Fragment {
    MainActivity main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!(getActivity() instanceof MainCallbacks)) {
            throw new IllegalStateException( "Activity must implement MainCallbacks");
        }
        main = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        txtLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.lang_title))
                        .setItems(new String[]{getString(R.string.lang_eng), getString(R.string.lang_vie)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Xử lý lựa chọn ngôn ngữ
                                String language = (which == 0) ? "en" : "vi";
                                // Gửi thông tin tới MainActivity
                                main.onMsgFromFragToMain("MORE-FRAG", "LANGUAGE_" + language);
                            }
                        })
                        .setNegativeButton("Hủy", null) // Nút hủy
                        .create();

                alertDialog.show();

                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                if (negativeButton != null) {
                    negativeButton.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
            }
        });

        return layout_more;
    }
}