/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import com.mojang.math.Transformation;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Utils {
    public static final String TAG_ENTITY_POS = "Pos";

    public static Transformation createScaleTransform(Vector3f scale) {
        return new Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Quaternionf().identity(),
                scale,
                new Quaternionf().identity());
    }

    public static ListTag newDoubleList(double... doubles) {
        ListTag listtag = new ListTag();
        for (double d : doubles) {
            listtag.add(DoubleTag.valueOf(d));
        }
        return listtag;
    }
}
