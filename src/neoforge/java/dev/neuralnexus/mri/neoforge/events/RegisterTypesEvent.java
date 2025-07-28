/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.events;

import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.TypeRegistry;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.ModLifecycleEvent;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class RegisterTypesEvent extends ModLifecycleEvent {
    public RegisterTypesEvent(ModContainer container) {
        super(container);
    }

    public TypeRegistry registry() {
        return MRIAPI.getInstance().typeRegistry();
    }
}
