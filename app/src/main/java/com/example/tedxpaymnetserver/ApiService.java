package com.example.tedxpaymnetserver;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    //    @POST("caa2f785-01b5-4ed7-9a97-6644ca60ad85")
    @POST("queue-received-payments")
        //replace with server URI
    Call<Void> sendTransaction(@Body TransactionData data);

}
