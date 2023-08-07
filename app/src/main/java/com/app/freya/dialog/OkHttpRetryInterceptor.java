package com.app.freya.dialog;

import java.io.IOException;
import java.io.InterruptedIOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by Admin
 * Date 2023/7/13
 *
 * @author Admin
 */
public class OkHttpRetryInterceptor implements Interceptor {


    public int executionCount;//最大重试次数
    private long retryInterval;//重试的间隔

    public OkHttpRetryInterceptor(Builder builder) {
        this.executionCount = builder.executionCount;
        this.retryInterval = builder.retryInterval;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = doRequest(chain, request);
        int retryNum = 0;
        while ((response == null || !response.isSuccessful()) && retryNum <= executionCount) {
            //Timber.e("intercept Request is not successful - {}",retryNum);
            Timber.e("intercept Request is not successful - {}"+retryNum);
            final long nextInterval = getRetryInterval();
            try {
                //log.info("Wait for {}",nextInterval);
                Timber.e("Wait for {}\",nextInterval"+nextInterval);
                Thread.sleep(nextInterval);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            }
            retryNum++;
            if(response != null){
                response.close();
            }

            // retry the request
            response = doRequest(chain, request);
        }
        //response.close();
        return response;
    }

    private Response doRequest(Chain chain, Request request) {
        Response response = null;
        try {
            response = chain.proceed(request);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return response;
        }

    }

    /**
     * retry间隔时间
     */
    public long getRetryInterval() {
        return this.retryInterval;
    }

    public static final class Builder {
        private int executionCount;
        private long retryInterval;

        public Builder() {
            executionCount = 3;
            retryInterval = 1000;
        }

        public OkHttpRetryInterceptor.Builder executionCount(int executionCount) {
            this.executionCount = executionCount;
            return this;
        }

        public OkHttpRetryInterceptor.Builder retryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        public OkHttpRetryInterceptor build() {
            return new OkHttpRetryInterceptor(this);
        }
    }

}
