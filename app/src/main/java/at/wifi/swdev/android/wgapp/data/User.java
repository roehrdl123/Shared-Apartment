package at.wifi.swdev.android.wgapp.data;

import java.io.Serializable;

public class User implements Serializable
{
    public String id;
    public String name;
    public String imageURL;


    public User()
    {

    }

    public User(String id, String name, String imageURL)
    {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
    }

    public User(String id, String name)
    {
        this.id = id;
        this.name = name;
        imageURL = "";
    }
}
