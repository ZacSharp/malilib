package fi.dy.masa.malilib.util;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class WorldUtils
{
    public static String getDimensionId(World world)
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
    public static World getBestWorld(Minecraft mc)
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
    public static Chunk getBestChunk(int chunkX, int chunkZ, Minecraft mc)
    {
        IntegratedServer server = mc.getSingleplayerServer();
        Chunk chunk = null;

        if (mc.level != null && server != null)
        {
            ServerWorld world = server.getLevel(mc.level.dimension());

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
