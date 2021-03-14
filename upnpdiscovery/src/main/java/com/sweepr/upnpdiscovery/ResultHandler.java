package com.sweepr.upnpdiscovery;

public interface ResultHandler<T> {
    void onSuccess(T data);
    void onFailure(Exception e);
}
