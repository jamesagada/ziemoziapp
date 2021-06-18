/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.io.Log;
import com.codename1.ui.ComponentSelector;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author jamesagada
 * watches broadcasts the changes in the field values
 */
public class FieldBroadcast extends Observable{

   public FieldBroadcast(){
       
   } 
  public void fieldChanged(Object o){
      setChanged();
      notifyObservers(o);
      //////////Log.p("Broadcasted change to " + this.countObservers()+ " listeners");
      
  }
}
