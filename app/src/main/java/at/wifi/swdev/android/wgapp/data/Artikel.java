package at.wifi.swdev.android.wgapp.data;

import java.io.Serializable;

public class Artikel implements Serializable
{
    private String id;
    private int quantity;
    private String title;
    private String content;
    private boolean done = false;


    public Artikel(int quantity, String title)
    {
        this.quantity = quantity;
        this.title = title;
    }

    public Artikel()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
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

    public boolean isDone()
    {
        return done;
    }

    public void setDone(boolean done)
    {
        this.done = done;
    }

    @Override
    public String toString()
    {
        return title;
    }
}
