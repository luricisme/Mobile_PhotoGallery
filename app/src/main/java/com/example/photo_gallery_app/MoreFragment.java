package com.example.photo_gallery_app;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
                        .setNegativeButton(getString(R.string.btn_cancel), null) // Nút hủy
                        .create();

                alertDialog.show();

                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                if (negativeButton != null) {
                    negativeButton.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
            }
        });
        txtChangePass.setOnClickListener(v -> showChangePasswordDialog());

        return layout_more;
    }

    private void showChangePasswordDialog() {
        // Tạo layout cho dialog
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // Tạo các ô nhập mật khẩu
        EditText inputOldPassword = new EditText(getContext());
        inputOldPassword.setHint(getString(R.string.old_password));
        inputOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText inputNewPassword = new EditText(getContext());
        inputNewPassword.setHint(getString(R.string.new_password));
        inputNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText inputConfirmPassword = new EditText(getContext());
        inputConfirmPassword.setHint(getString(R.string.confirm_password));
        inputConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Thêm các ô nhập vào layout
        layout.addView(inputOldPassword);
        layout.addView(inputNewPassword);
        layout.addView(inputConfirmPassword);

        // Tạo dialog
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.title_change_password))
                .setView(layout)
                .setPositiveButton(getString(R.string.btn_confirm), null) // Xử lý sau khi show
                .setNegativeButton(getString(R.string.btn_cancel), (dialog, which) -> dialog.dismiss())
                .create();

        alertDialog.show();

        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (negativeButton != null) {
            negativeButton.setTextColor(Color.BLACK); // Đổi màu chữ nút Hủy
            negativeButton.setBackgroundColor(Color.WHITE); // Đổi màu nền nút Hủy
            negativeButton.setPadding(20, 0, 20, 10); // Thêm padding cho nút Hủy
        }

        // Xử lý sự kiện cho nút xác nhận
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.BLACK); // Đổi màu chữ nút OK
        positiveButton.setBackgroundColor(Color.WHITE); // Đổi màu nền nút OK
        positiveButton.setPadding(20, 0, 20, 10); // Thêm padding cho nút Hủy

        positiveButton.setOnClickListener(v -> {
            String oldPassword = inputOldPassword.getText().toString().trim();
            String newPassword = inputNewPassword.getText().toString().trim();
            String confirmPassword = inputConfirmPassword.getText().toString().trim();

            DatabaseHandler db = new DatabaseHandler(requireContext());
            String storedPassword = db.getHiddenAlbumPassword();

            // Kiểm tra mật khẩu cũ
            if (!oldPassword.equals(storedPassword)) {
                Toast.makeText(getContext(), getString(R.string.in_old_password), Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu mới và xác nhận
            if (newPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), getString(R.string.fit_new_password), Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật mật khẩu mới
            boolean isUpdated = db.updateHiddenAlbumPassword(newPassword);
            if (isUpdated) {
                Toast.makeText(getContext(), getString(R.string.change_success), Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            } else {
                Toast.makeText(getContext(), getString(R.string.change_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }
}