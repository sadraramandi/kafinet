package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class activity_enter_phone extends AppCompatActivity {

    private TextView textViewTitle;
    private EditText editText1, editText2;
    private Button buttonNext;

    private RequestQueue requestQueue;

    private boolean thereis = false;
    private int clickCount = 1;
    private String phone = "";

    private static final String BASE_URL = "http://10.10.193.180/kafinet_php/";
    private static final String CHECK_USER_URL = BASE_URL + "check_user.php";
    private static final String REGISTER_USER_URL = BASE_URL + "register_user.php";
    private static final String LOGIN_USER_URL = BASE_URL + "login_user.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_phone);

        // چک وضعیت لاگین
        boolean isLoggedIn = getSharedPreferences("user_session", MODE_PRIVATE)
                .getBoolean("logged_in", false);
        if (isLoggedIn) {
            Intent intent = new Intent(this, NextActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        textViewTitle = findViewById(R.id.textViewTitle);
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        buttonNext = findViewById(R.id.buttonNext);

        editText2.setVisibility(View.GONE);

        editText1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        editText1.setInputType(InputType.TYPE_CLASS_NUMBER);

        requestQueue = Volley.newRequestQueue(this);

        buttonNext.setOnClickListener(v -> {

            if (clickCount == 1) {
                phone = editText1.getText().toString().trim();

                if (!phone.matches("\\d+")) {
                    editText1.setError("فقط عدد وارد کنید");
                    return;
                }

                if (!phone.startsWith("09")) {
                    editText1.setError("شماره موبایل باید با 09 شروع شود");
                    return;
                }

                if (phone.length() < 11) {
                    editText1.setError("شماره موبایل باید ۱۱ رقم باشد");
                    return;
                }

                StringRequest request = new StringRequest(Request.Method.POST, CHECK_USER_URL,
                        response -> {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                boolean exists = jsonObject.getBoolean("exists");
                                if (exists) {
                                    thereis = true;
                                    animateTextChange(textViewTitle, "رمز عبور خود را وارد کنید");
                                    animateHintChange(editText1, "رمز عبور");
                                    editText1.setText("");
                                    editText1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                    editText1.setFilters(new InputFilter[]{});
                                    editText2.setVisibility(View.GONE);
                                    animateButtonTextChange(buttonNext, "ورود");
                                    clickCount = 2;
                                } else {
                                    thereis = false;
                                    animateTextChange(textViewTitle, "کد پیامک شده را وارد کنید");
                                    animateHintChange(editText1, "کد تایید");
                                    editText1.setText("");
                                    editText1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
                                    editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    animateButtonTextChange(buttonNext, "ادامه");
                                    clickCount = 2;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "خطا در پردازش پاسخ", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Toast.makeText(this, "خطا در ارتباط", Toast.LENGTH_SHORT).show();
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("phone", phone);
                        return params;
                    }
                };

                requestQueue.add(request);

            } else if (clickCount == 2) {
                if (thereis) {
                    // لاگین
                    String password = editText1.getText().toString().trim();

                    if (phone.isEmpty()) {
                        Toast.makeText(this, "خطا: شماره موبایل نامشخص است", Toast.LENGTH_SHORT).show();
                        clickCount = 1;
                        return;
                    }

                    if (password.isEmpty()) {
                        editText1.setError("رمز را وارد کنید");
                        return;
                    }

                    StringRequest loginRequest = new StringRequest(Request.Method.POST, LOGIN_USER_URL,
                            response -> {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (obj.getBoolean("success")) {
                                        // ذخیره وضعیت لاگین
                                        getSharedPreferences("user_session", MODE_PRIVATE)
                                                .edit()
                                                .putBoolean("logged_in", true)
                                                .apply();

                                        Intent intent = new Intent(activity_enter_phone.this, NextActivity.class);
                                        startActivity(intent);
                                    } else {
                                        editText1.setError("رمز عبور نامعتبر ");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "خطا در پردازش پاسخ", Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> {
                                Toast.makeText(this, "خطا در ارتباط با سرور", Toast.LENGTH_SHORT).show();
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("phone", phone);
                            params.put("password", password);
                            return params;
                        }
                    };

                    requestQueue.add(loginRequest);
                } else {
                    // OTP
                    String code = editText1.getText().toString().trim();
                    if (code.length() < 5) {
                        editText1.setError("کد تایید باید ۵ رقم باشد");
                        return;
                    }

                    animateTextChange(textViewTitle, "رمز عبور خود را وارد کنید");
                    animateHintChange(editText1, "رمز عبور");
                    editText1.setText("");
                    editText1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText1.setFilters(new InputFilter[]{});

                    editText2.setHint("تکرار رمز عبور");
                    editText2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText2.setVisibility(View.VISIBLE);

                    animateButtonTextChange(buttonNext, "ثبت نام");
                    clickCount = 3;
                }

            } else if (clickCount == 3) {
                String pass1 = editText1.getText().toString().trim();
                String pass2 = editText2.getText().toString().trim();

                if (pass1.isEmpty() || pass2.isEmpty()) {
                    editText2.setError("لطفا رمزها را وارد کنید");
                    return;
                } else if (!pass1.equals(pass2)) {
                    editText2.setError("رمزها یکسان نیستند");
                    return;
                } else if (!isStrongPassword(pass1)) {
                    editText2.setError("رمز باید حداقل ۸ کاراکتر، حروف بزرگ، کوچک و عدد داشته باشد");
                    return;
                }

                StringRequest regRequest = new StringRequest(Request.Method.POST, REGISTER_USER_URL,
                        response -> {
                            try {
                                JSONObject obj = new JSONObject(response);
                                if (obj.getBoolean("success")) {
                                    Toast.makeText(this, "ثبت نام موفق", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(activity_enter_phone.this, NextActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(this, "خطا در ثبت نام", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "خطا در پردازش پاسخ", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Toast.makeText(this, "خطا در ارتباط", Toast.LENGTH_SHORT).show();
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("phone", phone);
                        params.put("password", pass1);
                        return params;
                    }
                };

                requestQueue.add(regRequest);
            }
        });
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*");
    }

    private void animateTextChange(TextView textView, String newText) {
        textView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    textView.setText(newText);
                    textView.animate()
                            .alpha(1f)
                            .setDuration(200);
                });
    }

    private void animateHintChange(EditText editText, String newHint) {
        editText.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    editText.setHint(newHint);
                    editText.animate()
                            .alpha(1f)
                            .setDuration(200);
                });
    }

    private void animateButtonTextChange(Button button, String newText) {
        button.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    button.setText(newText);
                    button.animate()
                            .alpha(1f)
                            .setDuration(200);
                });
    }
}
