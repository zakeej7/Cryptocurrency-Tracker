/*
 * NewsData.java
 * Object class to model a news article received by an API
 */

public class NewsData {
    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;
    private String content;

    // constructor
    public NewsData(String author, String title, String description, String url, String urlToImage, String publishedAt, String content) {
        this.author      = author;
        this.title       = title;
        this.description = description;
        this.url         = url;
        this.urlToImage  = urlToImage;
        this.publishedAt = publishedAt;
        this.content     = content;
    }

    // getters
    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Author: " + getAuthor());
        sb.append("\n");
        sb.append("Title: " + getTitle());
        sb.append("\n");
        sb.append("Description: " + getDescription());
        sb.append("\n");
        sb.append("URL: " + getUrl());
        sb.append("\n");
        sb.append("URL to Image: " + getUrlToImage());
        sb.append("\n");
        sb.append("Published At: " + getPublishedAt());
        sb.append("\n");
        sb.append("Content: " + getContent());
        sb.append("\n");

        return sb.toString();
    }
}
