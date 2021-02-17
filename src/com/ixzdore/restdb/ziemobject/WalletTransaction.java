/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;
import com.codename1.properties.IntProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Image;
import java.util.Date;
/**
 *
 * @author jamesagada
 * ({
    "_id",
    "icon",
    "description",
    "name",
    "_created",
    "_mock"
})
 */
public class WalletTransaction implements PropertyBusinessObject{
    public final Property<String, WalletTransaction> _id = new Property<>("_id");
    public final Property<String, WalletTransaction> wallet_txn_amount = new Property<>("wallet_txn_amount");
    public final ListProperty<Wallet, WalletTransaction> wallet_txn_source = 
            new ListProperty<>("wallet_txn_source",Wallet.class);
    public final ListProperty<Wallet, WalletTransaction> wallet_txn_destination = 
            new ListProperty<>("wallet_txn_destination",Wallet.class);
    public final Property<String, WalletTransaction> wallet_txn_comment = 
            new Property<>("wallet_txn_comment");
    public final Property<String, WalletTransaction> wallet_txn_reference = 
            new Property<>("wallet_txn_reference");
    public final Property<String, WalletTransaction> wallet_txn_status = 
            new Property<>("wallet_txn_status");
    public final Property<String,WalletTransaction> _created = new Property<>("_created");
    public final PropertyIndex idx = new PropertyIndex(this, "Wallet-Transaction",
            _id, wallet_txn_reference,wallet_txn_source,wallet_txn_destination,wallet_txn_amount,
            wallet_txn_reference,wallet_txn_status,wallet_txn_comment,_created);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public WalletTransaction(){

    }
}
