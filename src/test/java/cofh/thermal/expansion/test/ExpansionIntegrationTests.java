package cofh.thermal.expansion.test;

import cofh.thermal.core.util.managers.machine.FurnaceRecipeManager;
import cofh.thermal.expansion.common.block.entity.machine.MachineBottlerBlockEntity;
import cofh.thermal.expansion.common.block.entity.machine.MachineCrafterBlockEntity;
import cofh.thermal.expansion.common.block.entity.machine.MachineFurnaceBlockEntity;
import cofh.thermal.lib.common.block.DynamoBlock;
import cofh.thermal.lib.common.block.entity.MachineBlockEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static cofh.lib.util.constants.BlockStatePropertiesCoFH.ACTIVE;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static cofh.lib.api.control.IReconfigurable.SideConfig.SIDE_INPUT;

@GameTestHolder("thermal_expansion_tests")
@PrefixGameTestTemplate(false)
public final class ExpansionIntegrationTests {

    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);
    private static final List<String> BLOCKS = List.of(
            "machine_furnace", "machine_sawmill", "machine_pulverizer", "machine_smelter", "machine_insolator",
            "machine_centrifuge", "machine_press", "machine_crucible", "machine_chiller", "machine_refinery",
            "machine_pyrolyzer", "machine_bottler", "machine_brewer", "machine_crystallizer", "machine_crafter",
            "dynamo_stirling", "dynamo_compression", "dynamo_magmatic", "dynamo_numismatic", "dynamo_lapidary",
            "dynamo_disenchantment", "dynamo_gourmand");

    @GameTest(template = "machines")
    public static void registersBlocksStatesAndRecipes(GameTestHelper test) {
        var level = test.getLevel();
        int count = 0;
        for (String name : BLOCKS) {
            var id = thermal(name);
            var block = BuiltInRegistries.BLOCK.getOptional(id).orElseThrow();
            var entityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(id).orElseThrow();
            boolean dynamo = block instanceof DynamoBlock;
            test.assertTrue(!block.defaultBlockState().getValue(ACTIVE), name + " should default to inactive");
            var states = block.getStateDefinition().getPossibleStates();
            test.assertValueEqual(states.size(), dynamo ? 24 : 8, name + " state count");
            for (var state : states) {
                test.assertTrue(entityType.isValid(state), name + " rejects block entity for " + state);
                if (dynamo) {
                    test.assertValueEqual(state.getFluidState().isSourceOfType(Fluids.WATER),
                            state.getValue(DynamoBlock.WATERLOGGED), name + " waterlogging");
                }
                ++count;
            }
            test.assertTrue(level.getRecipeManager().byKey(id).isPresent(), "Crafting recipe missing for " + id);
        }

        for (String recipe : List.of("machines/bottler/bottler_water_bottle", "machines/insolator/insolator_azalea_bush",
                "machines/centrifuge/centrifuge_allium", "machines/refinery/refinery_crude_oil")) {
            test.assertTrue(level.getRecipeManager().byKey(thermal(recipe)).isPresent(), "Machine recipe missing for " + recipe);
        }
        System.out.println("Verified " + BLOCKS.size() + " Expansion blocks, " + count + " block states, and their crafting recipes.");
        test.succeed();
    }

    @GameTest(template = "machines", timeoutTicks = 180)
    public static void redstoneFurnaceSmelts(GameTestHelper test) {
        putMachine(test, "machine_furnace", new ItemStack(Items.RAW_IRON));
        var furnace = (MachineFurnaceBlockEntity) test.getLevel().getBlockEntity(test.absolutePos(MACHINE));
        test.succeedWhen(() -> {
            test.assertTrue(furnace.getItemInv().get(1).is(Items.IRON_INGOT), "Furnace has not produced an iron ingot");
            test.assertTrue(furnace.getItemInv().get(0).isEmpty(), "Furnace did not consume the raw iron");
        });
    }

    @GameTest(template = "machines", timeoutTicks = 180)
    public static void fluidEncapsulatorBottlesWater(GameTestHelper test) {
        putMachine(test, "machine_bottler", new ItemStack(Items.GLASS_BOTTLE));
        var level = test.getLevel();
        var pos = test.absolutePos(MACHINE);
        var bottler = (MachineBottlerBlockEntity) level.getBlockEntity(pos);
        var water = new FluidStack(Fluids.WATER, 250);
        for (Direction face : Direction.values()) {
            var fluids = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, face);
            if (fluids != null && fluids.fill(water, FluidAction.SIMULATE) == 250) {
                test.assertValueEqual(fluids.fill(water, FluidAction.EXECUTE), 250, "Water accepted");
                break;
            }
        }
        test.assertValueEqual(bottler.getTankInv().get(0).getAmount(), 250, "Machine input tank");
        test.succeedWhen(() -> {
            ItemStack output = bottler.getItemInv().get(1);
            test.assertTrue(output.is(Items.POTION) && output.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER),
                    "Encapsulator has not produced a water bottle");
            test.assertTrue(bottler.getTankInv().get(0).isEmpty(), "Encapsulator did not consume the water");
        });
    }

    @GameTest(template = "machines", timeoutTicks = 180)
    public static void crafterAcceptsConfiguredPattern(GameTestHelper test) {
        placeAndPowerMachine(test, "machine_crafter");
        var crafter = (MachineCrafterBlockEntity) test.getLevel().getBlockEntity(test.absolutePos(MACHINE));
        int patternSlot = MachineCrafterBlockEntity.SLOT_CRAFTING_START;
        crafter.getItemInv().set(patternSlot, new ItemStack(Items.OAK_PLANKS));
        var packet = new FriendlyByteBuf(Unpooled.buffer());
        try {
            crafter.getConfigPacket(packet);
            crafter.getItemInv().set(patternSlot, ItemStack.EMPTY);
            crafter.handleConfigPacket(packet);
            test.assertTrue(!packet.isReadable(), "The crafter did not consume its complete config packet");
        } finally {
            packet.release();
        }
        test.assertTrue(crafter.getItemInv().get(patternSlot).is(Items.OAK_PLANKS), "Crafter pattern did not round-trip");
        insertInput(test, "machine_crafter", new ItemStack(Items.OAK_PLANKS));
        test.succeedWhen(() -> test.assertTrue(crafter.getItemInv().get(9).is(Items.OAK_BUTTON),
                "Crafter has not made an oak button from its configured pattern"));
    }

    @GameTest(template = "machines", timeoutTicks = 180)
    public static void stirlingDynamoGeneratesEnergy(GameTestHelper test) {
        test.setBlock(MACHINE, BuiltInRegistries.BLOCK.getOptional(thermal("dynamo_stirling")).orElseThrow());
        insertInput(test, "dynamo_stirling", new ItemStack(Items.COAL));
        var pos = test.absolutePos(MACHINE);
        var level = test.getLevel();
        var facing = level.getBlockState(pos).getValue(FACING_ALL);
        var energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, facing);
        test.assertTrue(energy != null, "Missing energy output capability on the dynamo face");
        test.succeedWhen(() -> test.assertTrue(energy.extractEnergy(1, true) == 1, "Dynamo has not generated extractable energy"));
    }

    @GameTest(template = "machines", batch = "datapackReload", timeoutTicks = 400)
    public static void refreshesMachineRecipesAfterReload(GameTestHelper test) {
        var server = test.getLevel().getServer();
        var manager = FurnaceRecipeManager.instance();
        var originalRecipe = manager.getConvertedRecipes().getFirst();
        var reload = server.reloadResources(server.getPackRepository().getSelectedIds());
        test.succeedWhen(() -> {
            test.assertTrue(reload.isDone(), "Datapack reload is still running");
            reload.join();
            test.assertTrue(!manager.getConvertedRecipes().isEmpty() && manager.getConvertedRecipes().getFirst() != originalRecipe,
                    "The furnace still has its pre-reload recipe cache");
        });
    }

    private static void putMachine(GameTestHelper test, String name, ItemStack input) {
        placeAndPowerMachine(test, name);
        insertInput(test, name, input);
    }

    private static void placeAndPowerMachine(GameTestHelper test, String name) {
        test.setBlock(MACHINE, BuiltInRegistries.BLOCK.getOptional(thermal(name)).orElseThrow());
        var level = test.getLevel();
        var pos = test.absolutePos(MACHINE);
        var machine = (MachineBlockEntity) level.getBlockEntity(pos);
        machine.reconfigControl().setSideConfig(Direction.UP, SIDE_INPUT);
        var energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, Direction.UP);
        test.assertTrue(energy != null, "Missing energy capability for " + name);
        for (int i = 0; i < 32; ++i) {
            energy.receiveEnergy(20_000, false);
        }
        test.assertTrue(energy.getEnergyStored() >= 2_000, "Couldn't power " + name);
    }

    private static void insertInput(GameTestHelper test, String name, ItemStack input) {
        var level = test.getLevel();
        var pos = test.absolutePos(MACHINE);
        for (Direction face : Direction.values()) {
            var items = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);
            if (items != null && items.getSlots() > 0 && items.insertItem(0, input, true).isEmpty()) {
                test.assertTrue(items.insertItem(0, input, false).isEmpty(), "Couldn't insert " + input + " into " + name);
                return;
            }
        }
        test.fail("No item input accepted " + input + " for " + name);
    }

    private static ResourceLocation thermal(String path) {
        return ResourceLocation.fromNamespaceAndPath("thermal", path);
    }
}
