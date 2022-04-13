package com.android.onlineshoppingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.onlineshoppingapp.adapters.ViewPagerAdapterSettings;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SettingsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private CardView cardLogout, cardChangePass, cardDeleteAccount, cardPolicy, cardVersion;
    private ViewPagerAdapterSettings viewPagerAdapterSettings;
    private TabLayout tabLayoutSettings;
    private ViewPager2 viewPager2Settings;
    private String[] titles = new String[]{"Cá nhân", "Địa chỉ", "Thanh toán"};

    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        fAuth = FirebaseAuth.getInstance();
        viewPagerAdapterSettings = new ViewPagerAdapterSettings(this);

        //init
        ivBack = findViewById(R.id.ivBackToProfile);
        viewPager2Settings = findViewById(R.id.viewpagerSettings);
        tabLayoutSettings = findViewById(R.id.tablayoutSettings);
        cardChangePass = findViewById(R.id.cardChangePass);
        cardDeleteAccount = findViewById(R.id.cardDeleteAccount);
        cardPolicy = findViewById(R.id.cardPolicy);
        cardVersion = findViewById(R.id.cardVersion);
        cardLogout = findViewById(R.id.cardLogout);

        // click on back button
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // viewpager and tab layout
        viewPager2Settings.setAdapter(viewPagerAdapterSettings);
        new TabLayoutMediator(tabLayoutSettings, viewPager2Settings,
                ((tab, position) -> tab.setText(titles[position]))).attach();

        // click on change password
        cardChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

        // click on delete account
        cardDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccount();
            }
        });

        // click on policy
        cardPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
                alertDialog.setTitle("Chính sách")
                        .setMessage(policyContent())
                        .setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });

        // click on version
        cardVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Bạn đã cập nhật phiên bản mới nhất", Toast.LENGTH_SHORT).show();
            }
        });

        // click on logout
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //log out
                fAuth.signOut();
                // navigate to login activity
                finishAffinity();
                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            }
        });

    }

    // ------------------ Function --------------------------

    private void deleteAccount() {

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout_deleteacc, null);
        BottomSheetDialog bottomSheetDialogDeleteAcc = new BottomSheetDialog(SettingsActivity.this);
        bottomSheetDialogDeleteAcc.setContentView(sheetView);
        bottomSheetDialogDeleteAcc.setCancelable(false);
        bottomSheetDialogDeleteAcc.show();

        // click on close bottom sheet
        sheetView.findViewById(R.id.bottomSheetDeleteAccClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // close bottom sheet dialog
                bottomSheetDialogDeleteAcc.dismiss();
            }
        });

        // check email
        TextInputEditText etEmail = sheetView.findViewById(R.id.bottomSheetDeleteAccEmail);
        TextInputLayout layoutEmail = sheetView.findViewById(R.id.layout_bottomSheetDeleteAccEmail);
        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etEmail.getText().toString().trim().equals("")) {
                        layoutEmail.setHelperText("Email không được để trống!");
                    } else if (!isCorrectEmailFormat(etEmail.getText().toString().trim())) {
                        layoutEmail.setHelperText("Email bạn vừa nhập không đúng định dạng");
                    } else if (!etEmail.getText().toString().trim().equals(fAuth.getCurrentUser().getEmail())) { // replace by user email
                        layoutEmail.setHelperText("Email chưa đăng ký");
                    }
                } else {
                    layoutEmail.setHelperTextEnabled(false);
                }
            }
        });

        //check password
        TextInputEditText etPassword = sheetView.findViewById(R.id.bottomSheetDeleteAccPass);
        TextInputLayout layoutPassword = sheetView.findViewById(R.id.layout_bottomSheetDeleteAccPass);
        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etPassword.getText().toString().equals("")) {
                        layoutPassword.setHelperText("Mật khẩu không được để trống");
                    } else {
                        if (!etPassword.getText().toString().equals("admin")) { // replace by current user password
                            layoutPassword.setHelperText("Mật khẩu bạn nhập không đúng");
                        }
                    }
                } else {
                    layoutPassword.setHelperTextEnabled(false);
                }
            }
        });

        // click on send code
        Button btnSendCode = sheetView.findViewById(R.id.btnSendDeleteCode);
        Random random = new Random();
        String verificationCode = String.valueOf(random.nextInt(999999 - 100000) + 100000);
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send code to user email
                sendCodeByEmail(etEmail.getText().toString().trim(), verificationCode);
                System.out.println("VERIFICATION CODE: " + verificationCode);

                // delay button send code
                btnSendCode.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnSendCode.setEnabled(true);
                    }
                }, 10000);
            }
        });

        // check code
        TextInputEditText etVerify = sheetView.findViewById(R.id.bottomSheetDeleteAccVerify);
        TextInputLayout layoutVerify = sheetView.findViewById(R.id.layout_bottomSheetDeleteAccVerify);
        etVerify.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (!etVerify.getText().toString().equals(verificationCode)) {
                        layoutVerify.setHelperText("Mã xác minh không đúng");
                    }
                } else {
                    layoutVerify.setHelperTextEnabled(false);
                }
            }
        });

        // click on checkbox
        CheckBox cbConfirmDeleteAcc = sheetView.findViewById(R.id.cbConfirmDeleteAcc);
        Button confirmBtn = sheetView.findViewById(R.id.bottomSheetChangePassBtn);
        cbConfirmDeleteAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    confirmBtn.setEnabled(true);
                }
            }
        });

        // click on confirm button
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Xoá tài khoản thành công", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void changePassword() {

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout_changepass, null);
        BottomSheetDialog bottomSheetDialogChangePass = new BottomSheetDialog(SettingsActivity.this);
        bottomSheetDialogChangePass.setContentView(sheetView);
        bottomSheetDialogChangePass.setCancelable(false);
        bottomSheetDialogChangePass.show();

        // click on close bottom sheet
        sheetView.findViewById(R.id.bottomSheetChangePassClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // close bottom sheet dialog
                bottomSheetDialogChangePass.dismiss();
            }
        });

        //check old password
        TextInputEditText etOldPassword = sheetView.findViewById(R.id.bottomSheetChangePassOldPass);
        TextInputLayout layoutOldPassword = sheetView.findViewById(R.id.layout_bottomSheetChangePassOldPass);
        etOldPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etOldPassword.getText().toString().equals("")) {
                        layoutOldPassword.setHelperText("Mật khẩu không được để trống");
                    } else {
                        if (!etOldPassword.getText().toString().equals("admin")) { // replace by current user password
                            layoutOldPassword.setHelperText("Mật khẩu bạn nhập không đúng");
                        }
                    }
                } else {
                    layoutOldPassword.setHelperTextEnabled(false);
                }
            }
        });

        // Check null input data: password
        TextInputEditText etNewPassword = sheetView.findViewById(R.id.bottomSheetChangePassNewPass);
        TextInputLayout layoutNewPassword = sheetView.findViewById(R.id.layout_bottomSheetChangePassNewPass);
        etNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etNewPassword.getText().toString().equals("")) {
                        layoutNewPassword.setHelperText("Mật khẩu không được để trống!");
                    } else if (!isLongEnough(etNewPassword.getText().toString(), 8)) {
                        layoutNewPassword.setHelperText("Mật khẩu phải có ít nhất 8 kí tự");
                    } else if (isCorrectTextFormat(etNewPassword.getText().toString())) {
                        layoutNewPassword.setHelperText("Mật khẩu chỉ bao gồm số, chữ cái và các kí tự _ . -");
                    }
                } else {
                    layoutNewPassword.setHelperTextEnabled(false);
                }
            }
        });

        // Check null input data: Re-password
        TextInputEditText etReNewPassword = sheetView.findViewById(R.id.bottomSheetChangePassReNewPass);
        TextInputLayout layoutReNewPassword = sheetView.findViewById(R.id.layout_bottomSheetChangePassReNewPass);
        etReNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etReNewPassword.getText().toString().equals(etNewPassword.getText().toString())) {
                        layoutReNewPassword.setHelperTextEnabled(false);
                    } else {
                        layoutReNewPassword.setHelperText("Xác nhận mật khẩu không đúng!");
                    }
                }
            }
        });
    }

    private void sendCodeByEmail(String receiverEmail, String verificationCode) {

        try {
            String senderEmail = "project.onlineshoppingapp@gmail.com";
            String senderPassword = "hulwohxxyuihmesr";

            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            // send email to user: receiverEmail
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail));

            mimeMessage.setSubject("[OnlineShoppingApp] Mã xác minh yêu cầu xoá tài khoản");
            mimeMessage.setText("Xin chào," +
                    "\n\nBạn vừa yêu cầu mã xác minh để xoá tài khoản của bạn " +
                    "với địa chỉ email: " + receiverEmail +
                    "\n\nMã xác minh: " + verificationCode +
                    "\n\nVui lòng nhập mã xác minh bên trên để chúng tôi có thể tiếp tục thực hiện " +
                    "các bước xoá tài khoản của bạn." +
                    "\n\nTrân trọng,\nĐội ngũ OnlineShoppingApp");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    private boolean isCorrectEmailFormat(String str) {
        if (str.matches("[a-zA-Z0-9._-]+@[a-z]+\\.[a-zA-Z0-9.]+"))
            return true;
        return false;
    }

    private boolean isCorrectTextFormat(String str) {
        if (str.matches("[^a-zA-Z0-9._-]"))
            return true;
        return false;
    }

    private boolean isLongEnough(String str, int num) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            count++;
        }
        if (count >= num) return true;
        else return false;
    }

    private String policyContent() {
        String content = "1. Trong quá trình cung cấp cho Quý vị các Dịch Vụ (như được định nghĩa trong Điều Khoản Sử Dụng) hoặc quyền truy cập vào Nền Tảng Ứng dụng mua sắm trực tuyến (như được định nghĩa trong Điều Khoản Sử Dụng), chúng tôi sẽ thu thập, sử dụng, tiết lộ, lưu trữ và/hoặc xử lý dữ liệu, bao gồm cả dữ liệu cá nhân của Quý vị. Trong Chính Sách Bảo Mật này, ứng dụng cũng sẽ được dẫn chiếu đến (các) nền tảng của Nhà Bán Hàng (như được định nghĩa trong Quy Chế Hoạt Động) liên quan.\n" +
                "\n" +
                "2. Chính Sách Bảo Mật này thiết lập để giúp Quý vị biết được cách thức chúng tôi thu thập, sử dụng, tiết lộ, lưu trữ và/hoặc xử lý dữ liệu mà chúng tôi thu thập và nhận được trong quá trình cung cấp các Dịch Vụ hoặc cấp quyền truy cập vào ứng dụng cho Quý vị, người dùng của chúng tôi, cho dù Quý vị đang sử dụng ứng dụng của chúng tôi với tư cách là Khách Hàng (như được định nghĩa trong Quy Chế Hoạt Động) hoặc Nhà Bán Hàng. Chúng Tôi sẽ chỉ thu thập, sử dụng, tiết lộ, lưu trữ và/hoặc xử lý dữ liệu cá nhân của Quý vị theo Chính Sách Bảo Mật này.\n" +
                "\n" +
                "3. Quý vị cần đọc Chính Sách Bảo Mật này cùng với bất kỳ thông báo được áp dụng nào khác mà chúng tôi có thể đưa ra trong các trường hợp cụ thể khi chúng tôi thu thập, sử dụng, tiết lộ và/hoặc xử lý dữ liệu cá nhân về Quý vị, để Quý vị nhận thức đầy đủ về cách thức và lý do chúng tôi sử dụng dữ liệu cá nhân của Quý vị.";
        return content;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}