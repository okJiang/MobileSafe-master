package net.xwdoor.mobilesafe.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import net.xwdoor.mobilesafe.R;
import net.xwdoor.mobilesafe.base.BaseActivity;
import net.xwdoor.mobilesafe.db.AddressQuery;
import net.xwdoor.mobilesafe.entity.AppInfo;
import net.xwdoor.mobilesafe.global.Config;
import net.xwdoor.mobilesafe.net.HttpRequest;
import net.xwdoor.mobilesafe.net.RequestCallback;
import net.xwdoor.mobilesafe.utils.MD5Utils;
import net.xwdoor.mobilesafe.utils.PrefUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 闪屏界面
 * Created by XWdoor on 2016/2/24 024 12:03.
 * 博客：http://blog.csdn.net/xwdoor
 */
public class SplashActivity extends BaseActivity {

    private TextView mTvVersion;
    private AppInfo mLocalAppInfo;
    private AppInfo mRemoteAppInfo;
    private TextView mTvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initVariables() {
        mLocalAppInfo = getLocalAppInfo();
        Log.i(TAG_LOG, "当前版本信息--->" + mLocalAppInfo.toJson());
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_splash);

        mTvVersion = (TextView) findViewById(R.id.tv_version);
        mTvVersion.setText("版本号：" + mLocalAppInfo.getVersionName());

        mTvProgress = (TextView) findViewById(R.id.tv_progress);

        RelativeLayout rlRoot = (RelativeLayout) findViewById(R.id.rl_root);
        AlphaAnimation anim = new AlphaAnimation(0.3f, 1);
        anim.setDuration(1000);
        rlRoot.startAnimation(anim);
    }

    @Override
    protected void loadData() {
        getAppInfoFromServer();
        copyDb(AddressQuery.DB_NAME);
    }

    /**
     * 从服务器中获取版本信息
     */
    private void getAppInfoFromServer() {
        HttpRequest httpRequest = new HttpRequest();
        final long timeStart = System.currentTimeMillis();
        httpRequest.requestGet(Config.VERSION_URL, new RequestCallback() {
            @Override
            public void onSuccess(String content) {
                Gson gson = new Gson();
                //将Json字符串解析为AppInfo对象
                mRemoteAppInfo = gson.fromJson(content, AppInfo.class);
                Log.i(TAG_LOG, "服务器版本信息--->" + mRemoteAppInfo);

                if (mRemoteAppInfo != null && mRemoteAppInfo.getVersionCode() > mLocalAppInfo.getVersionCode()) {
                    //有更新，弹出升级对话框
                    boolean autoUpdate = PrefUtils.getBoolean(PREF_AUTO_UPDATE, true, SplashActivity.this);
                    if (autoUpdate) {
                        showUpdateDialog();
                    } else {
//                        startHomeActivity();
                          showSafeDialog();
                    }
                } else {
                    //无更新，停留一段时间，然后进入主界面
                    long timeEnd = System.currentTimeMillis();
                    long timeUsed = timeEnd - timeStart;
                    try {
                        Thread.sleep(2000 - timeUsed);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    startHomeActivity();
                    showSafeDialog();
                }
            }

            @Override
            public void onFaile(String errorMsg) {
                Toast.makeText(SplashActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG_LOG, "获取版本信息异常--->" + errorMsg);
//                startHomeActivity();
                showSafeDialog();
            }
        });
    }

    /**
     * 进入主界面
     */
    private void startHomeActivity() {
        HomeActivity.startAct(SplashActivity.this);
        finish();
    }

    /**
     * 显示升级对话框
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_update);//设置标题：发现新版本
        builder.setMessage(mRemoteAppInfo.getDescription());//设置内容
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //开始下载apk
                downloadApk(mRemoteAppInfo.getDownloadUrl());
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startHomeActivity();
            }
        });

        //点击返回键的监听
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                startHomeActivity();
            }
        });
        builder.show();
    }

    /**
     * 下载APK
     *
     * @param downloadUrl apk文件的网络路径
     */
    private void downloadApk(String downloadUrl) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "没有找到sdcard", Toast.LENGTH_SHORT).show();
            return;
        }
        //显示下载进度
//        mTvProgress.setVisibility(View.VISIBLE);

        HttpUtils httpUtils = new HttpUtils();

        //获取sdcard根目录
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mobileSafe.apk";
        httpUtils.download(downloadUrl, path, new RequestCallBack<File>() {
            //下载成功
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                Log.i(TAG_LOG, "下载成功--->" + responseInfo.result.getAbsolutePath());
                installApk(responseInfo.result.getAbsolutePath());
            }

            //下载失败
            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(SplashActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                Log.i(TAG_LOG, "下载失败--->" + s);
                startHomeActivity();
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                Log.i(TAG_LOG, "total--->" + total + ",current--->" + current);
                int percent = (int) (current * 100 / total);
                mTvProgress.setText("下载进度：" + percent + "%");
            }
        });
    }

    /**
     * 安装APK
     *
     * @param apkPath apk文件路径
     */
    private void installApk(String apkPath) {
        Log.d(TAG_LOG, "安装apk");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //安装过程中，用户点击取消按钮
        startHomeActivity();
    }

    /**
     * 获取当前App版本信息
     */
    private AppInfo getLocalAppInfo() {
        AppInfo appInfo = new AppInfo();
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            appInfo.setVersionCode(packageInfo.versionCode);
            appInfo.setVersionName(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appInfo;
    }

    /**
     * 拷贝数据库
     * @param dbName 数据库文件名
     */
    private void copyDb(String dbName) {
        AssetManager assets = getAssets();
        File filesDir = getFilesDir();
        File desFile = new File(filesDir, dbName);

        if(desFile.exists()){
            showLog("copyDb()","数据库已存在");
            return;
        }
        InputStream in = null;
        FileOutputStream out = null;
        try {
            //in=context.getClass().getClassLoader().getResourceAsStream("assets/"+names[i]);
            in = assets.open(dbName);
            out = new FileOutputStream(desFile);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                out.flush();
            }
        } catch (IOException e) {
            showLog("copyDb error",e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                showLog("copyDb io error",e.getMessage());
            }
        }
    }

    /**
     * 显示手机防盗弹窗
     */
    private void showSafeDialog() {
        String password = PrefUtils.getString(PREF_PASSWORD, "", this);

        //判断是否存在密码
        if (!TextUtils.isEmpty(password)) {
            //显示输入密码弹窗
            showInputPasswordDialog();
        } else {
            //显示设置密码弹窗
            showSetPasswordDialog();
        }
//        finish();

    }

    /**
     * 输入密码的弹窗
     */
    private void showInputPasswordDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = View.inflate(this, R.layout.dialog_input_password, null);
        Button btnOk = (Button) view.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        final EditText etPassword = (EditText) view.findViewById(R.id.et_password);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                //获取保存的密码
                String savedPassword = PrefUtils.getString(PREF_PASSWORD, "", SplashActivity.this);

                //判断密码是否为空
                if (!TextUtils.isEmpty(password)) {
                    if (MD5Utils.encode(password).equals(savedPassword)) {
                        AntiTheftActivity.startAct(SplashActivity.this);
                        dialog.dismiss();
                    } else {
                        showToast("密码错误");
                    }
                } else {
                    showToast("密码不能为空");
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }

    /**
     * 设置密码的弹窗
     */
    private void showSetPasswordDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = View.inflate(this, R.layout.dialog_set_password, null);
        Button btnOk = (Button) view.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        final EditText etPassword = (EditText) view.findViewById(R.id.et_password);
        final EditText etPasswordConfirm = (EditText) view.findViewById(R.id.et_password_confirm);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                String passwordConfirm = etPasswordConfirm.getText().toString().trim();

                if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordConfirm)) {
                    if (password.equals(passwordConfirm)) {
                        PrefUtils.putString(PREF_PASSWORD, MD5Utils.encode(password), SplashActivity.this);
                        AntiTheftActivity.startAct(SplashActivity.this);
                        dialog.dismiss();
                    } else {
                        showToast("两次密码不一致");
                    }
                } else {
                    showToast("密码不能为空");
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }
}