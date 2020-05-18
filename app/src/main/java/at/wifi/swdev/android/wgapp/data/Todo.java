package at.wifi.swdev.android.wgapp.data;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Todo implements Serializable
{
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

    @NonNull
    @Override
    public String toString()
    {
        return title;
    }
}
