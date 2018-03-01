package com.avos.avoscloud;

import com.avos.avoscloud.data.Armor;
import com.avos.avoscloud.data.Player;
import com.avos.avoscloud.internal.AppConfiguration;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.internal.InternalLogger;
import com.avos.avoscloud.internal.impl.DefaultAppConfiguration;
import com.avos.avoscloud.internal.impl.DefaultAppRouter;

public class TestApp {

  public static void init() {
    TestApp.init("uu2P5gNTxGhjyaJGAPPnjCtJ-gzGzoHsz", "j5lErUd6q7LhPD8CXhfmA2Rg", true);
  }

  public static void init(String applicationId, String clientKey, boolean isCN) {
    AVObject.registerSubclass(Armor.class);
    AVObject.registerSubclass(Player.class);

    AppConfiguration configuration = DefaultAppConfiguration.instance();
    configuration.setIsCN(isCN);
    configuration.setApplicationId(applicationId);
    configuration.setClientKey(clientKey);

    InternalLogger logger = new Log4j2Implementation();
    logger.setDebugEnabled(true);

    new InternalConfigurationController.Builder().setAppConfiguration(configuration)
        .setAppRouter(DefaultAppRouter.instance()).setInternalLogger(logger)
        .setInternalPersistence(new SimplePersistence())
        .setDownloaderImplementation(FakeDownloader.class).build();
    InternalConfigurationController.globalInstance().getAppRouter().updateServerHosts();
  }

}
