package at.wifi.swdev.android.wgapp.data;

import java.io.Serializable;

public class Calendar implements Serializable
{
    private String id;
    private String title;
    private String content;
    private long dateStart;
    private long dateEnd;
    private boolean notification;

    public Calendar()
    {
    }

    public Calendar(String title, String content)
    {
        this.title = title;
        this.content = content;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public long getDateStart()
    {
        return dateStart;
    }

    public void setDateStart(long dateStart)
    {
        this.dateStart = dateStart;
    }

    public long getDateEnd()
    {
        return dateEnd;
    }

    public void setDateEnd(long dateEnd)
    {
        this.dateEnd = dateEnd;
    }

    public boolean isNotification()
    {
        return notification;
    }

    public void setNotification(boolean notification)
    {
        this.notification = notification;
    }
}
