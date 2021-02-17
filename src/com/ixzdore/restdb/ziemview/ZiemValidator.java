/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.ui.ComponentSelector;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import java.util.Vector;

/**
 *
 * @author jamesagada
 * the ziem validator accepts the value of the attribute and  the serviceAttribute
 * and executes the validations attached to the attribute against the value
 * since the validation may include other attributes in the service calculation
 * it also takes the full service definition and the full requestParameter
 */
public class ZiemValidator {

public ZiemValidator(){
   ComponentSelector cs; 
}   
public ZiemValidator(ComponentSelector c){
    //run the validation on this component.
    //the set of components have the validations which are then extracted and executed.
}
public void validate(){
    //validates and populates the validateResult array
}
public void addValidator(String validateWhat, String validateScript){
    //validateWhat - what is the script to be used for validating
    //validateScript - the script which will be executed and used to validate.
}
}
