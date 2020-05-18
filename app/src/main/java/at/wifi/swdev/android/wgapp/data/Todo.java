package at.wifi.swdev.android.wgapp.data;

import java.io.Serializable;

public class Todo implements Serializable
{
    private String id;
    private String title;
    private String content;
    private boolean erledigt = false;

    public Todo(String title, String content)
    {
        this.title = title;
        this.content = content;
    }

    public Todo()
    {
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

    public boolean isErledigt()
    {
        return erledigt;
    }

    public void setErledigt(boolean erledigt)
    {
        this.erledigt = erledigt;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
