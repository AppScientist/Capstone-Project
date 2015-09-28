package com.krypto.offlineviewer.Utilities;

import android.content.Context;
import android.graphics.Typeface;

import com.krypto.offlineviewer.model.Articles.ArticleContent;
import com.krypto.offlineviewer.model.Articles.Articles;

import org.nustaq.serialization.FSTConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;


public class Utility {

    private static FSTConfiguration conf;


    /**
     * Creates a FSTConfiguration singleton instance.
     * @return FSTConfiguration instance
     */
    public static FSTConfiguration getInstance() {
        if (conf == null) {
            conf = FSTConfiguration.createDefaultConfiguration();
            conf.registerClass(Articles.class, String.class);
        }

        return conf;
    }


    private static final Hashtable<String, Typeface> CACHE = new Hashtable<>();

    /**
     * Stores two custom font files in Hashtable,so that only one instance of each is present for the whole application.
     *
     * @param c         Context of the activity
     * @param assetPath Path of the custom font file
     * @return Typeface font from the requested asset path
     */
    public static Typeface getFont(Context c, String assetPath) {

        synchronized (CACHE) {
            if (!CACHE.contains(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(), assetPath);
                    CACHE.put(assetPath, t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return CACHE.get(assetPath);
    }

    /**
     * Serializes a List to byte array
     * @param obj List to serialize
     * @return    Serialized byte array
     * @throws IOException
     */
    public static byte[] serialize(List<Articles> obj) throws IOException {
        return getInstance().asByteArray(obj);
    }

    /**
     * Deserializes byte array to List
     * @param bytes Byte array to deserialize
     * @return      Deserialized List
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Articles> deserializeToList(byte[] bytes) throws IOException, ClassNotFoundException {
        return (List<Articles>) getInstance().asObject(bytes);
    }


    /**
     * Stores text data of webpage to internal cache
     * @param context
     * @param url
     * @param articles
     */
    public static void storeTextToCache(Context context, String url, ArticleContent articles) {

        File path = context.getFilesDir();

        File dir = new File(path + File.separator + "webcache");
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdir();
        }
        url = url.replace("/", "-");
        File file = new File(dir, url + "_cache");
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        }


        String title = articles.getTitle();
        String body = articles.getContent();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!doctype html><html><head><h1>")
                .append(title)
                .append("</h1><hr />")
                .append("</head><body><div>")
                .append(body)
                .append("</div></body></html>");
        String plainText = stringBuilder.toString();

        File articleContent = new File(file, url + "_plaincontent.html");

        try {
            FileOutputStream articleStream = new FileOutputStream(articleContent);
            try {
                articleStream.write(plainText.getBytes());
            } finally {
                articleStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Stores HTML data of webpage to internal cache
     * @param context
     * @param url
     * @param content
     */
    public static void storeHtmlToCache(Context context, String url, String content) {

        File path = context.getFilesDir();

        File dir = new File(path + File.separator + "webcache");
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdir();
        }
        url = url.replace("/", "-");
        File file = new File(dir, url + "_cache");
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        }

        File fileContent = new File(file, url + "_fullcontent.html");

        try {
            FileOutputStream stream = new FileOutputStream(fileContent);
            try {
                stream.write(content.getBytes());
            } finally {
                stream.close();
            }

        } catch (IOException i) {
            i.printStackTrace();
        }


    }

    /**
     * Stores all images in a webpage to internal cache
     * @param context
     * @param url
     * @param resourceName
     * @param stream
     */
    public static void storeImagesToCache(Context context, String url, String resourceName, InputStream stream) {

        File path = context.getFilesDir();

        File dir = new File(path + File.separator + "webcache");
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdir();
        }
        url = url.replace("/", "-");
        File file = new File(dir, url + "_cache");
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        }

        resourceName = resourceName.replace("/", "-");
        File imageContent = new File(file, resourceName);
        try {

            FileOutputStream imageStream = new FileOutputStream(imageContent);
            try {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = stream.read(bytes)) != -1) {
                    imageStream.write(bytes, 0, read);
                }
            } finally {
                imageStream.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves Text data from Cache
     * @param context
     * @param url
     * @return
     */
    public static String getTextFromCache(Context context, String url) {
        File path = context.getFilesDir();
        url = url.replace("/", "-");
        File dir = new File(path + File.separator + "webcache" + File.separator + url + "_cache" + File.separator + url + "_plaincontent.html");

        int length = (int) dir.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(dir);

            try {
                in.read(bytes);
            } finally {
                in.close();
            }
        } catch (IOException i) {
            i.printStackTrace();
        }

        try {
            return new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Retreives Html data from internal cache.
     * @param context
     * @param url
     * @return
     */
    public static String getHtmlFromCache(Context context, String url) {
        File path = context.getFilesDir();
        url = url.replace("/", "-");
        File dir = new File(path + File.separator + "webcache" + File.separator + url + "_cache" + File.separator + url + "_fullcontent.html");

        int length = (int) dir.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(dir);

            try {
                in.read(bytes);
            } finally {
                in.close();
            }
        } catch (IOException i) {
            i.printStackTrace();
        }

        try {
            return new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Retrieves images stored in cache as stream
     * @param context
     * @param url
     * @param imageUrl
     * @return
     */
    public static InputStream getImagesFromCache(Context context, String url, String imageUrl) {


        File path = context.getFilesDir();
        url = url.replace("/", "-");
        File dir = new File(path + File.separator + "webcache" + File.separator + url + "_cache");
        imageUrl = imageUrl.replace("/", "-");
        try {
            return new FileInputStream(new File(dir, imageUrl));
        } catch (IOException i) {
            i.printStackTrace();
        }

        return null;
    }

    /**
     * Checks if imagefile for a particluar webpage exists
     * @param context
     * @param url
     * @param imageUrl
     * @return
     */
    public static boolean imageFileExists(Context context, String url, String imageUrl) {

        File path = context.getFilesDir();
        url = url.replace("/", "-");
        File dir = new File(path + File.separator + "webcache" + File.separator + url + "_cache");
        imageUrl = imageUrl.replace("/", "-");
        File imageFile = new File(dir, imageUrl);
        return imageFile.exists();
    }

    /**
     * Checks if HTML content of webpage exists.
     * @param context
     * @param url
     * @return
     */
    public static boolean htmlFileExists(Context context, String url) {

        File path = context.getFilesDir();
        url = url.replace("/", "-");
        File dir = new File(path + File.separator + "webcache" + File.separator + url + "_cache" + File.separator + url + "_fullcontent.html");
        return dir.exists();
    }

    /**
     * Checks if plain text content of webpage exists
     * @param context
     * @param url
     * @return
     */
    public static boolean textFileExists(Context context, String url) {

        File path = context.getFilesDir();
        url = url.replace("/", "-");
        File dir = new File(path + File.separator + "webcache" + File.separator + url + "_cache" + File.separator + url + "_plaincontent.html");
        return dir.exists();
    }


    /**
     * Deletes the content of a particular webpage
     * @param context
     * @param url
     */
    public static void deleteCache(Context context, String url) {

        File path = context.getFilesDir();
        url = url.replace("/", "-");
        File dir = new File(path + File.separator + "webcache" + File.separator + url + "_cache");
        if (dir.isDirectory())
            for (File child : dir.listFiles())
                DeleteRecursive(child);

        dir.delete();
    }

    private static void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                child.delete();
                DeleteRecursive(child);
            }

        fileOrDirectory.delete();
    }
}
