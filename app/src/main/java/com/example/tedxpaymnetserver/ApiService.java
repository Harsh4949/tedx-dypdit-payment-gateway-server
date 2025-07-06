package com.example.tedxpaymnetserver;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("3e97cbbb-64f8-4908-9ce2-272e323ecbb3")
        //replace with server URI
    Call<Void> sendTransaction(@Body TransactionData data);

}
