package com.ziemozi.forms;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.MultiButton;
import com.codename1.components.SpanButton;
import com.codename1.components.ToastBar;
import com.codename1.components.xmlview.DefaultXMLViewKit;
import com.codename1.components.xmlview.XMLView;
import com.codename1.io.Log;
import com.ixzdore.restdb.ziemobject.Comment;
import com.ixzdore.restdb.ziemobject.Post;
import com.ziemozi.server.ServerAPI;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.URLImage;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Border;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.User;
import com.ixzdore.restdb.ziemview.ZiemView;
import com.ixzdore.restdb.ziemview.ZiemViewTab;
import java.util.List;

public class CommentsForm extends Form {
    private final TextField commentField = new TextField();
    private final Container comments = new Container(BoxLayout.y());
    private String replyCommentId;
    private final Component commentEditor;
    private final Form previous;
    public CommentsForm(Request p, Request replyingTo) {
        super(p.title(), new BorderLayout());
        //we need to give requests title so we can refere to it
        commentField.getAllStyles().setBorder(Border.createEmpty());
        Button send = new Button("", "Label");
        send.setText("Click here to respond or comment");
        //we create a default comment request object
        //for this type of request. Each request will have 
        //a default comment_service_type
        //we can therefore just create a comment 
        //from the comment service definition
        //so we actually just edit the comment service
                //////////Log.p("\n\n" +
//                "Request on \nn" +
  //              p.getPropertyIndex().toJSON());
        //////////Log.p("\n\n" +
    //            "Commenting on \nn" +
      //          replyingTo.getPropertyIndex().toJSON());
        ZiemViewTab zv = new ZiemViewTab();
        replyingTo.service.get(0).refresh();
        commentEditor = zv.createRequestView(replyingTo.
                service.get(0).comment_services.asList());
         zv.setRequestParent(replyingTo);       
        //Container post = BorderLayout.centerEastWest(commentEditor, send, 
        //    null);
       // setMaterialIcon(send, MATERIAL_SEND);
        send.setMaterialIcon(MATERIAL_COMMENT);
        //replyCommentId = replyingTo == null ? null : replyingTo._id.get();
        //
       send.addActionListener(e -> postComment());
        //commentField.setDoneListener(e -> postComment(p));
       
        previous = getCurrentForm();
        getToolbar().addMaterialCommandToLeftBar("", 
            MATERIAL_ARROW_BACK, e -> previous.showBack());
        addComment(p);
       for(Request cmt : p.comments.asList()) {
           addComment(cmt);
       }
       // add(SOUTH, commentEditor);
        add(SOUTH,send);
        add(CENTER, comments);
    }

    private void postComment() {
        //InfiniteProgress ip = new InfiniteProgress();
        //Dialog ipd = new InfiniteProgress().showInfiniteBlocking();
        Command[] arg = null;
    Dialog dlg = new Dialog("Comment");
    dlg.setLayout(new BorderLayout());
    dlg.add(CENTER,commentEditor);
    Button b = new Button("Cancel");
      b.addActionListener(e -> {
          previous.showBack();
          //doneComment(dlg);
          //dlg.dispose();
      });
    dlg.setDisposeWhenPointerOutOfBounds(true);
    dlg.add(NORTH,b);
    dlg.show(0, 0, 0, 0);
        //Dialog.show("Comment", commentEditor, arg);
        //p.save();
        //ipd.dispose();
        //addComment(p);
        animateLayout(150);
    }
    
    private void postComment(Request p) {

                Command[] arg = null;
        Dialog.show("Comment", commentEditor, arg);
        InfiniteProgress ip = new InfiniteProgress();
        Dialog ipd = new InfiniteProgress().showInfiniteBlocking();        
        p.save();
        ipd.dispose();
        addComment(p);
        animateLayout(150);
    }
    
    private void addComment(Request cm) {
        Component c = createComment(cm);
        if(cm._parent_id.get() != null) {
            Component parent = findParentComment(cm._parent_id.get());
            if(parent != null) {
                Container chld = (Container)parent.
                    getClientProperty("child");
                if(chld == null) {
                    chld = BoxLayout.encloseY(c);
                    chld.getAllStyles().setPaddingLeft(convertToPixels(5));
                    parent.putClientProperty("child", chld);
                    int pos = comments.getComponentIndex(parent);
                    comments.addComponent(pos + 1, chld);
                } else {
                    chld.add(c);
                }
            } else {
                comments.add(c);
            }
        } else {
            comments.add(c);
        }
    }
    
    private Component createComment(Request cm) {
        cm.refreshService();
        cm.refreshUser();
        MultiButton cb = new MultiButton(cm.service.get(0).description.get());
        SpanButton sb = new SpanButton();
        ////////Log.p("Comment type " + cm.service.get(0).description.get());
        //////Log.p(cm.getPropertyIndex().toString());
        //////Log.p("Comment user is " + cm.ziemozi_user.get(0).getPropertyIndex().toString());

        List<User> u = cm.ziemozi_user.asList();
        //Log.p("comment users " + u.size());
        for (User x:u){
            //////Log.p(x.getPropertyIndex().toString());
            Image icon = getAvatarImage(x.avatar.get());
            cb.setIcon(icon);
            sb.setIcon(icon);
        }
        if(u.size() < 1){
            cb.setIcon(getAvatarImage(null));
            sb.setIcon(getAvatarImage(null));
        }
        cb.setTextLine1(cm.service.get(0).name.get());
        String s = cm.plain_summary();
        cb.setTextLine2(s);
        sb.setText(s);
        //////Log.p("Plain Summary from " + cm.summary.get() + " is " + s);
        /*
        if (s.length() > 80 ) {
            cb.setTextLine2(s.substring(0,80));
            cb.setTextLine3(s.substring(81));
        }else {
            cb.setTextLine2(s);
            //cb.setTextLine3(s.substring(s.length()/2));
         
        }
         */
        //TextArea c = new TextArea(cm.plain_summary());
        cb.addActionListener(e -> new PostForm(cm).show());
        sb.addActionListener(e -> new PostForm(cm).show());
        //XMLView c = new XMLView();
        //new DefaultXMLViewKit().install(c); 
        //c.setXML(cm.summary.get());
        //c.setEditable(false);
        //c.setFocusable(false);
        /*
        Label avatar = new Label();
        cb.setUIID("SmallLabel");
        if (u.size() > 0 ){
        if (u.size() < 2 ) {
            avatar = new Label(getAvatarImage(cm.ziemozi_user.get(0).avatar.get()));
        }else{
            avatar = new Label(getAvatarImage(cm.ziemozi_user.get(1).avatar.get()));           
        }}


        Container content = BorderLayout.centerEastWest(cb, null, avatar);
        */
        Container content=BorderLayout.centerEastWest(cb,null,null);
        //Container content=BorderLayout.centerEastWest(sb,null,null);
        if(cm._id.get() != null && cm._parent_id.get() == null) {
            Button reply = new Button("reply", "SmallBlueLabel");
            content.add(SOUTH, FlowLayout.encloseRight(reply));
            reply.addActionListener(e -> replyCommentId = cm._id.get());
        }
        content.putClientProperty("comment", cm);
        return content;
    }
    
    private Component findParentComment(String id) {
        for(Component cmp : comments) {
            Request c = (Request)cmp.getClientProperty("comment");
            if(c != null && id.equals(c._id.get())) {
                return cmp;
            } 
        }
        return null;
    }
    
    private Image getAvatarImage(String userId) {
        int size = convertToPixels(5);
        //////////Log.p("Avatar " + userId);
        if (userId == null || userId.isEmpty()) {
            userId="https://img.icons8.com/color/48/000000/human-head.png";
        }
        return URLImage.createCachedImage(userId , 
            userId, 
            Image.createImage(size, size), 
            URLImage.FLAG_RESIZE_SCALE_TO_FILL);
    } 

    private void doneComment(Dialog d) {
        //finished with the comment
        d.dispose();
        this.show();
       }
}
