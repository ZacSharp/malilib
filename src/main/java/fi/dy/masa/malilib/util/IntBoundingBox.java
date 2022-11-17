package fi.dy.masa.malilib.util;

import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;

public class IntBoundingBox
{
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;

    public IntBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean containsPos(Vector3i pos)
    {
        return pos.getX() >= this.minX &&
               pos.getX() <= this.maxX &&
               pos.getZ() >= this.minZ &&
               pos.getZ() <= this.maxZ &&
               pos.getY() >= this.minY &&
               pos.getY() <= this.maxY;
    }

    public boolean intersects(IntBoundingBox box)
    {
        return this.maxX >= box.minX &&
               this.minX <= box.maxX &&
               this.maxZ >= box.minZ &&
               this.minZ <= box.maxZ &&
               this.maxY >= box.minY &&
               this.minY <= box.maxY;
    }

    public MutableBoundingBox toVanillaBox()
    {
        return new MutableBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public IntArrayNBT toNBTIntArray()
    {
        return new IntArrayNBT(new int[] { this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ });
    }

    public static IntBoundingBox fromVanillaBox(MutableBoundingBox box)
    {
        return createProper(box.x0, box.y0, box.z0, box.x1, box.y1, box.z1);
    }

    public static IntBoundingBox createProper(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        return new IntBoundingBox(
                Math.min(x1, x2),
                Math.min(y1, y2),
                Math.min(z1, z2),
                Math.max(x1, x2),
                Math.max(y1, y2),
                Math.max(z1, z2));
    }

    public static IntBoundingBox fromArray(int[] coords)
    {
        if (coords.length == 6)
        {
            return new IntBoundingBox(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
        }
        else
        {
            return new IntBoundingBox(0, 0, 0, 0, 0, 0);
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.maxX;
        result = prime * result + this.maxY;
        result = prime * result + this.maxZ;
        result = prime * result + this.minX;
        result = prime * result + this.minY;
        result = prime * result + this.minZ;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass())
        {
            return false;
        }

        IntBoundingBox other = (IntBoundingBox) obj;

        return this.maxX == other.maxX &&
               this.maxY == other.maxY &&
               this.maxZ == other.maxZ &&
               this.minX == other.minX &&
               this.minY == other.minY &&
               this.minZ == other.minZ;
    }
}
