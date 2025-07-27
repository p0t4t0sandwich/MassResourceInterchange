package dev.neuralnexus.mri.neoforge.events;

import dev.neuralnexus.mri.TypeRegistry;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class RegisterTypesEvent extends Event {
    public TypeRegistry registry() {
        return TypeRegistry.getInstance();
    }
}
