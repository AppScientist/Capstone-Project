package com.krypto.offlineviewer.service;

import com.krypto.offlineviewer.model.Articles.ArticleContent;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;


public class UrlInterfaces {


    public interface APIService {

        @GET("api/content/v1/parser")
        Call<ArticleContent> loadRepo(@Query("url") String url ,@Query("token") String token);

    }

}
