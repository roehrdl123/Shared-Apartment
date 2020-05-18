package at.wifi.swdev.android.wgapp.data;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Artikel implements Serializable
{
    private String id;
    private int anzahl;
    private String titel;
    private String bezeichnung;
    private boolean erledigt = false;

    public Artikel(int anzahl, String titel)
    {
        this.anzahl = anzahl;
        this.titel = titel;
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

    public int getAnzahl()
    {
        return anzahl;
    }

    public void setAnzahl(int anzahl)
    {
        this.anzahl = anzahl;
    }

    public String getTitel()
    {
        return titel;
    }

    public void setTitel(String titel)
    {
        this.titel = titel;
    }

    public String getBezeichnung()
    {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung)
    {
        this.bezeichnung = bezeichnung;
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
        //TODO: toString ändern
        return titel;
    }
}
