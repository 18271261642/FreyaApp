package com.blala.blalable.listener;

import com.inuker.bluetooth.library.search.SearchResult;

public interface BleScanListener {

    void onSearchStarted();


    void onDeviceFounded(SearchResult searchResult);


    void onSearchStopped();

    void onSearchCanceled();

}
