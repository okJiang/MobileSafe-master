package net.xwdoor.mobilesafe.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.xwdoor.mobilesafe.R;
import net.xwdoor.mobilesafe.base.BaseActivity;
import net.xwdoor.mobilesafe.utils.MD5Utils;
import net.xwdoor.mobilesafe.utils.PrefUtils;

/**
 * Created by Administrator on 2018/3/26.
 */

public class LoginActivity extends BaseActivity {

    public static void startAct(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initVariables() {

    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
//        final AlertDialog dialog = new AlertDialog.Builder(this).create();
//        View view = View.inflate(this, R.layout.dialog_input_password, null);
        setContentView(R.layout.dialog_input_password);
        Button btnOk = (Button) findViewById(R.id.btn_ok);
//        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        final EditText etPassword = (EditText) findViewById(R.id.et_password);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                //获取保存的密码
                String savedPassword = PrefUtils.getString(PREF_PASSWORD, "", LoginActivity.this);

                //判断密码是否为空
                if (!TextUtils.isEmpty(password)) {
                    if (MD5Utils.encode(password).equals(savedPassword)) {
                        AntiTheftActivity.startAct(LoginActivity.this);
                    } else {
                        showToast("密码错误");
                    }
                } else {
                    showToast("密码不能为空");
                }
            }
        });

//        btnCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        dialog.setView(view, 0, 0, 0, 0);
//        dialog.show();
    }

    @Override
    protected void loadData() {

    }

}
