package com.ziemozi.forms;

//import com.codename1.camerakit.CameraKit;
import java.io.IOException;

import com.codename1.capture.Capture;
import com.codename1.components.FloatingActionButton;
import com.codename1.components.RSSReader;
import com.codename1.components.ToastBar;
import com.codename1.contacts.Contact;
import com.codename1.io.Log;
import com.codename1.ui.Image;
import com.codename1.ui.plaf.Style;
import com.ziemozi.server.ServerAPI;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.Tabs;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.Service;

public class MainForm extends Form {

    private final Tabs mainUI = new Tabs();
    private final NewsfeedContainer newsfeed = new NewsfeedContainer();
    private final NotificationsContainer notifications
            = new NotificationsContainer();
    private final ContactsContainer contacts = new ContactsContainer();
    private final ContactsfeedContainer contactfeed = new ContactsfeedContainer();
    private final ProvidersFeedContainer providerfeed = new ProvidersFeedContainer();

    public MainForm() {
        super("", new BorderLayout());
        newsfeed.getAllStyles().setBackgroundGradientEndColor(Style.BACKGROUND_GRADIENT_LINEAR_VERTICAL);
        mainUI.addTab("Ozi", MATERIAL_RSS_FEED, 5f, newsfeed);
//        mainUI.addTab("", MATERIAL_WEB, 5f, rssContainer());        
        FloatingActionButton fab
                = FloatingActionButton.createFAB(MATERIAL_IMPORT_CONTACTS);
        ////Container friends = fab.bindFabToContainer(new FriendsContainer());
        //fab.addActionListener(e -> uploadContacts());
        mainUI.addTab("Providers", MATERIAL_STORE_MALL_DIRECTORY, 5f,
                providerfeed);
        mainUI.addTab("Contacts", MATERIAL_PEOPLE_OUTLINE, 5f,
              contactfeed);
        //mainUI.addTab("", MATERIAL_NOTIFICATIONS_NONE, 
        //        5f, notifications);
        mainUI.addTab("Me", MATERIAL_PERSON_OUTLINE, 5f,
                new MoreContainer());
        mainUI.setUIID("TabZ");
        add(CENTER, mainUI);
        Image sosImage;
        try {
            sosImage = Image.createImage("/ziem_sos.png").scaled(96,96);
        } catch (IOException ex) {
            sosImage=Image.createImage(10, 10);
        }
        Image ziemImage;
        try {
            ziemImage = Image.createImage("/ziem_logo.png").scaled(96,96);
        } catch (IOException ex) {
            ziemImage=Image.createImage(96, 96);
        }

        getToolbar().addCommandToLeftBar("", sosImage,e -> sos());
        getToolbar().addCommandToRightBar("", ziemImage,e -> new ChooseService(new Category()).show());
        //getToolbar().addMaterialCommandToLeftBar("",
        //        MATERIAL_WARNING, 4, e -> sos());
        //getToolbar().addMaterialCommandToRightBar("",
        //        MATERIAL_CHAT, 4, e -> new ChooseService(new Category()).show());
        Button searchButton = new Button("Search", "TitleSearch");
       // setMaterialIcon(searchButton, MATERIAL_SEARCH);
        searchButton.setMaterialIcon(MATERIAL_SEARCH);
        getToolbar().setTitleComponent(searchButton);
        //searchButton.addActionListener(e -> new SearchForm().show());
        searchButton.addActionListener(e -> search());
    }

    public void search() {
        //this will handle search. Such that when we we click on search
        //it will determine that we are in search and go to the appropriate search form.
        ////////Log.p("Selected " + mainUI.getSelectedComponent());
        if ((mainUI.getSelectedComponent() == contacts) || (mainUI.getSelectedComponent() == contactfeed)) {
            ////////Log.p("Searching Contacts");
            new SearchContacts().show();
        } else if ((mainUI.getSelectedComponent() == providerfeed)) {
            ////////Log.p("Searching CProviders");
            new SearchProviders().show();
        }else
        {
            ////////Log.p("Searching Requests");
            SearchForm f = new SearchForm();
            f.show();
        }
    }
   public void report() {
        //this will handle search. Such that when we we click on search
        //it will determine that we are in search and go to the appropriate search form.
        ////////Log.p("Selected " + mainUI.getSelectedComponent());
        if ((mainUI.getSelectedComponent() == contacts) || (mainUI.getSelectedComponent() == contactfeed)) {
            ////////Log.p("Searching Contacts");
            new SearchContacts().show();
        } else if ((mainUI.getSelectedComponent() == providerfeed)) {
            ////////Log.p("Searching CProviders");
            //create 
            new SearchProviders().show();
        }else
        {
            ////////Log.p("Searching Requests");
            SearchForm f = new SearchForm();
            f.show();
        }
    }

    public void refresh() {
        if (mainUI.getSelectedComponent() == newsfeed && !isMinimized()) {
            shouldRefresh(e -> newsfeed.refresh());
        } else {
            newsfeed.refresh();
        }
    }

    private void sos() {
        ToastBar.showInfoMessage("Sending SOS message ..");
        //Service alive = ServerAPI.getAmFineService();
        Service alive = ServerAPI.getSosService();
        if (alive == null) {
            ToastBar.showErrorMessage("SOS service not yet defined");
        } else {
            Request aliveRequest = new Request();
            aliveRequest.service.add(alive);
            aliveRequest.createRequest();
            aliveRequest.save();
            SearchForm s = new SearchForm();
            s.searchField.setText(ServerAPI.me().fullName());
            s.ic.refresh();
            s.show();
        }
    }

    private void shouldRefresh(ActionListener l) {
        ToastBar.showMessage("Updates available. Click here to refresh...",
                MATERIAL_UPDATE, l);
    }
/*
    private void uploadContacts() {
        startThread(() -> {
            Contact[] cnt = Display.getInstance().
                    getAllContacts(true, true, false, true, true, false);
            ServerAPI.uploadContacts(cnt);
        }, "ContactUploader").start();
    }
*/
    private Container rssContainer() {
        Container rss = new Container(new BorderLayout());

        RSSReader rr = new RSSReader();
        rr.setURL("https://www-ziemozi-a3ef.restdb.io/feed.rss");
        rss.addComponent(BorderLayout.CENTER, rr);
        return rss;
    }
}
