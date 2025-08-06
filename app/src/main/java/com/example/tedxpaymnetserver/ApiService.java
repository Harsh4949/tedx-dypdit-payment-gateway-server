package com.example.tedxpaymnetserver;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ApiService {

    //    @POST("queue-received-payments")
    //https://tedx-dypdit-portal-backend-production.up.railway.app/

    @POST
    Call<Void> sendTransaction(@retrofit2.http.Url String url, @Body TransactionData data);
}
