package insane96mcp.desirepaths.feature;

import insane96mcp.desirepaths.DesirePaths;
import insane96mcp.desirepaths.data.BlockTransformation;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Desire Paths", description = "Wear down grass when passing on it.")
@LoadFeature(module = DesirePaths.RESOURCE_PREFIX + "base", canBeDisabled = false)
public class DesirePathsFeature extends Feature {
	public static final TagKey<Block> TALL_GRASS = TagKey.create(Registries.BLOCK, new ResourceLocation(DesirePaths.MOD_ID, "tall_grass"));

	private static final List<String> DEFAULT_TRANSFORMATION_LIST = List.of("minecraft:grass_block,minecraft:dirt", "minecraft:dirt,minecraft:coarse_dirt");
	private static ForgeConfigSpec.ConfigValue<List<? extends String>> transformationListConfig;

	public static ArrayList<BlockTransformation> transformationList;

	@Config(min = 0d, max = 1d)
	@Label(name = "Chance to transform", description = "Chance for blocks to transform each tick")
	public static Double chanceToTransform = 0.008d;
	@Config
	@Label(name = "Speed based chance", description = "If true, speed will increase/decrease based off speed. The base chance 'Chance to transform' is when you walk.")
	public static Boolean speedBasedChance = true;
	@Config(min = 0d, max = 1d)
	@Label(name = "Feather falling transform chance reduction", description = "Each level of Feather Falling will reduce the chance to transform by this percentage amount. E.g. with the default settings, Feather Falling IV will fully prevent blocks from transforming.")
	public static Double featherFallingTransformChanceReduction = 0.25d;
	@Config
	@Label(name = "Crouch prevents transform", description = "If true, crouching will prevent blocks from transforming.")
	public static Boolean crouchPreventsTransform = true;
	@Config
	@Label(name = "Break Tall Grass", description = "Tall grass is broken when grass is transformed")
	public static Boolean breakTallGrass = true;

	public DesirePathsFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void loadConfigOptions() {
		super.loadConfigOptions();
		transformationListConfig = this.getBuilder()
				.comment("Transform list of blocks")
				.defineList("Transformation List", DEFAULT_TRANSFORMATION_LIST, o -> o instanceof String);
	}

	@Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
		transformationList = BlockTransformation.parseStringList(transformationListConfig.get());
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				|| event.player.level().isClientSide
				|| event.phase != TickEvent.Phase.START
				|| !event.player.onGround())
			return;

		if (crouchPreventsTransform && event.player.isCrouching())
			return;

		float walkDistDelta = event.player.walkDist - event.player.walkDistO;
		float chance = chanceToTransform.floatValue();
		if (speedBasedChance)
			chance *= walkDistDelta / 0.12f; //chance:0.12=x:walkDistDelta
		if (featherFallingTransformChanceReduction > 0d) {
			int featherFallingLvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.FALL_PROTECTION, event.player);
			chance *= 1f - featherFallingTransformChanceReduction.floatValue() * featherFallingLvl;
		}
		AABB bb = event.player.getBoundingBox().deflate(0.1d, 0.1d, 0.1d);
		int mX = Mth.floor(bb.minX);
		int mZ = Mth.floor(bb.minZ);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x2 = mX; x2 < bb.maxX; x2++) {
			for (int z2 = mZ; z2 < bb.maxZ; z2++) {
				pos.set(x2, event.player.position().y - 1.0E-5F, z2);
				BlockState state = event.player.level().getBlockState(pos);
				for (BlockTransformation blockTransformation : transformationList) {
					if (!blockTransformation.blockToTransform.matchesBlock(state.getBlock()))
						continue;

					if (event.player.getRandom().nextFloat() < chance) {
						Block block = ForgeRegistries.BLOCKS.getValue(blockTransformation.transformTo);
						if (block == null) continue;
						event.player.level().setBlockAndUpdate(pos, block.defaultBlockState());

						if (!breakTallGrass) continue;
						pos.set(x2, event.player.position().y + 0.002d, z2);
						state = event.player.level().getBlockState(pos);
						if (state.is(TALL_GRASS))
							event.player.level().destroyBlock(pos, false, event.player);
					}
				}
			}
		}
	}
}