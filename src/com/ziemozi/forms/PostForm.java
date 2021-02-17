package com.ziemozi.forms;

import com.ixzdore.restdb.ziemobject.Post;
import static com.codename1.ui.CN.getCurrentForm;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.layouts.BoxLayout;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.User;

public class PostForm extends Form {
    
    public PostForm(Request p) {
        super(p.service.get(0).description.get(), BoxLayout.y());
        p.refreshUser();
        User un = new User();
        int lastuser = p.ziemozi_user.size() - 1; //this is a hack  
        if (lastuser < 0) lastuser = 0;
        if (p.ziemozi_user.size() < 1) {
         add(NewsfeedContainer.createNewsItem(un, p));           
        }else {
            add(NewsfeedContainer.createNewsItem(p.ziemozi_user.get(lastuser), p));
        }
        Form previous = getCurrentForm();
        getToolbar().addMaterialCommandToLeftBar("", 
            MATERIAL_ARROW_BACK, e -> previous.showBack());
    }
}
