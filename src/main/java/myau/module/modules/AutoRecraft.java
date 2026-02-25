package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class AutoRecraft extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final IntProperty startMinDelay = new IntProperty("start-min-delay", 30, 1, 400);
    public final IntProperty startMaxDelay = new IntProperty("start-max-delay", 42, 1, 400);
    public final IntProperty recraftMinDelay = new IntProperty("recraft-min-delay", 30, 1, 400);
    public final IntProperty recraftMaxDelay = new IntProperty("recraft-max-delay", 42, 1, 400);
    public final IntProperty startWith = new IntProperty("start-with", 3, 0, 41);
    public final BooleanProperty autoclose = new BooleanProperty("autoclose", false);
    public final BooleanProperty cactusMode = new BooleanProperty("cactus", false);
    public final BooleanProperty cocoaMode = new BooleanProperty("cocoa", true);
    public final BooleanProperty mushroomMode = new BooleanProperty("mushroom", true);
    public final ModeProperty sortMode = new ModeProperty("sort-by", 0, new String[]{"Size", "Index"});

    private final TimerUtil startTimer = new TimerUtil();
    private final TimerUtil recraftTimer = new TimerUtil();

    private final HashMap<String, Integer> recraftMap = new HashMap<>();
    public boolean start = false;
    private long recraftDelay = RandomUtils.nextLong(30L, 42L);
    private int step = 1;

    public AutoRecraft() {
        super("AutoRecraft", false);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        reset();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null || event.getType() != EventType.PRE) {
            return;
        }

        if (!(mc.currentScreen instanceof GuiInventory)) {
            reset();
            return;
        }

        if (start) {
            if (!recraftMap.isEmpty()) {
                if (recraftTimer.hasTimeElapsed(recraftDelay)) {
                    recraftTimer.reset();
                    recraftDelay = RandomUtils.nextLong(
                            Math.min(recraftMinDelay.getValue(), recraftMaxDelay.getValue()),
                            Math.max(recraftMinDelay.getValue(), recraftMaxDelay.getValue())
                    );

                    if (recraftMap.size() == 2) { // cactus / cocoa
                        switch (step) {
                            case 1:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, recraftMap.get("bowl"), 1, 0, mc.thePlayer);
                                step++;
                                break;
                            case 2:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 1, 0, 0, mc.thePlayer);
                                step++;
                                break;
                            case 3:
                                if (cactusMode.getValue() && recraftMap.containsKey("cactus")) {
                                    mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, recraftMap.get("cactus"), 1, 0, mc.thePlayer);
                                } else if (cocoaMode.getValue() && recraftMap.containsKey("cocoa")) {
                                    mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, recraftMap.get("cocoa"), 1, 0, mc.thePlayer);
                                }
                                step++;
                                break;
                            case 4:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 2, 0, 0, mc.thePlayer);
                                step++;
                                break;
                            case 5:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 0, 0, 1, mc.thePlayer);
                                step++;
                                break;
                            case 6:
                                if (mc.thePlayer.inventoryContainer.getSlot(2).getStack() == null) {
                                    step++;
                                    break;
                                }
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 2, 0, 1, mc.thePlayer);
                                break;
                            case 7:
                                if (mc.thePlayer.inventoryContainer.getSlot(1).getStack() == null) {
                                    step++;
                                    break;
                                }
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 1, 0, 1, mc.thePlayer);
                                break;
                            case 8:
                                reset();
                                break;
                        }
                    } else if (recraftMap.size() == 3) { // mushroom
                        switch (step) {
                            case 1:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, recraftMap.get("bowl"), 1, 0, mc.thePlayer);
                                step++;
                                break;
                            case 2:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 1, 0, 0, mc.thePlayer);
                                step++;
                                break;
                            case 3:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, recraftMap.get("red"), 1, 0, mc.thePlayer);
                                step++;
                                break;
                            case 4:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 2, 0, 0, mc.thePlayer);
                                step++;
                                break;
                            case 5:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, recraftMap.get("brown"), 1, 0, mc.thePlayer);
                                step++;
                                break;
                            case 6:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 3, 0, 0, mc.thePlayer);
                                step++;
                                break;
                            case 7:
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 0, 0, 1, mc.thePlayer);
                                step++;
                                break;
                            case 8:
                                if (mc.thePlayer.inventoryContainer.getSlot(3).getStack() == null) {
                                    step++;
                                    break;
                                }
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 3, 0, 1, mc.thePlayer);
                                break;
                            case 9:
                                if (mc.thePlayer.inventoryContainer.getSlot(2).getStack() == null) {
                                    step++;
                                    break;
                                }
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 2, 0, 1, mc.thePlayer);
                                break;
                            case 10:
                                if (mc.thePlayer.inventoryContainer.getSlot(1).getStack() == null) {
                                    step++;
                                    break;
                                }
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 1, 0, 1, mc.thePlayer);
                                break;
                            case 11:
                                reset();
                                break;
                        }
                    }
                }
            } else {
                reset();
            }
        } else {
            if (getTotalSoupsInInventory() <= startWith.getValue()) {
                long startDelay = RandomUtils.nextLong(
                        Math.min(startMinDelay.getValue(), startMaxDelay.getValue()),
                        Math.max(startMinDelay.getValue(), startMaxDelay.getValue())
                );
                if (startTimer.hasTimeElapsed(startDelay)) {
                    startTimer.reset();
                    if (cactusMode.getValue() && hasCactusRecraft()) {
                        getRecraft(1);
                    } else if (cocoaMode.getValue() && hasCocoaRecraft()) {
                        getRecraft(2);
                    } else if (mushroomMode.getValue() && hasMushroomRecraft()) {
                        getRecraft(3);
                    }
                    recraftDelay = RandomUtils.nextLong(
                            Math.min(recraftMinDelay.getValue(), recraftMaxDelay.getValue()),
                            Math.max(recraftMinDelay.getValue(), recraftMaxDelay.getValue())
                    );
                    start = true;
                }
            }
        }
    }

    private int getTotalSoupsInInventory() {
        int counter = 0;
        for (int i = 9; i < 45; i++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemSoup) {
                counter++;
            }
        }
        return counter;
    }

    private boolean hasCocoaRecraft() {
        AtomicBoolean bowl = new AtomicBoolean(false);
        AtomicBoolean cocoa = new AtomicBoolean(false);

        Arrays.stream(mc.thePlayer.inventory.mainInventory).forEach(itemStack -> {
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemDye) {
                    if (EnumDyeColor.byDyeDamage(itemStack.getMetadata()) == EnumDyeColor.BROWN) {
                        cocoa.set(true);
                    }
                } else if (itemStack.getItem() == Items.bowl) {
                    bowl.set(true);
                }
            }
        });

        return bowl.get() && cocoa.get();
    }

    private boolean hasMushroomRecraft() {
        AtomicBoolean bowl = new AtomicBoolean(false);
        AtomicBoolean red_mushroom = new AtomicBoolean(false);
        AtomicBoolean brown_mushroom = new AtomicBoolean(false);

        Arrays.stream(mc.thePlayer.inventory.mainInventory).forEach(itemStack -> {
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                    if (block == Blocks.red_mushroom) {
                        red_mushroom.set(true);
                    } else if (block == Blocks.brown_mushroom) {
                        brown_mushroom.set(true);
                    }
                } else if (itemStack.getItem() == Items.bowl) {
                    bowl.set(true);
                }
            }
        });

        return bowl.get() && red_mushroom.get() && brown_mushroom.get();
    }

    private boolean hasCactusRecraft() {
        AtomicBoolean bowl = new AtomicBoolean(false);
        AtomicBoolean cactus = new AtomicBoolean(false);

        Arrays.stream(mc.thePlayer.inventory.mainInventory).forEach(itemStack -> {
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                    if (block == Blocks.cactus) {
                        cactus.set(true);
                    }
                } else if (itemStack.getItem() == Items.bowl) {
                    bowl.set(true);
                }
            }
        });

        return bowl.get() && cactus.get();
    }

    /**
     * modes:
     * 1 - cactus
     * 2 - cocoa
     * 3 - mushroom
     */
    private void getRecraft(int mode) {
        HashMap<Integer, String> itemSlotMap = new HashMap<>();

        for (int i = 9; i < 45; i++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemStack != null) {
                switch (mode) {
                    case 1:
                        if (itemStack.getItem() == Items.bowl) {
                            itemSlotMap.put(i, "bowl");
                        } else if (itemStack.getItem() instanceof ItemBlock) {
                            Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                            if (block == Blocks.cactus) {
                                itemSlotMap.put(i, "cactus");
                            }
                        }
                        break;
                    case 2:
                        if (itemStack.getItem() == Items.bowl) {
                            itemSlotMap.put(i, "bowl");
                        } else if (itemStack.getItem() instanceof ItemDye) {
                            if (EnumDyeColor.byDyeDamage(itemStack.getMetadata()) == EnumDyeColor.BROWN) {
                                itemSlotMap.put(i, "cocoa");
                            }
                        }
                        break;
                    case 3:
                        if (itemStack.getItem() == Items.bowl) {
                            itemSlotMap.put(i, "bowl");
                        } else if (itemStack.getItem() instanceof ItemBlock) {
                            Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                            if (block == Blocks.red_mushroom) {
                                itemSlotMap.put(i, "red");
                            } else if (block == Blocks.brown_mushroom) {
                                itemSlotMap.put(i, "brown");
                            }
                        }
                        break;
                }
            }
        }

        Stream<Map.Entry<Integer, String>> itemSlotStream = itemSlotMap.entrySet().stream();

        if ("Size".equalsIgnoreCase(this.sortMode.getModeString())) {
            itemSlotStream = itemSlotStream.sorted(Comparator.comparingInt(entrySet ->
                    mc.thePlayer.inventoryContainer.getSlot(entrySet.getKey()).getStack().stackSize));
        } else {
            itemSlotStream = itemSlotStream.sorted(Map.Entry.comparingByKey((e1, e2) -> e2 - e1));
        }

        itemSlotStream.forEach(entrySet -> recraftMap.put(entrySet.getValue(), entrySet.getKey()));
    }

    private void reset() {
        recraftTimer.reset();
        startTimer.reset();
        recraftMap.clear();
        start = false;
        step = 1;
        recraftDelay = 0;
        if (autoclose.getValue() && mc.currentScreen instanceof GuiInventory) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{sortMode.getModeString()};
    }
}

