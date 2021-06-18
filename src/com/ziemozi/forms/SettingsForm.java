package com.ziemozi.forms;

import com.codename1.components.ToastBar;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Storage;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.ServerAPI;
import com.codename1.properties.InstantUI;
import com.codename1.properties.UiBinding;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import static com.codename1.ui.FontImage.*;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Toolbar;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.layouts.LayeredLayout;
import static com.codename1.ui.util.ImageIO.FORMAT_PNG;
import com.ixzdore.restdb.ziemview.PropertyBusinessObjectUI;
import com.ixzdore.restdb.ziemview.ZiemView;
import com.ziemozi.server.local.localAPI;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
//import java.util.logging.Level;
//import java.util.logging.Logger;

public class SettingsForm extends Form {
    private final Label cover = new Label(" ", "LoginTitle");
    private final Label avatar = new Label("", "LabelFrame");
    private final Button changeCover =
        new Button(MATERIAL_CAMERA_ALT, "CameraLabel");
    private final Button changeAvatar =
        new Button(MATERIAL_CAMERA_ALT, "CameraLabel");
          User me = localAPI.me();  
    public SettingsForm() {
        super("", BoxLayout.y());
        Form previous = getCurrentForm();
        getToolbar().setBackCommand("Back", e -> previous.showBack());

        avatar.setIcon(me.getAvatar(12));
        if(me.cover.get() != null) {
            me.fetchCoverImage(i -> {
                cover.getAllStyles().setBgImage(i);
                repaint();
            });
        }
        changeAvatar.addActionListener(e -> pickAvatar(avatar));
        changeCover.addActionListener(e -> pickCover(cover));
        Container coverContainer = LayeredLayout.encloseIn(
            cover, 
            FlowLayout.encloseRightBottom(changeCover)
        );
        coverContainer.setUIID("SettingsMargin");
        Container avatarContainer = LayeredLayout.encloseIn(
            avatar, 
            FlowLayout.encloseRightBottom(changeAvatar)
        );
        add(LayeredLayout.encloseIn(
            coverContainer,
            FlowLayout.encloseCenterBottom(avatarContainer)
        ));
        add(new Label(me.fullName(), "CenterLargeThinLabel"));
        add(createButtonBar());
    }

    private Container createButtonBar() {
        Button activity = new Button(MATERIAL_HISTORY, "CleanButton");
        Button settings = new Button(MATERIAL_SETTINGS, "CleanButton");
        Button viewAs = new Button(MATERIAL_ACCOUNT_CIRCLE, "CleanButton");
        Button more = new Button(MATERIAL_MORE_HORIZ, "CleanButton");
        settings.addActionListener(e -> 
            showUserEditForm(ServerAPI.me()));
        return GridLayout.encloseIn(4, activity, settings, viewAs, more);
    }
    
    private void pickAvatar(Label l) {
                    Display.getInstance().openGallery(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        String s = (String) evt.getSource();
                        me.avatar.set(s);
                        ServerAPI.update(me);
            me.getPropertyIndex().storeJSON("me.json"); 
                        ServerAPI.refreshMe();
                        l.setIcon(me.getAvatar(9f));
                        l.repaint();
                    }
                }, Display.GALLERY_IMAGE);
    }                
    
  private void pickCover(Label l) {
                    Display.getInstance().openGallery(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        String s = (String) evt.getSource();
                        me.cover.set(s);
                        ServerAPI.update(me);
                        me.fetchCoverImage(i -> {
                                cover.getAllStyles().setBgImage(i);
                                repaint();
                         });
                    }}, Display.GALLERY_IMAGE);
    } 

    //
    @Override
    protected void initGlobalToolbar() {
        Toolbar tb = new Toolbar(true);
        tb.setUIID("Container");
        setToolbar(tb);
    }

    private void showUserEditForm(User me) {
        //it may be better to use ziemview to edit the user
        //we will have to create the user view template which we can create on the server
        //that way we can always update without code. OR
        //it is possible to generate ziem view from a given propertybusinessobject. 
        //use instantui
        /**
        InstantUI iu = new InstantUI();
        iu.excludeProperties(me.authtoken, me.avatar, me.cover,
            me.friendRequests, me.friends, me.peopleYouMayKnow, me._id,
            me.password, me.birthday,me.groups,me.requests,me.subscriptions,me.home_latitude,me.home_longitude);
        iu.setMultiChoiceLabels(me.gender, "Male", "Female", "Other"); 
        iu.setMultiChoiceValues(me.gender, "Male", "Female", "Other");
        Container cnt = iu.createEditUI(me, true);
        cnt.setUIID("PaddedContainer");
        cnt.setScrollableY(true);
        */ 
        //////////Log.p("Name of " + me.getPropertyIndex().getName());
        me.refreshGroups();
        //////Log.p(me.getPropertyIndex().toString());
        PropertyBusinessObjectUI po = new PropertyBusinessObjectUI();
        po.excludeProperty(me._id);
        po.excludeProperty(me.requests);
        po.excludeProperty(me.password);
        po.excludeProperty(me.authtoken);
        po.excludeProperty(me.friends);
        po.excludeProperty(me.friendRequests);
        //po.excludeProperty(me.groups);
        po.excludeProperty(me.peopleYouMayKnow);
        po.excludeProperty(me.active);
        po.excludeProperty(me.avatar);
        po.excludeProperty(me.cover);
        me.avatar.putClientProperty("ziem-type", "Image");
        me.gender.putClientProperty("ziem-type", "SingleSelectList");
        HashMap m = new HashMap();
        m.put("option_list", "'Male','Female','Other'");
        me.gender.putClientProperty("ziem-property-options", m);
        //HashMap h = (HashMap)me.gender.getClientProperty("ziem-property-options");
        //////////Log.p("Client property options" + h.get("option_list"));
        //////////Log.p("Going to edit " + me.getPropertyIndex().getName());
        Container cnt = po.createEditUI(me, false);
        Form edit = new Form("Edit", new BorderLayout());
        edit.add(BorderLayout.CENTER, cnt);

       
        //we need to add the other profile sides
        //like home
        edit.getToolbar().setBackCommand("Back", e -> {
            //ask if the object should be saved or cancelled?
            Boolean update = Dialog.show("Update Profile","","Yes","No");
            //showBack();
            if (update) {
                po.updatePropertyBusinessObject(cnt,me);
                callSerially(() -> ServerAPI.update(me));
            }
            showBack();
        });
        edit.show();
    }

 
}
