package com.ziemozi.forms;

import com.codename1.components.MultiButton;
import com.codename1.components.ShareButton;
import com.codename1.components.ToastBar;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.SOUTH;
import com.codename1.ui.Component;
import com.ziemozi.server.ServerAPI;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.RoundBorder;
import com.codename1.ui.plaf.Style;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.Service;

public class ContactsContainer extends Container {
    String mename ="";
    MultiButton me = new MultiButton(ServerAPI.me().fullName());
    public ContactsContainer() {
        super(BoxLayout.y());
        //super(GridLayout.autoFit());
        //setLayout(new GridLayout(2));
        setUIID("HalfPaddedContainer");
        setScrollableY(true);
        mename = ""+ServerAPI.me().fullName();
        me.setTextLine1(mename);
        //we have to redo the PropertyBusinessObjectUI for this to work
        me.addActionListener(e -> new SettingsForm().show());
        me.setTextLine2("View your profile");
        me.setIcon(ServerAPI.me().getAvatar(9f));
        me.setIconUIID("PaddedContainer");
        me.setUIID("Container");
        me.getAllStyles().setBorder(Border.createUnderlineBorder(2, 0xcccccc));
        me.setUIIDLine1("MultiLine1WithMargin");
        me.setLinesTogetherMode(true);
        add(me);

        add(new Label("Shortcuts", "FriendSubtitle"));
        Container c = new Container();
        c.setLayout(BoxLayout.y());
        c.add(                createButton("Invite Friends", 
                        FontImage.MATERIAL_PEOPLE, 0x000000));
        c.add(                createButton("Log Out", 
                        FontImage.MATERIAL_EXIT_TO_APP, 0xF35369));
        c.add(                createButton("Service Directory", 
                        FontImage.MATERIAL_PHOTO_ALBUM, 0x54C7EC));
        c.add(                createButton("I am fine", FontImage.MATERIAL_THUMB_UP, 
                        0x000000));
        c.add(                createButton("My reports and requests", 
                        FontImage.MATERIAL_REPORT, 0x000000));
        c.add(                createButton("Around me", 
                        FontImage.MATERIAL_MY_LOCATION, 0x000000));
        c.add(                createButton("S.O.S", 
                        FontImage.MATERIAL_WARNING, 0x000000));
        /*
        c.addAll(
                createButton("Invite Friends", 
                        FontImage.MATERIAL_PEOPLE, 0x000000),
                createButton("Log Out", 
                        FontImage.MATERIAL_EXIT_TO_APP, 0xF35369),                
                createButton("Service Directory", 
                        FontImage.MATERIAL_PHOTO_ALBUM, 0x54C7EC),
                createButton("I am fine", FontImage.MATERIAL_THUMB_UP, 
                        0x000000),
                createButton("My reports and requests", 
                        FontImage.MATERIAL_REPORT, 0x000000),
                createButton("Around me", 
                        FontImage.MATERIAL_MY_LOCATION, 0x000000),
                createButton("S.O.S", 
                        FontImage.MATERIAL_WARNING, 0x000000)); 
        */
        add(c);
    }
    
    private Component createButton(String title, char icon, int color) {
        Button b = new Button(title);
        //b.setUIID("Container");
        b.setIcon(FontImage.createMaterial(icon, "LargeCircleIcon", 5f));
        //b.setIconUIID("LargeCircleIcon");
        //b.getIconComponent().getAllStyles().setBorder(RoundBorder.create().
        //        color(color));
        //b.setTextPosition(Label.BOTTOM);
        b.setUIID("SmallLabel");
        if (title.indexOf("Invite") >= 0) {
            ShareButton share;
            share = new ShareButton();
            share.setText("Invite your friend to Ziemozi");
            share.setTextToShare("Have you used Ziemozi yet?");
            return share; 
        }        
        if (title.indexOf("Log") >= 0) {
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {    
                    ServerAPI.logout();
                    me.setTextLine1(null);
                    new LoginForm().show();
                }
            });
        }
        if (title.indexOf("Around me") >= 0) {
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    SearchForm s = new SearchForm();
                    s.searchField.setText(ServerAPI.me().location());
                    s.geoSearch = true;
                    s.ic.refresh();
                    s.show();
                  }
            });
        }        
        if (title.indexOf("reports and requests") >= 0) {
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    SearchForm s = new SearchForm();
                    s.searchField.setText(ServerAPI.me().fullName());
                    s.ic.refresh();
                    s.show();
                  }
            });
        }
        if (title.indexOf("S.O.S") >= 0){
           b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    Service sos = ServerAPI.getSosService();
                    if (sos == null ) {
                        ToastBar.showErrorMessage("SOS Service not yet defined");
                     }else {
                        Request sosRequest = new Request();
                        sosRequest.service.add(sos);
                        sosRequest.createRequest();
                        sosRequest.save();
                        ToastBar.showInfoMessage("SOS Message Sent");
                    }
                  }
            });           
        }
        if (title.indexOf("fine") >= 0){
           b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    Service alive = ServerAPI.getAmFineService();
                    if (alive == null ) {
                        ToastBar.showErrorMessage("I am fine service not yet defined");
                     }else {
                        Request aliveRequest = new Request();
                        aliveRequest.service.add(alive);
                        aliveRequest.createRequest();
                        aliveRequest.save();
                    }
                  }
            });           
        }     
        if (title.indexOf("Service") >= 0){
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    SearchContacts s = new SearchContacts();
                    s.searchField.setText("from");
                    s.geoSearch = false;
                    s.ic.refresh();
                    s.show();
                  }
            });
        } 
        return b;
    }
}
