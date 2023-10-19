package co.neeve.nae2;

import co.neeve.nae2.client.gui.PatternMultiToolButtonHandler;
import co.neeve.nae2.client.models.ModelManager;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.items.patternmultitool.GuiHandlerPatternMultiTool;
import co.neeve.nae2.common.items.patternmultitool.net.HandlerPatternMultiTool;
import co.neeve.nae2.common.items.patternmultitool.net.PatternMultiToolPacket;
import co.neeve.nae2.common.registries.RegistryHandler;
import co.neeve.nae2.server.WorldListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]",
	dependencies = "required-after:appliedenergistics2;required-after:mixinbooter@[8.3,)")
public class NAE2 {
	public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);
	public static NAE2 instance;
	private static ConfigManager configManager;
	private final RegistryHandler registryHandler = new RegistryHandler();

	public static void setupConfig() {
		if (configManager == null) {
			configManager = new ConfigManager();
		}
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		setupConfig();

		NAE2.instance = this;
		MinecraftForge.EVENT_BUS.register(new WorldListener());

		// TODO: move.
		boolean isClient = event.getSide() == Side.CLIENT;
		if (Features.PATTERN_MULTI_TOOL.isEnabled()) {
			NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerPatternMultiTool());
			NAE2.network.registerMessage(HandlerPatternMultiTool.class, PatternMultiToolPacket.class, 0, Side.SERVER);

			if (isClient) {
				MinecraftForge.EVENT_BUS.register(new PatternMultiToolButtonHandler());
			}
		}

		if (isClient) {
			MinecraftForge.EVENT_BUS.register(new ModelManager());
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		registryHandler.postInit();
	}

	public static class ConfigManager {

		public ConfigManager() {
			var file = new File("config/" + Tags.MODID + ".cfg");
			Configuration config = new Configuration(file);

			for (var feature : Features.values()) {
				String lowerCase = feature.name().toLowerCase();

				var featureCategory = config.getCategory(lowerCase);
				var entry = featureCategory.computeIfAbsent("enabled", x -> new Property("enabled", "true",
					Property.Type.BOOLEAN));

				feature.setEnabled(entry.getBoolean(true));

				var subFeatures = feature.getSubFeatures();
				if (subFeatures != null) {
					for (var subFeature : subFeatures) {
						var subFeatureLowerCase = subFeature.name().toLowerCase();
						var subFeatureEntry = featureCategory.computeIfAbsent(subFeatureLowerCase,
							x -> new Property(subFeatureLowerCase, "true", Property.Type.BOOLEAN));

						subFeature.setEnabled(subFeatureEntry.getBoolean(true));
						subFeatureEntry.setComment(subFeature.getDescription());
					}
				}
			}

			config.save();
		}
	}
}