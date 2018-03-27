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
 * Created by Administrator on 2018/3/27.
 */

public class SetActivity extends BaseActivity {

//    private TextView mTitleTV;
//    public EditText etPassword;
//    public EditText etPasswordConfirm;
//    private MyCallBack myCallBack;
//    public Button btnOk;
//    public Button btnCancel;

    public static void startAct(Context context) {
        Intent intent = new Intent(context, SetActivity.class);
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
        setContentView(R.layout.dialog_set_password);

//        final AlertDialog dialog = new AlertDialog.Builder(this).create();
//        View view = View.inflate(this, R.layout.dialog_set_password, null);
        Button btnOk = (Button) findViewById(R.id.btn_ok);
//        btnCancel = (Button) findViewById(R.id.btn_cancel);
        final EditText etPassword = (EditText) findViewById(R.id.et_password);
        final EditText etPasswordConfirm = (EditText) findViewById(R.id.et_password_confirm);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                String passwordConfirm = etPasswordConfirm.getText().toString().trim();

                if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordConfirm)) {
                    if (password.equals(passwordConfirm)) {
                        PrefUtils.putString(PREF_PASSWORD, MD5Utils.encode(password), SetActivity.this);
                        AntiTheftActivity.startAct(SetActivity.this);
//                        dialog.dismiss();
                    } else {
                        showToast("两次密码不一致");
                    }
                } else {
                    showToast("密码不能为空");
                }
            }
        });
    }

    @Override
    protected void loadData() {

    }


}
