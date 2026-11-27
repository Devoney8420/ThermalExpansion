package cofh.thermal.expansion;

import cofh.thermal.expansion.common.config.ThermalDynamoConfig;
import cofh.thermal.expansion.common.config.ThermalMachineConfig;
import cofh.thermal.expansion.init.registries.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static cofh.lib.util.FlagManager.setFlag;
import static cofh.lib.util.constants.ModIds.ID_THERMAL_EXPANSION;
import static cofh.thermal.core.ThermalCore.CONFIG_MANAGER;
import static cofh.thermal.lib.util.ThermalFlags.*;
import static cofh.thermal.lib.util.ThermalIDs.*;

@Mod (ID_THERMAL_EXPANSION)
public class ThermalExpansion {

    public ThermalExpansion(ModContainer modContainer, IEventBus modEventBus) {

        setFeatureFlags();

        CONFIG_MANAGER.register(modEventBus)
                .addServerConfig(new ThermalDynamoConfig())
                .addServerConfig(new ThermalMachineConfig());

        modEventBus.addListener(this::capabilitySetup);

        TExpBlocks.register();
        TExpItems.register();

        TExpContainers.register();
        TExpSounds.register();
        TExpBlockEntities.register();
    }

    private void setFeatureFlags() {

        setFlag(FLAG_RESOURCE_CINNABAR, true);
        setFlag(FLAG_RESOURCE_OIL, true);

        setFlag(FLAG_AREA_AUGMENTS, true);
        setFlag(FLAG_DYNAMO_AUGMENTS, true);
        setFlag(FLAG_MACHINE_AUGMENTS, true);

        setFlag(FLAG_TOOL_COMPONENTS, true);

        setFlag(FLAG_COINS, true);
        setFlag(FLAG_PLATES, true);

        setFlag(ID_MACHINE_FRAME, true);

        setFlag(ID_DEVICE_TREE_EXTRACTOR, true);
        setFlag(ID_DEVICE_FISHER, true);
        setFlag(ID_DEVICE_COMPOSTER, true);
        setFlag(ID_DEVICE_WATER_GEN, true);
        setFlag(ID_DEVICE_ROCK_GEN, true);
        setFlag(ID_DEVICE_COLLECTOR, true);
        setFlag(ID_DEVICE_XP_CONDENSER, true);
        setFlag(ID_DEVICE_NULLIFIER, true);
        setFlag(ID_DEVICE_POTION_DIFFUSER, true);

        setFlag(ID_DYNAMO_STIRLING, true);
        setFlag(ID_DYNAMO_COMPRESSION, true);
        setFlag(ID_DYNAMO_MAGMATIC, true);
        setFlag(ID_DYNAMO_NUMISMATIC, true);
        setFlag(ID_DYNAMO_LAPIDARY, true);
        setFlag(ID_DYNAMO_DISENCHANTMENT, true);
        setFlag(ID_DYNAMO_GOURMAND, true);

        setFlag(ID_MACHINE_FURNACE, true);
        setFlag(ID_MACHINE_SAWMILL, true);
        setFlag(ID_MACHINE_PULVERIZER, true);
        setFlag(ID_MACHINE_SMELTER, true);
        setFlag(ID_MACHINE_INSOLATOR, true);
        setFlag(ID_MACHINE_CENTRIFUGE, true);
        setFlag(ID_MACHINE_PRESS, true);
        setFlag(ID_MACHINE_CRUCIBLE, true);
        setFlag(ID_MACHINE_CHILLER, true);
        setFlag(ID_MACHINE_REFINERY, true);
        setFlag(ID_MACHINE_PYROLYZER, true);
        setFlag(ID_MACHINE_BOTTLER, true);
        setFlag(ID_MACHINE_BREWER, true);
        setFlag(ID_MACHINE_CRYSTALLIZER, true);
        setFlag(ID_MACHINE_CRAFTER, true);

        setFlag(ID_ENERGY_CELL_FRAME, true);
        setFlag(ID_ENERGY_CELL, true);

        setFlag(ID_FLUID_CELL_FRAME, true);
        setFlag(ID_FLUID_CELL, true);

        //        setFlag(ID_ITEM_CELL_FRAME, true);
        //        setFlag(ID_ITEM_CELL, true);

        setFlag(FLAG_XP_STORAGE_AUGMENT, true);
    }

    // region INITIALIZATION
    private void capabilitySetup(RegisterCapabilitiesEvent event) {

        TExpBlockEntities.capabilitySetup(event);
    }

    // endregion
}
