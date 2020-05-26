package at.wifi.swdev.android.wgapp.data;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Todo implements Serializable
{
    private String id;
    private String title;
    private String content;
    private boolean done = false;

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

    public boolean isDone()
    {
        return done;
    }

    public void setDone(boolean done)
    {
        this.done = done;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString()
    {
        return title;
    }
}
