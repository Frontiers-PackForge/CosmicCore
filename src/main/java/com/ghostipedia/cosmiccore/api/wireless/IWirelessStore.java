package com.ghostipedia.cosmiccore.api.wireless;

public interface IWirelessStore<T> {

    void clearData();

    void uploadData(T data);

    T downloadData();
}
