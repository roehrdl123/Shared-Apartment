package at.wifi.swdev.android.wgapp.data;

import java.io.Serializable;

public class User implements Serializable
{
    public String id;
    public String name;


    public User()
    {

    }

    public User(String id, String name)
    {
        this.id = id;
        this.name = name;
    }
}
