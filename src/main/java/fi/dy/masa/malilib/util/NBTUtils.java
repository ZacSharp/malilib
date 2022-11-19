package fi.dy.masa.malilib.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

public class NBTUtils
{
    public static CompoundTag createBlockPosTag(Vec3i pos)
    {
        return writeBlockPosToTag(pos, new CompoundTag());
    }

    public static CompoundTag writeBlockPosToTag(Vec3i pos, CompoundTag tag)
    {
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    @Nullable
    public static BlockPos readBlockPos(@Nullable CompoundTag tag)
    {
        if (tag != null &&
            tag.contains("x", Constants.NBT.TAG_INT) &&
            tag.contains("y", Constants.NBT.TAG_INT) &&
            tag.contains("z", Constants.NBT.TAG_INT))
        {
            return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }

        return null;
    }

    public static CompoundTag writeVec3dToTag(Vec3 vec, CompoundTag tag)
    {
        tag.putDouble("dx", vec.x);
        tag.putDouble("dy", vec.y);
        tag.putDouble("dz", vec.z);
        return tag;
    }

    public static CompoundTag writeEntityPositionToTag(Vec3 pos, CompoundTag tag)
    {
        ListTag posList = new ListTag();

        posList.add(DoubleTag.valueOf(pos.x));
        posList.add(DoubleTag.valueOf(pos.y));
        posList.add(DoubleTag.valueOf(pos.z));
        tag.put("Pos", posList);

        return tag;
    }

    @Nullable
    public static Vec3 readVec3d(@Nullable CompoundTag tag)
    {
        if (tag != null &&
            tag.contains("dx", Constants.NBT.TAG_DOUBLE) &&
            tag.contains("dy", Constants.NBT.TAG_DOUBLE) &&
            tag.contains("dz", Constants.NBT.TAG_DOUBLE))
        {
            return new Vec3(tag.getDouble("dx"), tag.getDouble("dy"), tag.getDouble("dz"));
        }

        return null;
    }

    @Nullable
    public static Vec3 readEntityPositionFromTag(@Nullable CompoundTag tag)
    {
        if (tag != null && tag.contains("Pos", Constants.NBT.TAG_LIST))
        {
            ListTag tagList = tag.getList("Pos", Constants.NBT.TAG_DOUBLE);

            if (tagList.getElementType() == Constants.NBT.TAG_DOUBLE && tagList.size() == 3)
            {
                return new Vec3(tagList.getDouble(0), tagList.getDouble(1), tagList.getDouble(2));
            }
        }

        return null;
    }
}
