package com.infamous.dungeons_gear;

import com.infamous.dungeons_gear.capabilities.artifact.ArtifactUsage;
import com.infamous.dungeons_gear.capabilities.artifact.ArtifactUsageStorage;
import com.infamous.dungeons_gear.capabilities.artifact.IArtifactUsage;
import com.infamous.dungeons_gear.capabilities.bow.Bow;
import com.infamous.dungeons_gear.capabilities.bow.BowStorage;
import com.infamous.dungeons_gear.capabilities.bow.IBow;
import com.infamous.dungeons_gear.capabilities.combo.Combo;
import com.infamous.dungeons_gear.capabilities.combo.ComboStorage;
import com.infamous.dungeons_gear.capabilities.combo.ICombo;
import com.infamous.dungeons_gear.capabilities.offhand.IOffhand;
import com.infamous.dungeons_gear.capabilities.offhand.Offhand;
import com.infamous.dungeons_gear.capabilities.offhand.OffhandStorage;
import com.infamous.dungeons_gear.client.ClientProxy;
import com.infamous.dungeons_gear.client.renderer.BeamEntityRenderer;
import com.infamous.dungeons_gear.client.renderer.IceCloudRenderer;
import com.infamous.dungeons_gear.client.renderer.totem.BuzzyNestRenderer;
import com.infamous.dungeons_gear.client.renderer.totem.FireworksDisplayRenderer;
import com.infamous.dungeons_gear.client.renderer.totem.TotemOfRegenerationRenderer;
import com.infamous.dungeons_gear.client.renderer.totem.TotemOfShieldingRenderer;
import com.infamous.dungeons_gear.compat.DungeonsGearCompatibility;
import com.infamous.dungeons_gear.compat.ElenaiCompat;
import com.infamous.dungeons_gear.config.DungeonsGearConfig;
import com.infamous.dungeons_gear.entities.ModEntityTypes;
import com.infamous.dungeons_gear.groups.ArmorGroup;
import com.infamous.dungeons_gear.groups.ArtifactGroup;
import com.infamous.dungeons_gear.groups.MeleeWeaponGroup;
import com.infamous.dungeons_gear.groups.RangedWeaponGroup;
import com.infamous.dungeons_gear.items.DualWieldItemProperties;
import com.infamous.dungeons_gear.items.GearRangedItemModelProperties;
import com.infamous.dungeons_gear.items.armor.FreezingResistanceArmorGear;
import com.infamous.dungeons_gear.items.armor.PetBatArmorGear;
import com.infamous.dungeons_gear.loot.LootConditionRegistry;
import com.infamous.dungeons_gear.loot.ModLootFunctionTypes;
import com.infamous.dungeons_gear.network.NetworkHandler;
import com.infamous.dungeons_gear.registry.AttributeRegistry;
import com.infamous.dungeons_gear.registry.ItemRegistry;
import com.infamous.dungeons_gear.registry.ParticleInit;
import com.infamous.dungeons_libraries.client.renderer.ArmorGearRenderer;
import com.infamous.dungeons_libraries.items.gearconfig.ArmorGear;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

import static com.infamous.dungeons_gear.integration.curios.client.CuriosKeyBindings.setupCuriosKeybindings;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DungeonsGear.MODID)
public class DungeonsGear
{
    // Directly reference a log4j logger.
    public static final String MODID = "dungeons_gear";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ItemGroup MELEE_WEAPON_GROUP = new MeleeWeaponGroup();
    public static final ItemGroup RANGED_WEAPON_GROUP = new RangedWeaponGroup();
    public static final ItemGroup ARTIFACT_GROUP = new ArtifactGroup();
    public static final ItemGroup ARMOR_GROUP = new ArmorGroup();

    public static CommonProxy PROXY;

    public DungeonsGear() {

        new DungeonsGearConfig();
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);


        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ParticleInit.PARTICLES.register(modEventBus);
        AttributeRegistry.ATTRIBUTES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        LootConditionRegistry.init();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void processIMC(final InterModProcessEvent event) {
        DungeonsGearCompatibility.checkCompatStatus();
        if (DungeonsGearCompatibility.elenaiDodge)
            MinecraftForge.EVENT_BUS.register(ElenaiCompat.class);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.init();
        CapabilityManager.INSTANCE.register(ICombo.class, new ComboStorage(), Combo::new);
        CapabilityManager.INSTANCE.register(IArtifactUsage.class, new ArtifactUsageStorage(), ArtifactUsage::new);
        CapabilityManager.INSTANCE.register(IBow.class, new BowStorage(), Bow::new);
        CapabilityManager.INSTANCE.register(IOffhand.class, new OffhandStorage(), Offhand::new);
        event.enqueueWork(ModLootFunctionTypes::register);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {

        MinecraftForge.EVENT_BUS.register(new DualWieldItemProperties());
        GearRangedItemModelProperties.init();

        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.ICE_CLOUD.get(), IceCloudRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.BUZZY_NEST.get(), BuzzyNestRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.TOTEM_OF_SHIELDING.get(), TotemOfShieldingRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.TOTEM_OF_REGENERATION.get(), TotemOfRegenerationRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.FIREWORKS_DISPLAY.get(), FireworksDisplayRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.BEAM_ENTITY.get(), BeamEntityRenderer::new);

        setupCuriosKeybindings();

        GeoArmorRenderer.registerArmorRenderer(FreezingResistanceArmorGear.class, ArmorGearRenderer::new);
        GeoArmorRenderer.registerArmorRenderer(PetBatArmorGear.class, ArmorGearRenderer::new);
    }
}
