package cofh.thermal.expansion.client.event;

import cofh.thermal.expansion.client.gui.dynamo.*;
import cofh.thermal.expansion.client.gui.machine.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static cofh.lib.util.constants.ModIds.ID_THERMAL_EXPANSION;
import static cofh.thermal.expansion.init.registries.TExpContainers.*;

@EventBusSubscriber(value = Dist.CLIENT, modid = ID_THERMAL_EXPANSION)
public final class TExpClientEvents {

    private TExpClientEvents() {
    }

    @SubscribeEvent
    public static void menuScreenSetup(final RegisterMenuScreensEvent event) {

        event.register(MACHINE_FURNACE_CONTAINER.get(), MachineFurnaceScreen::new);
        event.register(MACHINE_SAWMILL_CONTAINER.get(), MachineSawmillScreen::new);
        event.register(MACHINE_PULVERIZER_CONTAINER.get(), MachinePulverizerScreen::new);
        event.register(MACHINE_SMELTER_CONTAINER.get(), MachineSmelterScreen::new);
        event.register(MACHINE_INSOLATOR_CONTAINER.get(), MachineInsolatorScreen::new);
        event.register(MACHINE_CENTRIFUGE_CONTAINER.get(), MachineCentrifugeScreen::new);
        event.register(MACHINE_PRESS_CONTAINER.get(), MachinePressScreen::new);
        event.register(MACHINE_CRUCIBLE_CONTAINER.get(), MachineCrucibleScreen::new);
        event.register(MACHINE_CHILLER_CONTAINER.get(), MachineChillerScreen::new);
        event.register(MACHINE_REFINERY_CONTAINER.get(), MachineRefineryScreen::new);
        event.register(MACHINE_PYROLYZER_CONTAINER.get(), MachinePyrolyzerScreen::new);
        event.register(MACHINE_BREWER_CONTAINER.get(), MachineBrewerScreen::new);
        event.register(MACHINE_BOTTLER_CONTAINER.get(), MachineBottlerScreen::new);
        event.register(MACHINE_CRYSTALLIZER_CONTAINER.get(), MachineCrystallizerScreen::new);
        event.register(MACHINE_CRAFTER_CONTAINER.get(), MachineCrafterScreen::new);

        event.register(DYNAMO_STIRLING_CONTAINER.get(), DynamoStirlingScreen::new);
        event.register(DYNAMO_COMPRESSION_CONTAINER.get(), DynamoCompressionScreen::new);
        event.register(DYNAMO_MAGMATIC_CONTAINER.get(), DynamoMagmaticScreen::new);
        event.register(DYNAMO_NUMISMATIC_CONTAINER.get(), DynamoNumismaticScreen::new);
        event.register(DYNAMO_LAPIDARY_CONTAINER.get(), DynamoLapidaryScreen::new);
        event.register(DYNAMO_DISENCHANTMENT_CONTAINER.get(), DynamoDisenchantmentScreen::new);
        event.register(DYNAMO_GOURMAND_CONTAINER.get(), DynamoGourmandScreen::new);
    }
}
