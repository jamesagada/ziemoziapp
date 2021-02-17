package com.codename1.startapp;


/**
 * 
 *  @author Chen
 */
public interface StartAppNative extends com.codename1.system.NativeInterface {

	public void initSDK(String accountId, String appId, boolean enableReturnAd);

	public void showAd();

	public void loadAd(int type);
}
