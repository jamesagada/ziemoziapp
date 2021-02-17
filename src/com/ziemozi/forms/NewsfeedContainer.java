package com.ziemozi.forms;

import com.codename1.components.MediaPlayer;
import com.codename1.components.ScaleImageButton;
import com.codename1.components.ShareButton;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.components.xmlview.DefaultXMLViewKit;
import com.codename1.components.xmlview.XMLView;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.RoundBorder;
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
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.InfiniteContainer;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.URLImage;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.util.StringUtil;
import com.codename1.util.regex.StringReader;
import com.codename1.xml.Element;
import com.codename1.xml.XMLParser;
import com.codename1.xml.XMLWriter;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.local.localAPI;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewsfeedContainer extends InfiniteContainer {
        List<Request> response = localAPI.newsfeed(0, 9999, false);
    private static String checkedXml(String xmls) {
        StringReader r = new StringReader(xmls);
        XMLParser parser = new XMLParser();
        Element elem = parser.parse(r);
        String rs = xmls;
        if (elem.isEmpty()) {
            rs = "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n";
            rs = rs + "<doc>\n" + "<body>";        //this.refreshService();
            rs = rs + "<p uiid=\"headline\">  Sorry !!!  </p>";
            rs = rs + "<p> We are unable to display this message. </p>";
            rs = rs + " </body></doc>";

        } else {
            XMLWriter xmlw = new XMLWriter(false);
            rs = xmlw.toXML(elem);
            rs = StringUtil.replaceAll(rs, "<p />", "");
        }
        //////log.p("Checked XML \n" + rs);
        return rs;
    }

    @Override
    public Component[] fetchComponents(int index, int amount) {
        // ////////log.p("\n\nindex is " + index);
        // ////////log.p("\n\namount is " + amount);
        ArrayList<Component> components = new ArrayList<>();
         response = localAPI.newsfeed(0, amount + 9999, false);
        if (index == 0) {
            components.add(createWelcomeBar());
            components.add(UIUtils.createSpace());
        }

        if ((response == null) || response.isEmpty()) {
            ////////log.p("No Requests to show \n");
            if (index == 0) {
                return UIUtils.toArray(components);
            }
            return null;
        }
        if (index + amount > response.size()) {
            amount = response.size() - index;
            if (amount <= 0) {
                return null;
            }
        }

        for (int iter = 0; iter < amount; iter++) {
            int offset = index + iter;
            if (offset < response.size()) {
                Request p = response.get(offset);
                p.refreshUser();
                if (p.ziemozi_user.size() > 0) {
                    ////////log.p("users attached to response " + p.ziemozi_user.size());
                    int lastuser = p.ziemozi_user.size() - 1; // this is a hack
                    //lastuser = 0;
                    ////////log.p("User From Request " + p.ziemozi_user.get(lastuser).getPropertyIndex().toString());
                    User pu = p.ziemozi_user.get(lastuser);
                    components.add(createSimpleNewsItem(pu, p));
                    components.add(UIUtils.createHalfSpace());
                }
            }
        }
        return UIUtils.toArray(components);
    }

    private static Container createNewsTitle(User u, Request p) {
        //////////log.p("\n\n Request \n" + p.getPropertyIndex().toString());
        //////////log.p(u.getPropertyIndex().toString());
        //////////log.p(u.fullName());
        Log.p("Bfore Refreshing " + p.service.get(0).name.get());
        //p.refreshService();
        Log.p(p.service.get(0).name.get());
        String ds = p.service.get(0).description.get();
        Button avatar = new Button("", u.getAvatar(6.5f), "CleanButton");
        Button name = new Button(u.fullName(), "PostTitle");
        Button desc = new Button(ds,"PostSubTitle");
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
        menu.addActionListener(e -> new PostForm(p).show());
        FontImage.setMaterialIcon(menu,
                FontImage.MATERIAL_MORE_HORIZ);
        Container titleArea = BorderLayout.centerEastWest(
                FlowLayout.encloseMiddle(BoxLayout.encloseY(name,desc, postTime)),
                FlowLayout.encloseIn(menu), avatar);
        titleArea.setUIID("HalfPaddedContainer");
        return titleArea;
    }

    /**
     *
     * @param u
     * @param p
     * @return
     */
    private static Container createSimpleNewsTitle(User u, Request p) {
        //////////log.p("\n\n Request \n" + p.getPropertyIndex().toString());
        //////////log.p(u.getPropertyIndex().toString());
        //////////log.p(u.fullName());
        p.refreshService();
        Button avatar = new Button("", u.getAvatar(6.5f), "CleanButton");
        Button name = new Button(u.fullName(), "PostTitle");
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
        //menu.addActionListener(e -> new PostForm(p).show());
        FontImage.setMaterialIcon(menu,
                FontImage.MATERIAL_MORE_HORIZ);
        Container titleArea = BorderLayout.centerEastWest(
                FlowLayout.encloseMiddle(BoxLayout.encloseY(name, postTime)),
                FlowLayout.encloseIn(menu), avatar);
        titleArea.setUIID("HalfPaddedContainer");
        return titleArea;
    }

    public static Container createNewsItem(User u, Request p) {
        //////////log.p("User " + u.fullName());
        //////////log.p("User Json " + u.getPropertyIndex().toString());
        Container titleArea = createNewsTitle(u, p);
        Component body;
        String style = null;
        //if(p.styling.get() != null && !p.styling.get().equals("Label")) {
        //    style = p.styling.get();
        //}
        String summary = p.summary.get() + "";
        if ((summary.indexOf("http") < 0)
                || (summary.indexOf("file:") < 0)) {
            //find the portion that contains carrousel
            summary = StringUtil.replaceAll(p.summary.get(),
                    " <carousel>   <img src=\"\"/>  </carousel>",
                    "");
            ////////log.p("Adjusted " + summary);
        }
        if (summary.length() > 0) {
            p.summary.set(summary);
        }
        if (p.summary.get().indexOf('<') > -1) {
            if (style != null) {
                //body = new RichTextView(p.summary.get(), "PostStyleText");

                body = new XMLView();
                new DefaultXMLViewKit().install((XMLView) body);
                ((XMLView) body).setXML(p.summary.get());
                //((RichTextView)body).setAlignment(CENTER);
                body.setUIID(style);
            } else {
                //body = new RichTextView(p.summary.get());
                body = new XMLView();
                new DefaultXMLViewKit().install((XMLView) body);
                ((XMLView) body).setXML(checkedXml(p.summary.get()));

                body.setUIID("HalfPaddedContainer");
            }
        } else {
            body = new SpanLabel(p.summary.get());
            if (style != null) {
                ((SpanLabel) body).setTextUIID("PostStyleText");
                body.setUIID(style);
            } else {
                body.setUIID("HalfPaddedContainer");
            }
        }
        //set an action for body so it get opened into 
        //a form.
        //p.refreshService();
        //body.addPointerPressedListener(e -> new PostForm(p).show());

        //add a likes number of likes
        //we put the likes in a
        body.setUIID("HalfPaddedContainer");
        CheckBox like = CheckBox.createToggle("Like");
        like.setUIID("CleanButton");
        Button comment = new Button("Comment", "CleanButton");
        ShareButton share = new ShareButton();
        //Button share = new Button("Share", "CleanButton");
        share.setUIID("CleanButton");
        FontImage.setMaterialIcon(like, FontImage.MATERIAL_THUMB_UP);
        FontImage.setMaterialIcon(comment,
                FontImage.MATERIAL_COMMENT);
        //FontImage.setMaterialIcon(share, FontImage.MATERIAL_SHARE);
        share.setText("Share");
        share.setTextToShare(p.summary.get());
        Container buttonBar = GridLayout.encloseIn(3, like, comment, share);
        buttonBar.setUIID("HalfPaddedContainer");

        if (p.ziemozi_user.size() > 0 )like.setSelected(p.likes.contains(p.ziemozi_user.get(0)));
        like.addActionListener(e -> like(p));
        //just update the likes. add this user to the likes array which should go to the 
        //and we mark the request as dirty so we can sync it with an update later
        //share.addActionListener(e -> Display.getInstance().share(p.summary.get()));
        //comment.addActionListener(e -> new CommentsForm(p, p).show());
        comment.addActionListener(e -> showCommentsForm(p, p));
        /*
       if(p.getAttachments().size() > 0) {
            Object key = p.getAttachments().keySet().iterator().next();
            return BoxLayout.encloseY(titleArea, body, 
                createMediaComponent(p.getAttachments().get(key).toString(), key.toString()),
                createPostStats(p), buttonBar);
        }
         */
        Container kk = BoxLayout.encloseY(
                titleArea, body, createPostStats(p), buttonBar);
        kk.getAllStyles().setBorder(Border.createEtchedRaised());
        return kk;
    }
    /**
     *
     */
    /**
     * Create a post item but without the clickable links
     *
     */
    public static Container createSimpleNewsItem(User u, Request p) {
        //////////log.p("User " + u.fullName());
        //////////log.p("User Json " + u.getPropertyIndex().toString());
        Container titleArea = createNewsTitle(u, p);
        Component body;
        String style = null;
        //if(p.styling.get() != null && !p.styling.get().equals("Label")) {
        //    style = p.styling.get();
        //}
        String summary = p.summary.get() + "";
        summary = p.simpleRequestSummary();
        Log.p(summary);
        if ((summary.indexOf("http") < 0)
                || (summary.indexOf("file:") < 0)) {
            //find the portion that contains carrousel
            summary = StringUtil.replaceAll(summary,
                    " <carousel>   <img src=\"\"/>  </carousel>",
                    "");
            ////////log.p("Adjusted " + summary);
        }
        //if (summary.length() > 0) {
        //    p.summary.set(summary);
        //}
        if (summary.indexOf('<') > -1) {
            if (style != null) {
                //body = new RichTextView(p.summary.get(), "PostStyleText");

                body = new XMLView();
                new DefaultXMLViewKit().install((XMLView) body);
                ((XMLView) body).setXML(summary);
                //((RichTextView)body).setAlignment(CENTER);
                body.setUIID(style);
            } else {
                //body = new RichTextView(p.summary.get());
                body = new XMLView();
                new DefaultXMLViewKit().install((XMLView) body);
                ((XMLView) body).setXML(checkedXml(summary));

                body.setUIID("HalfPaddedContainer");
            }
        } else {
            body = new SpanLabel(summary);
            if (style != null) {
                ((SpanLabel) body).setTextUIID("PostStyleText");
                body.setUIID(style);
            } else {
                body.setUIID("HalfPaddedContainer");
            }
        }
        //set an action for body so it get opened into
        //a form.
        //p.refreshService();
        //body.addPointerPressedListener(e -> new PostForm(p).show());

        //add a likes number of likes
        //we put the likes in a
        body.setUIID("HalfPaddedContainer");
        CheckBox like = CheckBox.createToggle("Like");
        like.setUIID("CleanButton");
        Button comment = new Button("Comment", "CleanButton");
        ShareButton share = new ShareButton();
        //Button share = new Button("Share", "CleanButton");
        share.setUIID("CleanButton");
        FontImage.setMaterialIcon(like, FontImage.MATERIAL_THUMB_UP);
        FontImage.setMaterialIcon(comment,
                FontImage.MATERIAL_COMMENT);
        //FontImage.setMaterialIcon(share, FontImage.MATERIAL_SHARE);
        share.setText("Share");
        share.setTextToShare(p.summary.get());
        Container buttonBar = GridLayout.encloseIn(3, like, comment, share);
        buttonBar.setUIID("HalfPaddedContainer");

        if (p.ziemozi_user.size() > 0 )like.setSelected(p.likes.contains(p.ziemozi_user.get(0)));
        like.addActionListener(e -> like(p));
        //just update the likes. add this user to the likes array which should go to the
        //and we mark the request as dirty so we can sync it with an update later
        //share.addActionListener(e -> Display.getInstance().share(p.summary.get()));
        //comment.addActionListener(e -> new CommentsForm(p, p).show());
        comment.addActionListener(e -> showCommentsForm(p, p));
        /*
       if(p.getAttachments().size() > 0) {
            Object key = p.getAttachments().keySet().iterator().next();
            return BoxLayout.encloseY(titleArea, body,
                createMediaComponent(p.getAttachments().get(key).toString(), key.toString()),
                createPostStats(p), buttonBar);
        }
         */
        Container kk = BoxLayout.encloseY(
                titleArea, body, createPostStats(p), buttonBar);
        kk.getAllStyles().setBorder(Border.createEtchedLowered());
        kk.getAllStyles().setMarginBottom(10);
        return kk;
    }

    private static Image placeholder;

    private static Component createMediaComponent(String mime, String id) {
        if (mime.startsWith("image")) {
            if (placeholder == null) {
                placeholder = Image.createImage(getDisplayWidth(),
                        getDisplayHeight() / 2, 0);
            }
            ScaleImageButton sb = new ScaleImageButton(
                    URLImage.createCachedImage(id, ServerAPI.mediaUrl(id),
                            placeholder, URLImage.FLAG_RESIZE_SCALE_TO_FILL));
            return sb;
        } else {
            try {
                Media media = MediaManager.createMedia(ServerAPI.mediaUrl(id),
                        true);
                MediaPlayer mp = new MediaPlayer(media) {
                    @Override
                    protected Dimension calcPreferredSize() {
                        return new Dimension(getDisplayWidth(),
                                getDisplayHeight() / 2);
                    }
                };
                mp.setLoop(true);
                return mp;
            } catch (IOException err) {
                Log.e(err);
                return new Label("Error loading media");
            }
        }
    }

    private static Container createPostStats(Request p) {
        ////////log.p("Creating Stats for " + p.getPropertyIndex().toString());
        Container stats = new Container(new BorderLayout(),
                "PaddedContainer");
        if (p.likes.size() > 0) {
            Label thumbUp = new Label("", "SmallBlueCircle");
            FontImage.setMaterialIcon(thumbUp,
                    FontImage.MATERIAL_THUMB_UP);
            Label count = new Label("" + p.likes.size(), "SmallLabel");
            stats.add(WEST, BoxLayout.encloseX(thumbUp, count));
        }
        //children will be the size of the comments children.
//        ////////log.p("\n\nNumber of comments = " + "0" + p._children.asList().size());
        p.refreshComments();
        int t = p.comments.asList().size();
//        List<Request> r = p.comments.asList();
//        for (Request rr:r){
//            ////////log.p("Comment " + rr.getPropertyIndex().toJSON());
//        }
        if (p.comments != null) {
            if (p.comments.asList().size() > 0) {
                stats.add(EAST, new Label(t + " comments",
                        "SmallLabel"));
            }
        }
        return stats;
    }

    private static Container createWelcomeBar() {
        //Button avatar = new Button(ServerAPI.me().getAvatar(6.5f), "Label");
        TextArea welcome = new TextArea();
        String w = " ****Pull To Refresh Ziemozi Newsfeed.*** ";
        welcome.setText(w);
        welcome.setEditable(false);
        welcome.getStyle().setAlignment(Component.CENTER);
        //welcome.setUIID("SmallBlueLabel");
        Button map = new Button("Pull To Refresh Ziemozi Feed");
        //map.addActionListener(e -> new MapShowForm(localAPI.newsfeed(1, 100)).show());
        //map.addActionlistener(e -> )
        Container c = BorderLayout.center(map);
        //c.add(SOUTH, map);
        c.setUIID("HalfPaddedContainer");

        return c;
    }

    private static void showCommentsForm(Request p, Request pp) {
        //call the comment form only if this allows comments
        if (p.service.get(0).comment_services.size() >= 0) {
            new CommentsForm(p, p).show();
        }

    }

    private static void like(Request p) {
        ToastBar.showInfoMessage("Liking this  message ..");
        Service like = ServerAPI.getLikeService();
        if (like == null) {
            ToastBar.showErrorMessage("Liking not yet available");
        } else {
            Request likeRequest = new Request();
            likeRequest.service.add(like);
            likeRequest.createRequest("likedrequest", p._id.get());
            //go through the request parameters and find  the one that corresponds
            //to the request_id attribute
            //and set it to this request

            likeRequest.save();

        }
    }
}
