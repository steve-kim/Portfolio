package com.example.connexusandroid;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class RequestAllStreams extends RetrofitSpiceRequest<ConnexusStream.List, ConnexusApi> {

    public RequestAllStreams() {
        super(ConnexusStream.List.class, ConnexusApi.class);
    }

    @Override
    public ConnexusStream.List loadDataFromNetwork() throws Exception {
        return getService().allStreams();
    }

}