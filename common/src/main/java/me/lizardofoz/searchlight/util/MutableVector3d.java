package me.lizardofoz.searchlight.util;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class MutableVector3d
{
    public double x;
    public double y;
    public double z;

    public MutableVector3d(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(@NotNull Vec3d direction)
    {
        x += direction.x;
        y += direction.y;
        z += direction.z;
    }
}
