package com.ziemozi.forms;

import com.codename1.components.MediaPlayer;
import com.codename1.components.MultiButton;
import com.codename1.components.ScaleImageButton;
import com.codename1.components.SpanLabel;
import com.codename1.components.xmlview.DefaultXMLViewKit;
import com.codename1.components.xmlview.XMLView;
import com.ziemozi.components.RichTextView;
import com.ixzdore.restdb.ziemobject.Post;
import com.ixzdore.restdb.ziemobject.User_bak;
import com.ziemozi.server.ServerAPI;
import com.codename1.io.Log;
import com.codename1.media.Media;
import com.codename1.media.MediaManager;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.*;
import com.codename1.ui.CheckBox;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.CENTER;
import static com.codename1.ui.Component.LEFT;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import static com.codename1.ui.FontImage.MATERIAL_CLOSE;
import static com.codename1.ui.FontImage.MATERIAL_SEARCH;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.InfiniteContainer;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.Toolbar;
import com.codename1.ui.URLImage;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.util.UITimer;
import com.codename1.util.StringUtil;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.ServiceContact;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.local.localAPI;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ContactsfeedContainer extends InfiniteContainer {
    ArrayList<ServiceContact> contacts = new ArrayList<ServiceContact>();
    @Override
        public Component[] fetchComponents(int index, int amount) {
            Log.p("Index " + index);
             List<Component> response = new ArrayList<>();
        if(index == 0) {
            response.add(createWelcomeBar());
            response.add(UIUtils.createSpace());                
                //we are just starting so load it all up
                contacts = localAPI.searchContacts(
                        "", 0, amount + 9999);
                Log.p("Contacts " + contacts.size());
 
            }
            if ((contacts == null) || (contacts.size() < 1)) {
                return null;
            }            
            if ( index + amount > contacts.size()) {
                amount = contacts.size() - index;
                if ( amount <=0) return null;
            }

            for (int iter =0; iter < amount; iter++) { 
             int offset = index+iter;
             response.add(createEntry(contacts.get(offset)));
            }
            if (response.isEmpty()) {
                return null;
            }
            return UIUtils.toArray(response);
        
        }

    private Component createEntry(ServiceContact p) {
        if (p != null) {
            ////////Log.p("Service Contact name " + p.name.get());
            //if (p.services.size() > 0) {
                ////////Log.p("Creating Entry for " + p.summary());
                p.refresh();
                
                MultiButton mb = new MultiButton(p.name.get());
                mb.setUIID("SmallLabel");
                //String summary = p.summary();    
                mb.setTextLine1(p.name.get());
                mb.setTextLine2(p.fullAddress());
                mb.setTextLine3(p.extendedDescription());
                //////////Log.p(p.plain_summary());
                //if ( )

                mb.setIcon(p.getAvatar(8));
                mb.addActionListener(e -> new ContactForm(p).show());
                return mb;

        } else {
            return new MultiButton("-");
        }
    }
       private static Container createWelcomeBar() {
        //Button avatar = new Button(ServerAPI.me().getAvatar(6.5f), "Label");
        TextArea welcome = new TextArea();
        String w=" ****Pull To Refresh Service Contacts.*** ";
        welcome.setText(w);
        welcome.setEditable(false);
        //welcome.setUIID("SmallBlueLabel");
        Button map = new Button("Pull To Refresh List of Service Contacts");
               //map.addActionListener(e -> new MapShowForm(localAPI.newsfeed(1, 100)).show());
        Container c = BorderLayout.center(map);
        //c.add(SOUTH, map);
        c.setUIID("HalfPaddedContainer");
        
        return c;
    }
}
