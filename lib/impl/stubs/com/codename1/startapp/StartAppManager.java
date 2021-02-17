package com.codename1.startapp;


/**
 * 
 *  @author Chen
 */
public class StartAppManager {

	public static final int AD_INTERSTITIALS = 0;

	public static final int AD_REWARDED_VIDEO = 1;

	public StartAppManager() {
	}

	/**
	 *  Init the startapp Android SDK
	 * 
	 *  @param appId
	 *  @param enableReturnAd 
	 */
	public void initAndroidSDK(String accountId, String appId, boolean enableReturnAd) {
	}

	/**
	 *  Init the startapp iOS SDK
	 * 
	 *  @param accountId 
	 *  @param appId
	 *  @param enableReturnAd 
	 */
	public void initIOSSDK(String accountId, String appId, boolean enableReturnAd) {
	}

	/**
	 *  Show an Ad, call loadAd before.
	 */
	public void showAd() {
	}

	/**
	 *  Load an Ad, this might take a few seconds.
	 *  @param type the Ad to load: AD_INTERSTITIALS or AD_REWARDED_VIDEO
	 */
	public void loadAd(int type) {
	}

	/**
	 *  Sets an Ad listener to receive Ads callbacks
	 *  @param l StartAppAdListener Object
	 */
	public void setAdsListener(StartAppAdListener l) {
	}
}
