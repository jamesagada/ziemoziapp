package com.ziemozi.forms;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.ShareButton;
import com.codename1.components.SpanLabel;
import com.codename1.components.xmlview.DefaultXMLViewKit;
import com.codename1.components.xmlview.XMLView;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.EAST;
import static com.codename1.ui.CN.WEST;
import com.ixzdore.restdb.ziemobject.Post;
import static com.codename1.ui.CN.getCurrentForm;
import com.codename1.ui.CheckBox;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.util.StringUtil;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceContact;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.ServerAPI;
import com.ziemozi.server.local.localAPI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ProviderForm extends Form {
    
    public ProviderForm(Provider p) {
        super(p.name.get(), BoxLayout.y());               
        add(createContactItem( p));
        Form previous = getCurrentForm();
        getToolbar().addMaterialCommandToLeftBar("", 
            MATERIAL_ARROW_BACK, e -> previous.showBack());
    }

    private Container createContactItem(Provider p) {
        //////////log.p("User " + u.fullName());
        //////////log.p("User Json " + u.getPropertyIndex().toString());
        Container titleArea = createTitle(p);
        Component body;
        String style = null;
        //if(p.styling.get() != null && !p.styling.get().equals("Label")) {
        //    style = p.styling.get();
        //}
        String summary = p.summary()+"";
 
        if(summary.indexOf('<') > -1) {
            if(style != null) {
                //body = new RichTextView(p.summary.get(), "PostStyleText");

                body = new XMLView();
                new DefaultXMLViewKit().install((XMLView)body);                
                ((XMLView)body).setXML(summary);
                //((RichTextView)body).setAlignment(CENTER);
                body.setUIID(style);
            } else {
                //body = new RichTextView(p.summary.get());
                body = new XMLView();
                new DefaultXMLViewKit().install((XMLView)body);            
                ((XMLView)body).setXML(summary);
                body.setUIID("HalfPaddedContainer");
            }
        } else {
            body = new SpanLabel(summary);
            if(style != null) {
                ((SpanLabel)body).setTextUIID("PostStyleText");
                body.setUIID(style);
            } else {
                body.setUIID("HalfPaddedContainer");
            }
        }
        //set an action for body so it get opened into 
        //a form.
        //p.refreshService();
        //body.addPointerPressedListener(e -> new PostForm(p).show());
        
        CheckBox like = CheckBox.createToggle("Rate");
        like.setUIID("CleanButton");
        Button comment = new Button("Report", "CleanButton");
        ShareButton share = new ShareButton();
        share.setTextToShare(p.summary());
        share.setText("Share");
        FontImage.setMaterialIcon(like, FontImage.MATERIAL_THUMB_UP);
        FontImage.setMaterialIcon(comment, 
                FontImage.MATERIAL_COMMENT);
        FontImage.setMaterialIcon(share, FontImage.MATERIAL_SHARE);
        
        Container buttonBar = GridLayout.encloseIn(3, like, comment, share);
        buttonBar.setUIID("HalfPaddedContainer");
   
        like.addActionListener(e -> rateProvider(p));
        //if we click on this, it should actually show the list of 
        //posts for this service contact. Best thing is to go to the search request window
        //and show the requests for this service contact which will be the 
      comment.addActionListener(e -> requestService(p));
      
        buttonBar.revalidate();
        Container ret = BoxLayout.encloseY(
                titleArea, body, createPostStats(p), buttonBar);
        ret.revalidate();
        return ret;
    }
     private static Container createTitle(Provider p) {
        //////////log.p("\n\n Request \n" + p.getPropertyIndex().toString());
        //////////log.p(u.getPropertyIndex().toString());
        //////////log.p(u.fullName());
        p.refresh();
        Button avatar = new Button("", p.getAvatar(7), "CleanButton");
        Button name = new Button(p.name.get(), "PostTitle");
        SimpleDateFormat sdf = null;
        Date d = new Date(); 
        try {
            String s = p._created.get();
            String sd = s.substring(0, s.indexOf("T"));
            //2018-07-27T11:49:47.014Z
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");        
            ////////log.p("\n\n" + sd + "\n\n");
            d = sdf.parse(s);
        } catch (ParseException ex) {
            //we will figure this out later.          
        }
        Button postTime = new Button(UIUtils.formatTimeAgo(d.getTime()), 
                "PostSubTitle");
        Button menu = new Button("", "Label");
       menu.addActionListener(e -> new ProviderForm(p).show());
        FontImage.setMaterialIcon(menu, 
                FontImage.MATERIAL_MORE_HORIZ);
        Container titleArea = BorderLayout.centerEastWest(
                FlowLayout.encloseMiddle(BoxLayout.encloseY(name, postTime)), 
                FlowLayout.encloseIn(menu), avatar);
        titleArea.setUIID("HalfPaddedContainer");
        return titleArea;
    }
    private static Container createPostStats(Provider p) {
        ////////log.p("Creating Stats for " + p.getPropertyIndex().toString());
        Container stats = new Container(new BorderLayout(), 
                "PaddedContainer");
        /*if(p.likes.get() != null  ) {
            Label thumbUp = new Label("", "SmallBlueCircle");
            FontImage.setMaterialIcon(thumbUp, 
                    FontImage.MATERIAL_THUMB_UP);
            Label count = new Label("" + p.likes.get(), "SmallLabel");
            stats.add(WEST, BoxLayout.encloseX(thumbUp, count));
        }
        */
       // p.refreshComments();

        //if (p.comments.get() != null){
          //  stats.add(EAST, new Label(p.comments.get() + " comments", 
            //        "SmallLabel"));
        //}

        return stats;        
    }

    private void showServiceCommentsForm(ServiceContact p) {
        //call the searchField for these services and possibly providers
        //Or create a special form for that
     }

private void requestService(Provider p) {
        //determine the services
        p.refresh();
                           InfiniteProgress ip = new InfiniteProgress();
                    Dialog ipd = ip.showInfiniteBlocking();
                    ArrayList<Service> s = new ArrayList<Service>();
                    s.addAll(p.services.asList());
                    ArrayList<Category> c = new ArrayList<Category>();
                    c.addAll(p.category.asList());
                    if (!s.isEmpty() ){
                    ipd.dispose();
                    ip.remove();
                    ZiemForm z = new ZiemForm(s, "To " + p.name.get());
                    //z.setTitle(b.getText() + "Report/Request");

                    z.show();
                    } else if (!c.isEmpty()){
                                            ipd.dispose();
                    ip.remove();
                        new ChooseService(c).show();
                    }
                   
       }

    private void rateProvider(Provider p) {
        //find the rating service
        // and then open it
        ArrayList<Service> s = localAPI.getRatingService();
        if (!(s == null ) && (!s.isEmpty())){
             ZiemForm z = new ZiemForm(s, "Rate " + p.name.get()); 
             z.show();
        }
     }

}
