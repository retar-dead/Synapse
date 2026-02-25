package myau.mixin;

import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(Side.CLIENT)
@Mixin(value = GuiPlayerTabOverlay.class, priority = 9999)
public abstract class MixinGuiPlayerTabOverlay {

    @Redirect(
            method = "getPlayerName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/ScorePlayerTeam;formatPlayerName(Lnet/minecraft/scoreboard/Team;Ljava/lang/String;)Ljava/lang/String;"
            )
    )
    private String getPlayerNameFormat(Team team, String name) {
        // Temporariamente sem ClanTags: apenas retorna o nome formatado padrão
        return ScorePlayerTeam.formatPlayerName(team, name);
    }
}
