package com.example.connexusandroid;

import java.util.ArrayList;
import java.util.Date;

public class ConnexusStream {

    long id;
    String name;
    String tags;
    Date createDate;
    String coverImageUrl;

    public ConnexusStream() {
    }

    @SuppressWarnings("serial")
    public static class List extends ArrayList<ConnexusStream> {
    }
}