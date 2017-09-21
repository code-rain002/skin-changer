package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.PlayerProfile;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.mojang.auth.Account;

import java.util.UUID;

public abstract class SharedUploader implements Runnable, MessageReceiver {

    protected final ChangeSkinCore core;
    protected final Account owner;
    protected final String url;

    public SharedUploader(ChangeSkinCore core, Account owner, String url) {
        this.core = core;
        this.owner = owner;
        this.url = url;
    }

    @Override
    public void run() {
        PlayerProfile profile = owner.getProfile();
        String oldSkinUrl = core.getMojangAuthApi().getSkinUrl(profile.getName());

        UUID uuid = ChangeSkinCore.parseId(profile.getId());
        UUID accessToken = ChangeSkinCore.parseId(owner.getAccessToken());
        core.getMojangAuthApi().changeSkin(uuid, accessToken, url, false);

        //this could properly cause issues for uuid resolving to this database entry
        SkinData newSkin = core.getMojangSkinApi().downloadSkin(uuid);
        core.getStorage().save(newSkin);

        core.getMojangAuthApi().changeSkin(uuid, accessToken, oldSkinUrl, false);
        sendMessageInvoker("skin-uploaded", owner.getProfile().getName(), "Skin-" + newSkin.getSkinId());
    }
}
