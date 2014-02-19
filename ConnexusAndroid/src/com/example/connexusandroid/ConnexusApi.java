package com.example.connexusandroid;

import retrofit.http.GET;
import retrofit.http.Query;

public interface ConnexusApi {

    @GET("/AllStreamsServletAPI")
    ConnexusStream.List allStreams();
    //void allStreams(Callback<ConnexusStream.List> streams);
    
    @GET("/SingleStreamServletAPI")
    ConnexusPhoto.List singleStream(@Query("streamId") long streamId);
    
   // @GET("/UploadServletAPI")
   // boolean uploadPhoto(@Query("streamId") long streamId, @Query("streamName") String streamName, Image img);
}