package com.br.yat.gerenciador.util;

import java.util.ArrayList;
import java.util.List;
import com.br.yat.gerenciador.controller.RefreshCallback;

public class AppEventManager {
    private static final List<RefreshCallback> listenersLogo = new ArrayList<>();

    public static void subscribeLogoChange(RefreshCallback callback) {
        listenersLogo.add(callback);
    }

    public static void notifyLogoChange() {
        listenersLogo.forEach(RefreshCallback::onSaveSuccess);
    }
}
