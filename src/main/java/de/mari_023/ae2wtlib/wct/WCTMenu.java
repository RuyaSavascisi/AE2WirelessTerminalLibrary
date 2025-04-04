package de.mari_023.ae2wtlib.wct;

import static de.mari_023.ae2wtlib.api.AE2wtlibAPI.id;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

import appeng.api.networking.IGridNode;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantic;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;

import de.mari_023.ae2wtlib.api.gui.AE2wtlibSlotSemantics;
import de.mari_023.ae2wtlib.api.terminal.ItemWUT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetHandler;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetMenu;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetMode;

public class WCTMenu extends CraftingTermMenu {
    public static final ResourceLocation ID = id("wireless_crafting_terminal");
    public static final MenuType<WCTMenu> TYPE = MenuTypeBuilder.create(WCTMenu::new, WCTMenuHost.class)
            .buildUnregistered(ID);

    public static final String MAGNET_MODE = "magnetMode";
    public static final String MAGNET_MENU = "magnetMenu";
    public static final String TRASH_MENU = "trash";

    private final WCTMenuHost wctMenuHost;

    public WCTMenu(int id, final Inventory ip, final WCTMenuHost gui) {
        super(TYPE, id, ip, gui, true);
        wctMenuHost = gui;

        addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.HEAD) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return (stack.getItem() instanceof BlockItem bItem && bItem.getBlock() instanceof Equipable)
                        || super.mayPlace(stack);
            }
        }, AE2wtlibSlotSemantics.HELMET);
        addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.CHEST), AE2wtlibSlotSemantics.CHESTPLATE);
        addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.LEGS), AE2wtlibSlotSemantics.LEGGINGS);
        addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.FEET), AE2wtlibSlotSemantics.BOOTS);

        if (Integer.valueOf(Inventory.SLOT_OFFHAND).equals(wctMenuHost.getPlayerInventorySlot()))
            addSlot(new ArmorSlot.DisabledOffhandSlot(getPlayerInventory()), AE2wtlibSlotSemantics.OFFHAND);
        else
            addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.OFFHAND), AE2wtlibSlotSemantics.OFFHAND);
        addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.QE_SINGULARITY,
                wctMenuHost.getSubInventory(WTMenuHost.INV_SINGULARITY), 0), AE2wtlibSlotSemantics.SINGULARITY);

        registerClientAction(MAGNET_MODE, MagnetMode.class, this::setMagnetMode);
        registerClientAction(MAGNET_MENU, this::openMagnetMenu);
        registerClientAction(TRASH_MENU, this::openTrashMenu);
    }

    @Override
    public IGridNode getGridNode() {
        return wctMenuHost.getActionableNode();
    }

    public MagnetMode getMagnetMode() {
        return MagnetHandler.getMagnetMode(wctMenuHost.getItemStack());
    }

    public void setMagnetMode(MagnetMode mode) {
        if (isClientSide())
            sendClientAction(MAGNET_MODE, mode);
        MagnetHandler.saveMagnetMode(wctMenuHost.getItemStack(), mode);
    }

    public void openMagnetMenu() {
        if (isClientSide()) {
            sendClientAction(MAGNET_MENU);
            return;
        }
        MenuOpener.open(MagnetMenu.TYPE, getPlayer(), getLocator());
    }

    public void openTrashMenu() {
        if (isClientSide()) {
            sendClientAction(TRASH_MENU);
            return;
        }
        MenuOpener.open(TrashMenu.TYPE, getPlayer(), getLocator());
    }

    @Override
    protected boolean canSlotsBeHidden(SlotSemantic semantic) {
        return semantic == AE2wtlibSlotSemantics.OFFHAND
                || semantic == AE2wtlibSlotSemantics.HELMET
                || semantic == AE2wtlibSlotSemantics.CHESTPLATE
                || semantic == AE2wtlibSlotSemantics.LEGGINGS
                || semantic == AE2wtlibSlotSemantics.BOOTS;
    }

    public boolean isWUT() {
        return wctMenuHost.getItemStack().getItem() instanceof ItemWUT;
    }
}
