# Optifine Banner Module - Guia de Uso

## Descrição
O módulo Cape agora suporta renderização de banners do Optifine com gradiente de cores personalizável, assim como no site oficial do Optifine.

## Como Usar

## Propriedades do Módulo

| Propriedade | Tipo | Descrição | Padrão |
|---|---|---|---|
| `cape` | Mode | Selecionar uma cape pré-definida (ignorado se bannerMode está ativo) | Founder's |
| `bannerMode` | Mode | Escolher entre "Disabled" ou "Optifine Banner" | Disabled |
| `bannerUrl` | Text | URL do banner do Optifine | (vazio) |
| `gradientTop` | Color | Cor do topo do gradiente (RGB) | #6B9BD1 (Azul claro) |
| `gradientBottom` | Color | Cor da base do gradiente (RGB) | #1E3C72 (Azul escuro) |

## Exemplos de URLs de Banner
```
https://livzmc.net/banner/?u=USERNAME
https://livzmc.net/banner/?u=USERNAME&t=TEXTURE_ID
```

## Notas Técnicas

- O banner é carregado em uma thread separada para não congelar o cliente
- A transparência original do banner é preservada
- As cores do gradiente são aplicadas mantendo a opacidade dos pixels originais
- O banner é cacheado automaticamente após ser carregado
- Se as cores forem alteradas, o banner é automaticamente recarregado com as novas cores

## Troubleshooting

### "Falha ao carregar banner do Optifine"
- Verifique se a URL está correta
- Certifique-se de ter conexão com a internet
- Verifique se a URL está acessível

### Banner não aparece
- Certifique-se de que o módulo Cape está habilitado
- Verifique se bannerMode está configurado como "Optifine Banner"
- Verifique se uma URL válida foi inserida em bannerUrl

### As cores não estão sendo aplicadas
- Aguarde alguns segundos após inserir a URL ou mudar as cores
- O sistema pode estar carregando ou processando a imagem
