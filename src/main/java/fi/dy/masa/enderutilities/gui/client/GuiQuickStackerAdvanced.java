package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonIcon;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStackerAdvanced;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityQuickStackerAdvanced;

public class GuiQuickStackerAdvanced extends GuiEnderUtilities implements IButtonStateCallback
{
    private final TileEntityQuickStackerAdvanced teqsa;

    public GuiQuickStackerAdvanced(ContainerQuickStackerAdvanced container, TileEntityQuickStackerAdvanced te)
    {
        super(container, 192, 256, "gui.container." + te.getTEName());

        this.infoArea = new InfoArea(176, 4, 11, 11, "enderutilities.gui.infoarea." + te.getTEName());
        this.teqsa = te;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String str = this.teqsa.hasCustomName() ? this.teqsa.getName() : I18n.format(this.teqsa.getName());
        this.fontRenderer.drawString(str, this.xSize / 2 - this.fontRenderer.getStringWidth(str) / 2, 5, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        if (this.teqsa.isAreaMode() == false)
        {
            int index = this.teqsa.getSelectedTarget();
            // Draw the selection border around the active Link Crystals' selection button
            this.drawTexturedModalRect(x + 26 + index * 18, y + 53, 120, 0, 10, 10);
        }

        int enabledTargets = this.teqsa.getEnabledTargetsMask();
        long bit = 0x1;

        for (int slotNum = 0, dx = 22, dy = 16; slotNum < TileEntityQuickStackerAdvanced.NUM_TARGET_INVENTORIES * 2; slotNum++)
        {
            if (this.teqsa.isAreaMode())
            {
                // Module slots are disabled in Area mode
                this.drawTexturedModalRect(x + dx, y + dy, 102, 0, 18, 18);
            }
            else
            {
                if ((enabledTargets & bit) != 0)
                {
                    // Draw the green background for active Link Crystal and Memory Card slots
                    this.drawTexturedModalRect(x + dx, y + dy     , 102, 54, 18, 18);
                    this.drawTexturedModalRect(x + dx, y + dy + 18, 102, 54, 18, 18);
                }

                Slot slot = this.inventorySlots.getSlot(slotNum);

                // Draw the module type background to empty module slots
                if (slot instanceof SlotItemHandlerModule && slot.getHasStack() == false)
                {
                    if (((SlotItemHandlerModule) slot).getModuleType() == ModuleType.TYPE_INVALID)
                    {
                        this.drawTexturedModalRect(x + dx + 1, y + dy + 1, 102, 0, 18, 18);
                    }
                    else
                    {
                        // Draw a darker background for the disabled slots, and the module type background for enabled slots
                        int u = ((SlotItemHandlerModule) slot).getBackgroundIconU();
                        int v = ((SlotItemHandlerModule) slot).getBackgroundIconV();

                        // Only one type of module is allowed in this slot
                        if (u >= 0 && v >= 0)
                        {
                            this.drawTexturedModalRect(x + dx + 1, y + dy + 1, u, v, 16, 16);
                        }
                    }
                }
            }

            bit <<= 1;
            dx += 18;

            if (slotNum == 8)
            {
                dx = 22;
                dy += 18;
            }
        }

        int posX = x + 22;
        int posY = y + 82;

        // Non-accessible filter inventory, draw the dark background
        if (this.teqsa.isInventoryAccessible(this.player) == false)
        {
            for (int r = 0; r < 4; r++)
            {
                for (int c = 0; c < 9; c++)
                {
                    this.drawTexturedModalRect(posX + c * 18, posY + r * 18, 102, 0, 18, 18);
                }
            }
        }

        // Draw a blue background for the enabled player inventory slots
        posX = x + 22;
        posY = y + 231;
        long mask = this.teqsa.getEnabledSlotsMask();
        bit = 0x1;

        // Hotbar
        for (int c = 0; c < 9; c++)
        {
            if ((mask & bit) != 0)
            {
                this.drawTexturedModalRect(posX + c * 18, posY, 102, 18, 18, 18);
            }
            bit <<= 1;
        }

        posY = y + 173;
        // Inventory
        for (int r = 0; r < 3; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(posX + c * 18, posY + r * 18, 102, 18, 18, 18);
                }
                bit <<= 1;
            }
        }

        // Offhand slot
        bit = 1L << 40;
        if ((mask & bit) != 0)
        {
            this.drawTexturedModalRect(x + 4, y + 155, 102, 18, 18, 18);
        }
    }

    protected void addConditionalButton(int id, int x, int y, int w, int h, ButtonState... states)
    {
        this.buttonList.add(new GuiButtonStateCallback(id, x, y, w, h, w, 0, this.guiTextureWidgets, this, states));
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        int id = 0;
        // Add the Link Crystal selection buttons
        for (int i = 0; i < TileEntityQuickStackerAdvanced.NUM_TARGET_INVENTORIES; i++)
        {
            GuiButton button = new GuiButtonIcon(id++, x + 27 + i * 18, y + 54, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0);
            if (this.teqsa.isAreaMode())
            {
                button.enabled = false;
            }

            this.buttonList.add(button);
        }

        // Add the column selection buttons
        for (int i = 0; i < 9; i++)
        {
            this.buttonList.add(new GuiButtonIcon(id++, x + 24 + i * 18, y + 157, 14, 14, 60, 42, this.guiTextureWidgets, 14, 0));
        }

        // Add the row selection buttons, hotbar is first
        this.buttonList.add(new GuiButtonIcon(id++, x + 6, y + 233, 14, 14, 60, 28, this.guiTextureWidgets, 14, 0));

        for (int i = 0; i < 3; i++)
        {
            this.buttonList.add(new GuiButtonIcon(id++, x + 6, y + 175 + i * 18, 14, 14, 60, 28, this.guiTextureWidgets, 14, 0));
        }

        // Add the Link Crystals vs. Area toggle button
        this.addConditionalButton(id++, x + 6, y + 36, 14, 14,
                ButtonState.createTranslate(60, 28, "enderutilities.gui.label.use.linkcrystaltargets"),
                ButtonState.createTranslate(60, 168, "enderutilities.gui.label.use.areablock"));

        // Match or ignore this group of filters
        this.addConditionalButton(id++, x + 24, y + 66, 14, 14,
                ButtonState.createTranslate(60, 98, "enderutilities.gui.label.filters.disabled"),
                ButtonState.createTranslate(60, 28, "enderutilities.gui.label.filters.enabled"));

        // Blacklist or Whitelist
        this.addConditionalButton(id++, x + 42, y + 66, 14, 14,
                ButtonState.createTranslate(60, 84, "enderutilities.gui.label.whitelist"),
                ButtonState.createTranslate(60, 70, "enderutilities.gui.label.blacklist"));

        // Match or ignore damage/metadata
        this.addConditionalButton(id++, x + 60, y + 66, 14, 14,
                ButtonState.createTranslate(60, 126, "enderutilities.gui.label.meta.ignore"),
                ButtonState.createTranslate(60, 112, "enderutilities.gui.label.meta.match"));

        // Match or ignore NBT
        this.addConditionalButton(id++, x + 78, y + 66, 14, 14,
                ButtonState.createTranslate(60, 154, "enderutilities.gui.label.nbt.ignore"),
                ButtonState.createTranslate(60, 140, "enderutilities.gui.label.nbt.match"));
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
    {
        int action = 0;
        int element = 0;
        boolean valid = true;
        int dim = this.teqsa.getWorld().provider.getDimension();
        BlockPos pos = this.teqsa.getPos();

        // Toggle targets on/off
        if (button.id >= 0 && button.id < 9)
        {
            action = mouseButton == 1 ? TileEntityQuickStackerAdvanced.GUI_ACTION_TOGGLE_TARGET_ENABLED :
                TileEntityQuickStackerAdvanced.GUI_ACTION_SET_ACTIVE_TARGET;
            element = button.id;
        }
        // Toggle column selection
        else if (button.id >= 9 && button.id < 18)
        {
            action = TileEntityQuickStackerAdvanced.GUI_ACTION_TOGGLE_COLUMNS;
            element = button.id - 9;
        }
        // Toggle row selection
        else if (button.id >= 18 && button.id < 22)
        {
            action = TileEntityQuickStackerAdvanced.GUI_ACTION_TOGGLE_ROWS;
            element = button.id - 18;
        }
        // Toggle Link Crystal targets vs. Area
        else if (button.id == 22)
        {
            action = TileEntityQuickStackerAdvanced.GUI_ACTION_TOGGLE_TARGET_TYPE;
        }
        // Toggle filter settings
        else if (button.id >= 23 && button.id < 27)
        {
            action = TileEntityQuickStackerAdvanced.GUI_ACTION_TOGGLE_FILTER_SETTINGS;
            element = button.id - 23;
        }
        else
        {
            valid = false;
        }

        if (valid)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, pos,
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, action, element));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        switch (callbackId)
        {
            case 22: return this.teqsa.isAreaMode() ? 1 : 0;
            case 23: return this.teqsa.getSelectedFilterSettings().isEnabled() ? 1 : 0;
            case 24: return this.teqsa.getSelectedFilterSettings().isBlacklist() ? 1 : 0;
            case 25: return this.teqsa.getSelectedFilterSettings().getMatchMeta() ? 1 : 0;
            case 26: return this.teqsa.getSelectedFilterSettings().getMatchNBT() ? 1 : 0;
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        // Disable the filter mode buttons if there is no Memory Card present
        if (callbackId >= 23 && callbackId <= 26)
        {
            return this.teqsa.isInventoryAccessible(this.player);
        }

        return true;
    }
}
