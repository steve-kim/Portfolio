package com.example.connexusandroid;

import java.util.ArrayList;
import java.util.Date;

public class ConnexusPhoto {

    long id;
    long streamId;
    String comments;
    String bkUrl;
    Date createDate;
    double latitude;
    double longitude;

    public ConnexusPhoto() {
    }

    @SuppressWarnings("serial")
    public static class List extends ArrayList<ConnexusPhoto> {
    }
}