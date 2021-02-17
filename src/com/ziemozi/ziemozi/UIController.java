package com.ziemozi.ziemozi;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.ziemozi.forms.LoginForm;
import com.ziemozi.forms.MainForm;
import com.ziemozi.forms.SignupForm;
import com.ziemozi.server.ServerAPI;
import com.codename1.ui.Form;
import com.codename1.ui.layouts.BorderLayout;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Display;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.animations.MorphTransition;
import com.codename1.ui.animations.Motion;
import com.codename1.ui.util.UITimer;
import com.ziemozi.server.local.localAPI;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import com.codename1.io.Log;
//import java.util.logging.Level;
//import java.util.logging.Logger;

public class UIController {
    private static MainForm main;
    public static void showSplashScreen() {
        Form splash = new Form(new BorderLayout(
                    BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE));
        
        splash.addShowListener(e -> afterSplash());
       Image bgImage;
        try {
            bgImage = Image.createImage("/ziemozi.png");
        } catch (IOException ex) {
            bgImage=Image.createImage(10, 10);
        }
       splash.getAllStyles().setBgImage(bgImage);      
       splash.add(CENTER,new InfiniteProgress());
       Display.getInstance().callSerially(()->{
           ////log.p("Initiatilizing local database");
       ServerAPI.initDb();
       });
        //splash.setUIID("SplashForm");
        //Label logo = new Label("\uf301", "IconFont");
        Label logo = new Label("Ziemozi");
        logo.setName("Logo");
        logo.setUIID("Title");
        
        splash.add(CENTER, logo);
        splash.setTransitionOutAnimator(
                MorphTransition.
                        create(2400).
                        morph("Logo"));
        final Motion anim = Motion.createLinearMotion(0, 127, 1000);
        anim.start();
        UITimer.timer(100, true, splash, () -> {
            if(anim.isFinished()) {
                //
                if(!ServerAPI.isLoggedIn()) {
                    //just show mainui. You only log in
                    //or ask to register when he wants to send a post
                    showLoginForm();
                    //showMainUI();
                } else {
                    //attempt tp synchronize
//                    localAPI.saveRequestsToServer();    
//                                        ServerAPI.loadToLocalDatabase();
                    showMainUI();
                }
            } else {
                logo.getUnselectedStyle().setOpacity(anim.getValue() + 127);
                logo.repaint();
            }
        });

        splash.show();
    }
    
    public static void showLoginForm() {
        new LoginForm().show();
    }
    
    public static void showSignup() {
        SignupForm.createTerms().show();
    }
    
    public static void showMainUI() {
        if(main == null) {
            main = new MainForm();
        }
//                            ToastBar.showInfoMessage("Syncing with Server ...");


        main.show();
    }
    
    public static void refresh() {
        if(main != null) {
            main.refresh();
        }
    }

    private static void afterSplash() {
    //    ToastBar.showInfoMessage("Syncing with Server ..");

       
        Timer t = new Timer();
          TimerTask ta = new TimerTask(){

             public void run() 
             {
                                 // //log.p("Syncing definitions ");
               //ToastBar.showInfoMessage("Syncing messages ...");
               localAPI.saveRequestsToServer();                 
                ServerAPI.loadRequests();
             }
          };
          TimerTask tb = new TimerTask(){

             public void run() 
             {
//                 ToastBar.showInfoMessage("Syncing definitions and setups ...");
                 ////log.p("Syncing definitions ");
                ServerAPI.loadDefinitions();
             }
          };

        t.schedule(ta,900,1200);  
        t.schedule(tb, 900,900);
       }
}
