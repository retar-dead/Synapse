package myau.module.modules;

import myau.module.Module;

public class Teste extends Module {

    public Teste() {
        super("Teste", false);
    }

    @Override
    public void onEnabled() {
        // Módulo de teste - apenas para verificar se a categoria Mush funciona
    }

    @Override
    public void onDisabled() {
        // Módulo de teste
    }
}
