package com.wuest.repurpose.Proxy;

import com.wuest.repurpose.Config.ModConfiguration;
import com.wuest.repurpose.Crafting.RecipeCondition;
import com.wuest.repurpose.Crafting.SmeltingCondition;
import com.wuest.repurpose.Loot.Conditions.ConfigRandomChance;
import com.wuest.repurpose.ModRegistry;
import com.wuest.repurpose.Repurpose;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemUseContext;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.conditions.LootConditionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;

public class CommonProxy {
	public static ModConfiguration proxyConfiguration;
	public static ForgeConfigSpec COMMON_SPEC;
	public static Path Config_File_Path;

	public CommonProxy() {
		// Builder.build is called during this method.
		Pair<ModConfiguration, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder()
				.configure(ModConfiguration::new);
		COMMON_SPEC = commonPair.getRight();
		proxyConfiguration = commonPair.getLeft();
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, COMMON_SPEC);
		CommonProxy.Config_File_Path = FMLPaths.CONFIGDIR.get().resolve("repurpose.toml");

		ModConfiguration.loadConfig(CommonProxy.COMMON_SPEC, CommonProxy.Config_File_Path);
	}

	/*
	 * Methods for ClientProxy to Override
	 */
	public void registerRenderers() {
	}

	public void preInit(FMLCommonSetupEvent event) {
		// Register recipe conditions
		CraftingHelper.register(new RecipeCondition.Serializer());
		CraftingHelper.register(new SmeltingCondition.Serializer());

		// Register loot table conditions.
		// TODO: Need to be able to register loot conditions.
		// NOTE: this will register the condition but seems kinda hacky.
		LootConditionType lootConditionType = ConfigRandomChance.lootConditionType;
		//LootConditionManager.registerCondition(new ConfigRandomChance.Serializer());

		Repurpose.network = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Repurpose.MODID, "main_channel"))
				.clientAcceptedVersions(Repurpose.PROTOCOL_VERSION::equals)
				.serverAcceptedVersions(Repurpose.PROTOCOL_VERSION::equals)
				.networkProtocolVersion(() -> Repurpose.PROTOCOL_VERSION).simpleChannel();

		ModRegistry.RegisterMessages();

		// Register the capabilities.
		ModRegistry.RegisterCapabilities();
	}

	public void init(FMLCommonSetupEvent event) {
	}

	public void postinit(FMLCommonSetupEvent event) {
	}

	public void openGuiForItem(ItemUseContext itemUseContext, Container container) {
	}

	public void openGuiForBlock(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
	}

	public void clientSetup(FMLClientSetupEvent clientSetupEvent) {
	}

	public ModConfiguration getServerConfiguration() {
		return CommonProxy.proxyConfiguration;
	}
}