package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.module.Module;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.Random;

public class Insults extends Module {
    private static final Random RANDOM = new Random();
    private static final Minecraft mc = Minecraft.getMinecraft();

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
            "Utilize monero em todas as suas transações %s",
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
            "Wow you just got raped! %s",
            "Vai virar pedreiro lil bro %s",
            "Deleta o game que é melhor %s",
            "Sai do fake Marciano Comunista %s",
            "Se for chorar manda audio %s",
            "Baixa um cheat na CrackCrew %s",
            "Você é o motivo do KanyeWest ter virado nazista %s",
            "Como foi a festa do Diddy? %s",
            "Seu pai chora no banho todo dia por causa de você %s",
            "Good Goy %s",
            "Benjamin Netanyahu está orgulhoso de você %s",
            "%s Foi conhecer o Charlie Kirk!",
            "%s Clicou ( ) Não Clicou (X)",
            "%s NUEVAYoL!!!",
            "%s Mirá, puñeta, no me quiten el perreo!",
            "%s, Não é pq o game é gratis que você tem que baixar...",
            "Matei um turco no rolas. %s",
            "Print:(minhas_costas_tao_doendo) %s",
            "%s mantenha o foco, você vai conseguir!",
            "A mira do %s agora é patrocinada pelo Parkinson!",
            "%s eu não estou usando reach, é só você clicar mais rápido",
            "Um milhão de anos de evolução, e temos pessoas como %s",
            "Isso foi realmente uma jogada muito ruim %s",
            "Você é a inspiração pro aborto %s",
            "%s você gosta tanto assim de morrer?",
            "Estou surpreso que você tenha conseguido clicar no botão de \"instalar\" %s",
            "A quantidade de kills que você tem é equivalente a suas células cerebrais %s",
            "É como se eu pudesse ouvir você chorando %s",
            "Synapse continua dando bypass no CrisAc #getbettermariaum %s",
            "Use Synapse today! %s",
            "Compre um vip para morrer pro Synapse Client! %s",
            "Porque tentar jogar em um servidor onde todos usam cheat %s?",
            "%s sua conta foi morta por não utilizar trapaças #getsynapsetoday",
            "%s porque você não pode ser igual o XandaoXisde?"
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

        if (!msg.contains("MORTE") || !msg.contains("Você matou ")) return;

        int idx = msg.indexOf("Você matou ");
        if (idx < 0) return;

        String after = msg.substring(idx + "Você matou ".length()).trim();
        if (after.isEmpty()) return;

        String player = after.split(" ")[0].replaceAll("[^A-Za-z0-9_]", "");
        if (player.isEmpty()) return;

        String template = INSULTS[RANDOM.nextInt(INSULTS.length)];
        String message = template.replace("%s", player);
        
        // Processar placeholders adicionais
        message = processPlaceholders(message);

        ChatUtil.sendMessage(message);
    }

    private String processPlaceholders(String message) {
        // %p = nome do próprio jogador
        if (message.contains("%p")) {
            String playerName = mc.thePlayer != null ? mc.thePlayer.getName() : "Unknown";
            message = message.replace("%p", playerName);
        }

        // %r = jogador aleatório da tablist
        if (message.contains("%r")) {
            String randomPlayer = getRandomPlayerFromTablist();
            message = message.replace("%r", randomPlayer);
        }

        return message;
    }

    private String getRandomPlayerFromTablist() {
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
            return "Alguém";
        }

        // Tenta pegar a lista de players do tablist
        try {
            Object tabListData = mc.thePlayer.sendQueue.getClass().getField("playerInfoMap").get(mc.thePlayer.sendQueue);
            if (tabListData instanceof java.util.Collection) {
                java.util.Collection<?> players = (java.util.Collection<?>) tabListData;
                if (players.isEmpty()) return "Alguém";

                int randomIndex = RANDOM.nextInt(players.size());
                Object[] playersArray = players.toArray();
                Object playerInfo = playersArray[randomIndex];

                // Tenta pegar o nome do jogador
                if (playerInfo != null) {
                    Object profile = playerInfo.getClass().getMethod("getProfile").invoke(playerInfo);
                    String name = (String) profile.getClass().getMethod("getName").invoke(profile);
                    return name != null ? name : "Alguém";
                }
            }
        } catch (Exception e) {
            // Fallback se não conseguir acessar a tablist
        }

        return "Alguém";
    }
}

