package com.app.freya.http;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InterruptedIOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class OkHttpRetryInterceptor implements Interceptor {

    public int executionCount; //最大重试次数
    private long retryInterval; //重试的间隔

    public OkHttpRetryInterceptor(Builder builder){
        this.executionCount = builder.executionCount;
        this.retryInterval = builder.retryInterval;

    }


    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = doRequest(chain, request);
        if(request == null){
            return null;
        }
        int retryNum = 0;
        while ((response == null || !response.isSuccessful()) && retryNum <= executionCount) {
            Timber.e("intercept Request is not successful - {}"+retryNum);
            final long nextInterval = getRetryInterval();
            try {
                Timber.e("Wait for {}"+nextInterval);
                Thread.sleep(nextInterval);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            }
            retryNum++;
            // retry the request
            response = doRequest(chain, request);
        }
        return response;
    }


    private Response doRequest(Chain chain,Request request){
        Response response = null;
        try {
            response = chain.proceed(request);
            return response;
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * retry间隔时间
     */
    public long getRetryInterval() {
        return this.retryInterval;
    }

    public final static class Builder{
        private int executionCount;

        private long retryInterval;

        public Builder(){
            executionCount = 3;
            retryInterval = 1000;
        }


        public OkHttpRetryInterceptor.Builder executionCount(int count){
            this.executionCount = count;
            return this;
        }

        public OkHttpRetryInterceptor.Builder retryInterval(int interval){
            this.retryInterval = interval;
            return this;
        }

        public OkHttpRetryInterceptor build(){
            return new OkHttpRetryInterceptor(this);
        }
    }
}
