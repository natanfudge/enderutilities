package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.inventory.container.ContainerASU;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;

public class GuiASU extends GuiContainerLargeStacks
{
    private final TileEntityASU teasu;

    public GuiASU(ContainerASU container, TileEntityASU te)
    {
        super(container, 176, 139, "gui.container.asu");

        this.teasu = te;
        this.infoArea = new InfoArea(160, 5, 11, 11, "enderutilities.gui.infoarea.asu");
        this.scaledStackSizeTextInventories.add(container.inventory);
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
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.asu"), 8, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 46, 0x404040);

        String str = String.valueOf(this.teasu.getBaseItemHandler().getInventoryStackLimit());
        this.fontRendererObj.drawString(str, 116 - this.fontRendererObj.getStringWidth(str), 16, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw the slot backgrounds according to how many slots this tier has
        this.drawTexturedModalRect(x + 7, y + 26, 7, 56, this.teasu.getInvSize() * 18, 18);

        this.drawLockedSlotBackgrounds(this.teasu.getInventoryASU());
        this.drawTemplateStacks(this.teasu.getInventoryASU());
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.buttonList.add(new GuiButtonHoverText(0, x + 120, y + 16, 8, 8, 0, 120,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.stacklimit"));

        this.buttonList.add(new GuiButtonHoverText(1, x + 138, y + 16, 8, 8, 0, 120,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.slot.amount"));
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
    {
        int dim = this.teasu.getWorld().provider.getDimension();
        int amount = 0;

        if (mouseButton == 0 || mouseButton == 11)
        {
            amount = 1;
        }
        else if (mouseButton == 1 || mouseButton == 9)
        {
            amount = -1;
        }

        if (button.id == 0 || button.id == 1)
        {
            if (button.id == 0 && mouseButton == 2)
            {
                amount = GuiScreen.isShiftKeyDown() ? TileEntityASU.MAX_STACK_SIZE : -TileEntityASU.MAX_STACK_SIZE;
            }

            if (GuiScreen.isShiftKeyDown()) { amount *= 16; }
            if (GuiScreen.isCtrlKeyDown())  { amount *= 64; }

            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.teasu.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, button.id, amount));
        }
    }
}
