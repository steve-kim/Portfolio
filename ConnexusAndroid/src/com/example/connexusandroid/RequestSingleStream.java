package com.example.connexusandroid;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public class RequestSingleStream extends RetrofitSpiceRequest<ConnexusPhoto.List, ConnexusApi> {
 
	private long streamId;
	
    public RequestSingleStream(long stream) {
        super(ConnexusPhoto.List.class, ConnexusApi.class);
        this.streamId = stream;
    }

    @Override
    public ConnexusPhoto.List loadDataFromNetwork() throws Exception {
        return getService().singleStream(streamId);
    }

}