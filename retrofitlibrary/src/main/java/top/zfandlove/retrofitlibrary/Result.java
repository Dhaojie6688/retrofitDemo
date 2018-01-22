package top.zfandlove.retrofitlibrary;

import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by zfan on 2018/1/16.
 */

public abstract class Result<T> implements Observer<T> {

    private static final String SOCKET_TIMEOUT_EXCEPTION = "网络连接超时，请检查您的网络状态，稍后重试";
    private static final String CONNECT_EXCEPTION = "网络连接异常，请检查您的网络状态";
    private static final String UNKNOWN_HOST_EXCEPTION = "网络异常，请检查您的网络状态";
    final String TAG = "http_result";
    private Dialog mDialog;
    private Disposable mDisposable;

    public Result() {
    }

    public Result(Dialog mDialog) {
        this.mDialog = mDialog;
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                release();
            }
        });
    }

    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
        subscribe(mDisposable);
    }

    @Override
    public void onNext(Object o) {
        success((T) o);
        release();
    }

    @Override
    public void onError(Throwable e) {
        httpError(e);
        release();
    }

    @Override
    public void onComplete() {

    }

    void release() {
        if (mDisposable == null) return;
        mDisposable.dispose();
        mDisposable = null;
    }

    protected abstract void success(T t);

    protected abstract void error(String t);

    protected abstract void subscribe(Disposable d);

    private void httpError(Throwable throwable) {
        if (throwable instanceof SocketTimeoutException) {
            error(SOCKET_TIMEOUT_EXCEPTION);
            Log.d(TAG, "httpError: " + SOCKET_TIMEOUT_EXCEPTION);
        } else if (throwable instanceof ConnectException) {
            error(CONNECT_EXCEPTION);
            Log.d(TAG, "httpError: " + CONNECT_EXCEPTION);
        } else if (throwable instanceof UnknownHostException) {
            error(UNKNOWN_HOST_EXCEPTION);
            Log.d(TAG, "httpError: " + UNKNOWN_HOST_EXCEPTION);
        } else {
            error(getHttpException(throwable.toString()));
            Log.d(TAG, "httpError: " + getHttpException(throwable.toString()));
        }

    }

    private String getHttpException(String throwable) {
        return throwable.toString().substring(throwable.lastIndexOf(":") + 1, throwable.length());
    }

}
