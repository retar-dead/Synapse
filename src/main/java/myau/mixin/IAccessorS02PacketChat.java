package myau.mixin;

import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SideOnly(Side.CLIENT)
@Mixin({S02PacketChat.class})
public interface IAccessorS02PacketChat {
    @Accessor("chatComponent")
    void setChatComponent(IChatComponent chatComponent);
}
