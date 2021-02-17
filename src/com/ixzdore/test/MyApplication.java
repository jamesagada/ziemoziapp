package com.ixzdore.test;


import static com.codename1.ui.CN.*;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;
import com.codename1.ui.Toolbar;
import java.io.IOException;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.io.NetworkEvent;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.codename1.components.InfiniteProgress;
import com.codename1.components.ToastBar;
import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.ConnectionRequest.CachingMode;
import static com.codename1.io.ConnectionRequest.setDefaultCacheMode;
import com.ziemozi.server.ServerAPI;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;
import com.codename1.ui.Toolbar;
import com.codename1.io.Preferences;
import com.codename1.push.Push;
import com.codename1.push.PushCallback;
import com.codename1.io.File;
import com.codename1.io.FileSystemStorage;
import static com.codename1.io.Log.REPORTING_DEBUG;
import com.codename1.io.Util;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.ui.Display;
import com.codename1.util.StringUtil;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.local.localAPI;
import com.ziemozi.ziemozi.UIController;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename
 * One</a> for the purpose of building native mobile applications using Java.
 */
public class MyApplication implements PushCallback {

    private Form current;
    private Resources theme;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);
        //
        //initialize random number generatr

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature
        Log.bindCrashProtection(true);
        Log.setReportingLevel(REPORTING_DEBUG);

        setDefaultCacheMode(CachingMode.OFF);

        addNetworkErrorListener(err -> {
                    // prevent the event from propagating
                    ////log.p(err.getMessage());
                    Log.sendLogAsync();
                    err.consume();
                    if (err.getError() != null) {
                        Log.e(err.getError());
                    }
                    ToastBar.showErrorMessage("Network not availavle");
                }

        );

    }

    public void start() {
        if (current != null) {

            current.show();
            return;
        }

        UIController.showSplashScreen();
        if (ServerAPI.isLoggedIn()) {
            callSerially(() -> registerPush());
        }

    }

    public void stop() {
        //ToastBar.showInfoMessage("Syncing with Server");
        //ServerAPI.loadToLocalDatabase();
        //localAPI.saveRequestsToServer();

        current = getCurrentForm();
        if (current instanceof Dialog) {
            ((Dialog) current).dispose();
            current = getCurrentForm();
        }

    }

    public void destroy() {
    }

    @Override
    public void push(String value) {
        UIController.refresh();
    }

    @Override
    public void registeredForPush(String deviceId) {
        String pk = Preferences.get("pushkey", "");
        if (!pk.equals(Push.getPushKey())) {
            if (ServerAPI.updatePushKey(pk)) {
                Preferences.set("pushkey", Push.getPushKey());
            }
        }
    }

    @Override
    public void pushRegistrationError(String error, int errorCode) {
        ////log.p("Failed to register for push: " + error);
    }


}
