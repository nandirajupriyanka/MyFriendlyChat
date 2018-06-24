package com.priyankanandiraju.friendlychat.auth;

import android.content.Context;
import android.content.Intent;

import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;

/**
 * Created by priyankanandiraju on 6/24/18.
 */

public final class AuthUIHelper {
    public static Intent getSignInIntent() {
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
                        /*new AuthUI.IdpConfig.FacebookBuilder().build(),*/
                        /*new AuthUI.IdpConfig.TwitterBuilder().build()*/))
                .build();
    }

    public static void performSignOut(Context context) {
        AuthUI.getInstance().signOut(context);
    }
}
