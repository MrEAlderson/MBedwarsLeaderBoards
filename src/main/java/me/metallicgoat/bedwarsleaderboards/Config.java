package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.tools.YamlConfigurationDescriptor;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {

  public static long reCacheMinutes = 10L;
  public static int positionsCached = 15;

  public static String unfilledRank = "-";
  public static String uncachedPosition = "UNCACHED POSITION";
  public static String dataLoading = "Loading...";

  private static final byte VERSION = 0;

  private static File getFile(LeaderboardsPlugin plugin) {
    return new File(LeaderboardsPlugin.getAddon().getDataFolder(), "config.yml");
  }

  public static void load(LeaderboardsPlugin plugin) {
    synchronized (Config.class) {
      try {
        loadUnchecked(plugin);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static void loadUnchecked(LeaderboardsPlugin plugin) throws Exception {
    final File file = getFile(plugin);

    if (!file.exists()) {
      save(plugin);
      return;
    }

    // load it
    final FileConfiguration config = new YamlConfiguration();

    try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      config.load(reader);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // read it
    reCacheMinutes = config.getLong("re-cache-minutes", reCacheMinutes);
    positionsCached = config.getInt("positions-cached", positionsCached);

    unfilledRank = config.getString("unfilled-rank", unfilledRank);
    uncachedPosition = config.getString("uncached-position", uncachedPosition);
    dataLoading = config.getString("data-loading", dataLoading);

    // auto update file if newer version
    {
      final int currentVersion = config.getInt("file-version", -1);

      if (currentVersion != VERSION)
        save(plugin);
    }
  }

  private static void save(LeaderboardsPlugin plugin) throws Exception {
    final YamlConfigurationDescriptor config = new YamlConfigurationDescriptor();

    config.addComment("Used for auto-updating the config file. Ignore it");
    config.set("file-version", VERSION);

    config.addEmptyLine();

    config.addComment("PLACEHOLDERS:");
    config.addComment("---> %MBLeaderboards_playeratposition-<statId>-<position>%");
    config.addComment("---> %MBLeaderboards_valueatposition-<statId>-<position>%");
    config.addComment("---> %MBLeaderboards_playerposition-<statId>%");

    config.addEmptyLine();
    config.addEmptyLine();
    config.addEmptyLine();

    config.addComment("How many minutes before rankings will automatically be recalculated.");
    config.addComment("We recommend you keep this no lower than 5 minutes");
    config.set("re-cache-minutes", Config.reCacheMinutes);

    config.addEmptyLine();

    config.addComment("How many rankings should be cached at any given time.");
    config.set("positions-cached", Config.positionsCached);

    config.addEmptyLine();

    config.addComment("What should be returned if there is no player at a certian rank.");
    config.addComment("For example if you want the 10th position, but only 5 players have played");
    config.set("unfilled-rank", Config.unfilledRank);

    config.addEmptyLine();

    config.addComment("What should be returned if someone is requesting a placeholder for a rank that is not");
    config.addComment("kept in cache as defined by 'positions-cached' config.");
    config.addComment("NOTE: this is only related to the '%MBLeaderboards_playeratposition-<statId>-<position>%' placeholder");
    config.set("uncached-position", Config.uncachedPosition);

    config.addEmptyLine();

    config.addComment("What should be displayed if a requested placeholder is still in the process of being cached");
    config.set("data-loading", Config.dataLoading);

    // save
    getFile(plugin).getParentFile().mkdirs();

    try (Writer writer = Files.newBufferedWriter(getFile(plugin).toPath(), StandardCharsets.UTF_8)) {
      writer.write(config.saveToString());
    }
  }
}
