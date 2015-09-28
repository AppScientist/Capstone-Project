package com.krypto.offlineviewer.model.Articles;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ArticleContent {

    @Expose
    private String domain;
    @SerializedName("next_page_id")
    @Expose
    private Object nextPageId;
    @Expose
    private String url;
    @SerializedName("short_url")
    @Expose
    private String shortUrl;
    @Expose
    private String author;
    @Expose
    private String excerpt;
    @Expose
    private String direction;
    @SerializedName("word_count")
    @Expose
    private int wordCount;
    @SerializedName("total_pages")
    @Expose
    private int totalPages;
    @Expose
    private String content;
    @SerializedName("date_published")
    @Expose
    private String datePublished;
    @Expose
    private Object dek;
    @SerializedName("lead_image_url")
    @Expose
    private String leadImageUrl;
    @Expose
    private String title;
    @SerializedName("rendered_pages")
    @Expose
    private int renderedPages;

    /**
     *
     * @return
     * The domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     *
     * @param domain
     * The domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     *
     * @return
     * The nextPageId
     */
    public Object getNextPageId() {
        return nextPageId;
    }

    /**
     *
     * @param nextPageId
     * The next_page_id
     */
    public void setNextPageId(Object nextPageId) {
        this.nextPageId = nextPageId;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The shortUrl
     */
    public String getShortUrl() {
        return shortUrl;
    }

    /**
     *
     * @param shortUrl
     * The short_url
     */
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    /**
     *
     * @return
     * The author
     */
    public String getAuthor() {
        return author;
    }

    /**
     *
     * @param author
     * The author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     *
     * @return
     * The excerpt
     */
    public String getExcerpt() {
        return excerpt;
    }

    /**
     *
     * @param excerpt
     * The excerpt
     */
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    /**
     *
     * @return
     * The direction
     */
    public String getDirection() {
        return direction;
    }

    /**
     *
     * @param direction
     * The direction
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     *
     * @return
     * The wordCount
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     *
     * @param wordCount
     * The word_count
     */
    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    /**
     *
     * @return
     * The totalPages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     *
     * @param totalPages
     * The total_pages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     *
     * @return
     * The content
     */
    public String getContent() {
        return content;
    }

    /**
     *
     * @param content
     * The content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     *
     * @return
     * The datePublished
     */
    public String getDatePublished() {
        return datePublished;
    }

    /**
     *
     * @param datePublished
     * The date_published
     */
    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    /**
     *
     * @return
     * The dek
     */
    public Object getDek() {
        return dek;
    }

    /**
     *
     * @param dek
     * The dek
     */
    public void setDek(Object dek) {
        this.dek = dek;
    }

    /**
     *
     * @return
     * The leadImageUrl
     */
    public String getLeadImageUrl() {
        return leadImageUrl;
    }

    /**
     *
     * @param leadImageUrl
     * The lead_image_url
     */
    public void setLeadImageUrl(String leadImageUrl) {
        this.leadImageUrl = leadImageUrl;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The renderedPages
     */
    public int getRenderedPages() {
        return renderedPages;
    }

    /**
     *
     * @param renderedPages
     * The rendered_pages
     */
    public void setRenderedPages(int renderedPages) {
        this.renderedPages = renderedPages;
    }

}
