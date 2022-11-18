package fi.dy.masa.malilib.util;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.server.level.ServerLevel;

public class WorldUtils
{
    public static String getDimensionId(Level world)
    {
        ResourceLocation id = world.dimension().location();
        return id != null ? id.getNamespace() + "_" + id.getPath() : "__fallback";
    }

    /**
     * Best name. Returns the integrated server world for the current dimension
     * in single player, otherwise just the client world.
     * @param mc
     * @return
     */
    @Nullable
    public static Level getBestWorld(Minecraft mc)
    {
        IntegratedServer server = mc.getSingleplayerServer();

        if (mc.level != null && server != null)
        {
            return server.getLevel(mc.level.dimension());
        }
        else
        {
            return mc.level;
        }
    }

    /**
     * Returns the requested chunk from the integrated server, if it's available.
     * Otherwise returns the client world chunk.
     * @param chunkX
     * @param chunkZ
     * @param mc
     * @return
     */
    @Nullable
    public static LevelChunk getBestChunk(int chunkX, int chunkZ, Minecraft mc)
    {
        IntegratedServer server = mc.getSingleplayerServer();
        LevelChunk chunk = null;

        if (mc.level != null && server != null)
        {
            ServerLevel world = server.getLevel(mc.level.dimension());

            if (world != null)
            {
                chunk = world.getChunk(chunkX, chunkZ);
            }
        }

        if (chunk != null)
        {
            return chunk;
        }

        return mc.level != null ? mc.level.getChunk(chunkX, chunkZ) : null;
    }
}
