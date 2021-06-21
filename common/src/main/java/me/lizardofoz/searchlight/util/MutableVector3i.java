package me.lizardofoz.searchlight.util;

import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class MutableVector3i
{
    public int x;
    public int y;
    public int z;

    public MutableVector3i(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MutableVector3i(double x, double y, double z)
    {
        this(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public void set(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(@NotNull MutableVector3i input)
    {
        set(input.x, input.y, input.z);
    }

    public void set(@NotNull MutableVector3d currentPosVecD)
    {
        set(MathHelper.floor(currentPosVecD.x), MathHelper.floor(currentPosVecD.y), MathHelper.floor(currentPosVecD.z));
    }

    public boolean areSame(int x, int y, int z)
    {
        return this.x == x && this.y == y && this.z == z;
    }

    public boolean areSame(@NotNull MutableVector3i other)
    {
        return areSame(other.x, other.y, other.z);
    }
}