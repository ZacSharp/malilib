package fi.dy.masa.malilib.util;

import java.util.Comparator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public class SubChunkPos extends Vector3i
{
    public SubChunkPos(BlockPos pos)
    {
        this(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }

    public SubChunkPos(int x, int y, int z)
    {
        super(x, y, z);
    }

    public static class DistanceComparator implements Comparator<SubChunkPos>
    {
        private final SubChunkPos referencePosition;

        public DistanceComparator(SubChunkPos referencePosition)
        {
            this.referencePosition = referencePosition;
        }

        @Override
        public int compare(SubChunkPos pos1, SubChunkPos pos2)
        {
            int x = this.referencePosition.getX();
            int y = this.referencePosition.getY();
            int z = this.referencePosition.getZ();

            double dist1 = pos1.distSqr(x, y, z, false);
            double dist2 = pos2.distSqr(x, y, z, false);

            return dist1 < dist2 ? -1 : (dist1 > dist2 ? 1 : 0);
        }
    }
}
