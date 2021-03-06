package fi.dy.masa.enderutilities.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class RenderEnderArrow<T extends EntityEnderArrow> extends RenderArrow<T>
{
    private static final ResourceLocation RESOURCE = new ResourceLocation(ReferenceTextures.getEntityTextureName(ReferenceNames.NAME_ENTITY_ENDER_ARROW));

    public RenderEnderArrow(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entityArrow)
    {
        return RESOURCE;
    }
}
