package fi.dy.masa.malilib.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public enum Quadrant
{
    NORTH_WEST,
    NORTH_EAST,
    SOUTH_WEST,
    SOUTH_EAST;

    public static Quadrant getQuadrant(BlockPos pos, Vector3d center)
    {
        return getQuadrant(pos.getX(), pos.getZ(), center);
    }

    public static Quadrant getQuadrant(int x, int z, Vector3d center)
    {
        // West
        if (x <= center.x)
        {
            // North
            if (z <= center.z)
            {
                return NORTH_WEST;
            }
            // South
            else
            {
                return SOUTH_WEST;
            }
        }
        // East
        else
        {
            // North
            if (z <= center.z)
            {
                return NORTH_EAST;
            }
            // South
            else
            {
                return SOUTH_EAST;
            }
        }
    }

    public static Quadrant getQuadrant(double x, double z, Vector3d center)
    {
        // West
        if (x <= center.x)
        {
            // North
            if (z <= center.z)
            {
                return NORTH_WEST;
            }
            // South
            else
            {
                return SOUTH_WEST;
            }
        }
        // East
        else
        {
            // North
            if (z <= center.z)
            {
                return NORTH_EAST;
            }
            // South
            else
            {
                return SOUTH_EAST;
            }
        }
    }
}