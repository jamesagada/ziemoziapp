/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;

import com.codename1.properties.PropertyBusinessObject;
import java.util.HashMap;
import com.ixzdore.restdb.ziemobject.ServiceContact;
/**
 *
 * @author jamesagada
 * We use this to get instances of the ziemobjects from their
 * names since we cannot use introspection in codenameone
 */
public class ZiemObjectFactory {
    private static final HashMap<String, Class<? extends PropertyBusinessObject>> ziemObject = new
        HashMap<String, Class<? extends PropertyBusinessObject>>();
    private static void init(){
        ziemObject.clear();
        ziemObject.put("ServiceContact", ServiceContact.class);
        ziemObject.put("Service", Service.class);
        ziemObject.put("Category",Category.class);
        ziemObject.put("Provider",Provider.class);
    }
    public static Class<? extends PropertyBusinessObject>  getObjectFor(String obj) {
        if (ziemObject.isEmpty()) init();
        return ziemObject.get(obj);
    }
    
}
