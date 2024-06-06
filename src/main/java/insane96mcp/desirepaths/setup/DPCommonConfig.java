package insane96mcp.desirepaths.setup;

import insane96mcp.desirepaths.DesirePaths;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class DPCommonConfig {
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final CommonConfig COMMON;

    public static final ForgeConfigSpec.Builder builder;

    static {
        builder = new ForgeConfigSpec.Builder();
        final Pair<CommonConfig, ForgeConfigSpec> specPair = builder.configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public static class CommonConfig {
        public CommonConfig(final ForgeConfigSpec.Builder builder) {
            DesirePaths.initModule();
            Module.loadFeatures(ModConfig.Type.COMMON, DesirePaths.MOD_ID, this.getClass().getClassLoader());
        }
    }
}
