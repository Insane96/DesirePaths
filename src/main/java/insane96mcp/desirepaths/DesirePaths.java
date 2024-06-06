package insane96mcp.desirepaths;

import com.mojang.logging.LogUtils;
import insane96mcp.desirepaths.setup.DPCommonConfig;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DesirePaths.MOD_ID)
public class DesirePaths
{
    public static final String MOD_ID = "desirepaths";
    public static final String RESOURCE_PREFIX = MOD_ID + ":";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Module base;

    public DesirePaths()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DPCommonConfig.CONFIG_SPEC, MOD_ID + ".toml");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    }

    public static void initModule() {
        base = Module.Builder.create(DesirePaths.RESOURCE_PREFIX + "base", "base", ModConfig.Type.COMMON, DPCommonConfig.builder).canBeDisabled(false).build();
    }
}