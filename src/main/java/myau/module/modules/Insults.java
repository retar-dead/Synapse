package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.module.Module;
import myau.util.ChatUtil;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.Random;

public class Insults extends Module {
    private static final Random RANDOM = new Random();

    private static final String[] INSULTS = new String[]{
            "%s https://forum.mush.com.br/como-parar-de-morrer-na-arena",
            "%s Entre você tentar me matar e desistir eu prefiro que você se mate",
            "Espelho, espelho meu, existe alguém pior que o %s?",
            "Granada, C4, TNT, Dinamite, Bomba Caseira, Bomba Relógio, Mina Terrestre, HE do CS",
            "Use Adjust today! %s",
            "Use Paroxide today! %s",
            "Use Atmosphere today! %s",
            "Use ꜱｋｕｕｒ today! %s",
            "Use Myau today! %s",
            "Use Augustus today! %s",
            "Use Breeze today! %s",
            "Use slinky today! %s",
            "Use Drip today! %s",
            "Use Neon today! %s",
            "Use rise today! %s",
            "Use RavenBfun today! %s",
            "Use Huzuni today! %s",
            "Use Wurst today! %s",
            "%s Como foi sua estadia na ilha Epstein?",
            "Pra você se matar é sempre uma opção %s",
            "%s abre as perna pra minha gang lil hoe",
            "%s vi seu nickname no doxbin recentemente",
            "%s eu te vi na 696 ontem a noite, como foi o lulz?",
            "Sempre utilize ɢᴇᴛᴍᴏɴᴇｒᴏ.ᴏʀɢ %s",
            "Como foi ter o pai assassinado na operação da Penha? %s",
            "%s como é webnamorar em 2026?",
            "Já entrou na Crackcrew? %s",
            "Já entrou na Karoushi? %s",
            "Já entrou na Rato Cheats? %s",
            "%s this account has been provided by ｖｏｄｌᴇｘ",
            "%s Que a lâmina da razão corte teus sonhos, e teu nome seja apagado pelo vento do vazio.",
            "%s parece que você esta jogando sem cabeça hoje, bem vindo de volta Kurt Cobain.",
            "ChatGPT me informe uma pessoa pior do que o %s",
            "O bullying evita situações igual as do %s",
            "Você é membro do clan Biblia? %s",
            "W MushMC %s",
            "%s porque você insiste em ficar vivo?",
            "Prefiro me matar do que jogar igual você %s",
            "%s você é um desperdício de memória RAM",
            "%s prefere no pé ou na mão?",
            "%s falar \"mds\" não vai te ajudar.",
            "Ainda bem que você morreu %s, criança cocuda do caralho.",
            "%s eu tenho uma coleção de macacos da Macacolândia!",
            "Wow you just got raped! %s"
    };

    public Insults() {
        super("Insults", false);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled()) return;
        if (event.getType() != EventType.RECEIVE) return;
        if (!(event.getPacket() instanceof S02PacketChat)) return;

        S02PacketChat packet = (S02PacketChat) event.getPacket();
        String msg = packet.getChatComponent().getUnformattedText();
        if (msg == null) return;

        // Procurar mensagem de morte do tipo: "MORTE Você matou {player}."
        if (!msg.contains("MORTE") || !msg.contains("Você matou ")) return;

        int idx = msg.indexOf("Você matou ");
        if (idx < 0) return;

        String after = msg.substring(idx + "Você matou ".length()).trim();
        if (after.isEmpty()) return;

        // Pega primeiro token como nick e remove pontuação final
        String player = after.split(" ")[0].replaceAll("[^A-Za-z0-9_]", "");
        if (player.isEmpty()) return;

        String template = INSULTS[RANDOM.nextInt(INSULTS.length)];
        String message = template.replace("%s", player);

        ChatUtil.sendMessage(message);
    }
}

