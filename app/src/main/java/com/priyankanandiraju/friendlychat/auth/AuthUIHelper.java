package com.priyankanandiraju.friendlychat.auth;

import android.content.Context;
import android.content.Intent;

import com.firebase.ui.auth.AuthUI;
import com.priyankanandiraju.friendlychat.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by priyankanandiraju on 6/24/18.
 */

public final class AuthUIHelper {

    private static List<AuthUI.IdpConfig> getAuthProviders() {
        return Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());
    }

    public static Intent getSignInIntent() {
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(getAuthProviders())
                .setTheme(R.style.LoginTheme)
                .build();
    }

    public static void performSignOut(Context context) {
        AuthUI.getInstance().signOut(context);
    }
}
