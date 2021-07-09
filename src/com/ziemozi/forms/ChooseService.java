package com.ziemozi.forms;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.ui.Graphics;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Border;
import com.ixzdore.restdb.ziemobject.Post;
import static com.codename1.ui.CN.getCurrentForm;
import com.codename1.ui.Component;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.tree.Tree;
import com.codename1.ui.tree.TreeModel;
import java.util.Vector;
import com.codename1.io.Log;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.convertToPixels;
import static com.codename1.ui.CN.createStorageInputStream;
import static com.codename1.ui.CN.deleteStorageFile;
import static com.codename1.ui.CN.existsInStorage;
import static com.codename1.ui.Component.CENTER;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Tabs;
import com.codename1.ui.URLImage;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Group;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.ServerAPI;
import static com.ziemozi.server.ServerAPI.providers;
import com.ziemozi.server.local.localAPI;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChooseService extends Form {

    EncodedImage placeholder;

    public ChooseService(Object ctc) {
        super("Ziem Ozi", BoxLayout.y());
        Boolean isCategory = false;
        try {
            Category cc = (Category) ctc;
            if (cc.name.get() != null) {
                this.setTitle(" Ozi " + cc.name.get());

            } else {
                cc = null;
            }
            isCategory = true;
            add(ServiceSelector(cc));
        } catch (ClassCastException e) {
            //we are only interested in ClassCast Exception
            isCategory = false;
        }
        if (!isCategory) {
            try {
                ArrayList<Category> cc = (ArrayList<Category>) ctc;
                add(categorySelector(cc));
            } catch (ClassCastException e) {
                // we really cannot do mutch
                ToastBar.showErrorMessage("No Services To Report");
            }
        }

        Form previous = getCurrentForm();
        Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
        FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
        placeholder = EncodedImage.createFromImage(p.scaled(p.getWidth() * 3, p.getHeight() * 3), false);
        getToolbar().addMaterialCommandToLeftBar("",
                MATERIAL_ARROW_BACK, e -> previous.showBack());
    }

    public Component categorySelector(ArrayList<Category> c) {
        //we will create a tab consisting of 
        //Categories
        //Providers
        //Favorites
        //InfiniteProgress ip = new InfiniteProgress();   
        //Dialog ipd = ip.showInfiniteBlocking();
        Dialog ip = new InfiniteProgress().showInfiniteBlocking();
        Tabs t = new Tabs();
        t.setAnimateTabSelection(true);
        t.setTabPlacement(Component.TOP);
        //Style s = UIManager.getInstance().getComponentStyle("Tab");
        if (c == null) {

            t.setUIID("Tab");

            t.addTab("Service Categories", categories(null));
            //t.addTab(" By Provider", providers());
            //t.addTab("Favorites", favorites());
            //ipd.dispose();
        } else {
            //we are dealing with a category group
            //we will only have categories
            t.setUIID("Tab");
            Component ccat = categoriesFromCategoryList(c);
            t.addTab("Service Categories", ccat);
        }
        ip.dispose();
        ip.remove();
        return t;
    }

    public Component ServiceSelector(Category c) {
        //we will create a tab consisting of 
        //Categories
        //Providers
        //Favorites
        //InfiniteProgress ip = new InfiniteProgress();   
        //Dialog ipd = ip.showInfiniteBlocking();
        Dialog ip = new InfiniteProgress().showInfiniteBlocking();
        Tabs t = new Tabs();
        t.setAnimateTabSelection(true);
        t.setTabPlacement(Component.TOP);
        //Style s = UIManager.getInstance().getComponentStyle("Tab");
        if (c == null) {

            t.setUIID("Tab");

            t.addTab("Service Categories", categories(null));
            //t.addTab(" By Provider", providers());
            //t.addTab("Favorites", favorites());
            //ipd.dispose();
        } else {
            //we are dealing with a category group
            //we will only have categories
            t.setUIID("Tab");
            Component ccat = categories(c);
            t.addTab("Service Categories", ccat);
        }
        ip.dispose();
        ip.remove();
        return t;
    }

    private Component providers() {
        Container cnt = new Container();
        cnt.setLayout(new GridLayout(2, 4));
        ArrayList<Provider> cat = ServerAPI.providers();
        for (Provider ct : cat) {
            ct.refreshIcon();
            ////////////Log.p("Provider " + ct.getPropertyIndex().toString());
            String mediaUrl = ServerAPI.mediaUrl(ct.icon.get(0));

            Button b = new Button(ct.name.get());
            b.setUIID("SmallLabel");
            b.setTextPosition(BOTTOM);
            Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
            FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
            EncodedImage placeholder = EncodedImage.createFromImage(p.scaled(p.getWidth() * 2, p.getHeight() * 2), false);
            if ((ct.icon.get(0)) != null) {
                Image i = URLImage.createToStorage(placeholder, ct.name.get(),
                        mediaUrl);
                b.setIcon(i);
            } else {
                b.setIcon(p);
            }
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    //get the services
                    InfiniteProgress ip = new InfiniteProgress();
                    Dialog ipd = ip.showInfiniteBlocking();
                    ArrayList<Service> s = localAPI.serviceList(ct);
                    ipd.dispose();
                    ip.remove();
                    ZiemForm z = new ZiemForm(s, b.getText());
                    //z.setTitle(b.getText() + "Report/Request");

                    z.show();
                }
            });
            cnt.add(b);
        }
        //cnt.setUIID("Title");
        return cnt;
    }

    private Component favorites() {
        Container cnt = new Container();
        cnt.setLayout(new GridLayout(2, 3));
        ArrayList<Category> cat = ServerAPI.categories();
        for (Category ct : cat) {
            ct.refreshIcon();
            String mediaUrl = ServerAPI.mediaUrl(ct.icon.get(0));

            Button b = new Button(ct.name.get());
            b.setUIID("SmallLabel");
            Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
            FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
            EncodedImage placeholder = EncodedImage.createFromImage(p.scaled(p.getWidth() * 2, p.getHeight() * 2), false);
            if ((ct.icon.get(0)) != null) {
                Image i = URLImage.createToStorage(placeholder, ct.name.get(),
                        mediaUrl);
                b.setIcon(i);
            } else {
                b.setIcon(p);
            }
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    //get the services
                    InfiniteProgress ip = new InfiniteProgress();
                    Dialog ipd = ip.showInfiniteBlocking();
                    ArrayList<Service> s = localAPI.favoriteServiceList(ct);
                    ipd.dispose();
                    ip.remove();
                    ZiemForm z = new ZiemForm(s, b.getText());
                    //z.setTitle(b.getText() + "Report/Request");

                    z.show();
                }
            });
            cnt.add(b);
        }
        return cnt;
    }

    private Component categories(Category cx) {
        Container cnt = new Container();
        cnt.setLayout(new GridLayout(4, 3));
        //cnt.setLayout(new FlowLayout());
        ArrayList<Category> cat = new ArrayList<Category>();
        if (cx == null) {
            cat = ServerAPI.categories(true);
        } else {
            //////////Log.p("finding subcategories for " + cx.name.get());
            cat = ServerAPI.categories(cx);
        }
        for (Category ct : cat) {
            ////////Log.p("Is in Menu " + ct.name.get() + " " + ct.inmenu.get());
            if (ct.inmenu.get().contains("true")) {
                ct.refreshIcon();
                String mediaUrl = ServerAPI.mediaUrl(ct.icon.get(0));

                Button b = new Button(ct.name.get());
                b.setUIID("SmallLabel");
                //b.getAllStyles().setBorder(Border.createEtchedRaised());
                b.setTextPosition(BOTTOM);
                Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
                s=new Style();

                s.setFgColor(0xc2c2c2);
                s.setBgTransparency(255);
                s.setBgColor(0xe9e9e9);
                FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
                EncodedImage placeholder = EncodedImage.createFromImage(p.scaled(
                        p.getWidth() * 2, p.getHeight() * 2), false);
                ////////////Log.p("\n\n" + ct.name.get() + "\n" + ct.getPropertyIndex().toString());
                Image roundMask = Image.createImage(placeholder.getWidth()*2
                        , placeholder.getHeight()*2, 0xff000000);
                Graphics g = roundMask.getGraphics();
                g.setAntiAliased(true);
                g.setColor(0x000000);
                g.fillRect(0, 0, placeholder.getWidth()*2, placeholder.getHeight()*2);
                g.setColor(0xffffff);
                g.fillArc(0, 0, placeholder.getWidth()*2, placeholder.getHeight()*2, 0, 360);

                if ((ct.icon.get(0)) != null) {
                    Image i = URLImage.createToStorage(placeholder, ct.icon.get(0),
                            mediaUrl);
                    //Image i = URLImage.createToStorage(placeholder,ct._id.get(),
                     //       mediaUrl,URLImage.createMaskAdapter(roundMask.createMask()));
                    //i.applyMask(roundMask.createMask());
                    b.setIcon(i.scaled(placeholder.getWidth()*2,
                            placeholder.getHeight()*2));
                  //  b.setIcon(i.scaled(placeholder.getWidth()*2,
                    //        placeholder.getHeight()*2).applyMask(roundMask.createMask()));

                } else {
                    b.setIcon(p);
                }
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        //get the services
                        InfiniteProgress ip = new InfiniteProgress();
                        Dialog ipd = ip.showInfiniteBlocking();
                        //check if the category is a group category 
                        //a group category has a the isgroup field as true
                        //////////Log.p("Is this a group " + ct.isgroup.get());
                        if (!ct.isgroup.get().contains("true")) {
                            ArrayList<Service> s = localAPI.serviceList(ct);
                            ArrayList<Service> ss = new ArrayList<Service>();
                            //////////Log.p("Selected Category " + ct.name.get());
                            //////Log.p("\n " + ct.name.get() + " has " + s.size() + " members");
                            User u = localAPI.me();
                            u.refreshGroups();
                            for (Service z : s) {
                                //take user list and check if the user has access.
                                //////Log.p(z.name.get());
                                if (checkUserAccess(u, z)) {
                                    ss.add(z);
                                }
                            }
                            ipd.dispose();
                            ip.remove();
                            ZiemForm z = new ZiemForm(ss, b.getText());
                            //z.setTitle(b.getText() + "Report/Request");

                            z.show();
                            //ipd.dispose();
                        } else {
                            //this is a group
                            //we need to fil the categories again. We just 
                            //popup a window with the list of categories so we can choose
                            ipd.dispose();
                            ip.remove();
                            showCategoryGroup(ct);
                        }
                    }

                });
                cnt.add(b);
            }
        }
        //cnt.setUIID("Title");        
        return cnt;
    }

    private boolean checkUserAccess(User u, Service z) {
        Boolean return_value = false;
        u.refreshGroups();
        for (Group g : u.groups.asList()) {
            //////Log.p(g.name.get());
            for (Group sg:z.groups.asList()) {
                ////////Log.p("Checking against " + sg.name.get());
                //////Log.p(g._id.get() + "against " + sg._id.get());
                if (g._id.get().equalsIgnoreCase(sg._id.get())) {
                    //////Log.p("Found A Match");
                    return_value = true;
                }
            }
        }
        return return_value;
    }

    private Component categoriesFromCategoryList(ArrayList<Category> cx) {
        Container cnt = new Container();
        cnt.setLayout(new GridLayout(3, 3));

        for (Category ct : cx) {
            ////////Log.p("Is in Menu " + ct.name.get() + " " + ct.inmenu.get());
            if (ct.inmenu.get().contains("true")) {
                ct.refreshIcon();
                String mediaUrl = ServerAPI.mediaUrl(ct.icon.get(0));

                Button b = new Button(ct.name.get());
                b.setUIID("SmallLabel");
                b.setTextPosition(BOTTOM);
                Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
                s = new Style();
                s.setFgColor(0xc2c2c2);
                s.setBgTransparency(255);
                s.setBgColor(0xe9e9e9);
                FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
                EncodedImage placeholder = EncodedImage.createFromImage(p.scaled(
                        p.getWidth() * 2, p.getHeight() * 2), false);
                ////////////Log.p("\n\n" + ct.name.get() + "\n" + ct.getPropertyIndex().toString());
                Image roundMask = Image.createImage(placeholder.getWidth()*2,
                        placeholder.getHeight()*2, 0xff000000);
                Graphics gr = roundMask.getGraphics();
                gr.setAntiAliased(true);
                gr.setColor(0x000000);
                gr.fillRect(0, 0, placeholder.getWidth()*2, placeholder.getHeight()*2);
                gr.setColor(0xffffff);
                gr.fillArc(0, 0, placeholder.getWidth()*2, placeholder.getHeight()*2, 0, 360);

                if ((ct.icon.get(0)) != null) {
                    Image i = URLImage.createToStorage(placeholder, ct.icon.get(0),
                           mediaUrl);
                    //Image i = URLImage.createToStorage(placeholder,ct._id.get(),
                    //        mediaUrl,URLImage.createMaskAdapter(roundMask.createMask()));
                    //i.applyMask(roundMask.createMask());
                    b.setIcon(i.scaled(placeholder.getWidth()*2,
                            placeholder.getHeight()*2));
                   // b.setIcon(i.scaled(placeholder.getWidth()*2,
//                            placeholder.getHeight()*2).applyMask(roundMask.createMask()));

                } else {
                    b.setIcon(p);
                }

                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        //get the services
                        InfiniteProgress ip = new InfiniteProgress();
                        Dialog ipd = ip.showInfiniteBlocking();
                        //check if the category is a group category 
                        //a group category has a the isgroup field as true
                        //////////Log.p("Is this a group " + ct.isgroup.get());
                        if (!ct.isgroup.get().contains("true")) {
                            ArrayList<Service> s = localAPI.serviceList(ct);
                            ArrayList<Service> ss = new ArrayList<Service>();
                            //////////Log.p("Selected Category " + ct.name.get());
                            //////////Log.p("\n " + ct.name.get() + " has " + s.size() + " members");
                            User u = localAPI.me();
                            u.refreshGroups();
                            for (Service z : s) {
                                //take user list and check if the user has access.
                                if (checkUserAccess(u, z)) {
                                   ss.add(z);
                                }
                            }                            
                            ipd.dispose();
                            ip.remove();
                            ZiemForm z = new ZiemForm(ss, b.getText());
                            //z.setTitle(b.getText() + "Report/Request");

                            z.show();
                            //ipd.dispose();
                        } else {
                            //this is a group
                            //we need to fil the categories again. We just 
                            //popup a window with the list of categories so we can choose
                            ipd.dispose();
                            ip.remove();
                            showCategoryGroup(ct);
                        }
                    }
                });
                cnt.add(b);
            }
        }
        //cnt.setUIID("Title");        
        return cnt;
    }

    private void showCategoryGroup(Category c) {
        //we take the category
        //determine the list of categories for which it is a parent and then show it 
        //in a form. 
        ChooseService s = new ChooseService(c);
        s.show();
    }
}
