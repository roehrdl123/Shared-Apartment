package at.wifi.swdev.android.wgapp.data;

import java.util.Map;

public class QRItems
{
    private String key;
    private int qrId;
    private String qrCodeURL;
    private Map<String, Artikel> articles;
    private boolean occupied = false;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public int getQrId()
    {
        return qrId;
    }

    public void setQrId(int qrId)
    {
        this.qrId = qrId;
    }

    public Map<String, Artikel> getArticles()
    {
        return articles;
    }

    public void setArticles(Map<String, Artikel> articles)
    {
        this.articles = articles;
    }

    public String getQrCodeURL()
    {
        return qrCodeURL;
    }

    public void setQrCodeURL(String qrCodeURL)
    {
        this.qrCodeURL = qrCodeURL;
    }

    public boolean isOccupied()
    {
        return occupied;
    }

    public void setOccupied(boolean occupied)
    {
        this.occupied = occupied;
    }
}
