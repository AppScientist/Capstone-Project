package com.krypto.offlineviewer.model.Articles;

import java.io.Serializable;


public class Articles implements Serializable{

    private String title,url,desc,image;

    public Articles(String title, String url, String desc, String image) {

        this.title = title;
        this.url = url;
        this.desc =desc;
        this.image= image;
    }


    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }
}
