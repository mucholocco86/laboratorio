# Wells Mic — protótipo experimental v0.1

Primeira bancada de testes do Wells Mic.

## Alvo inicial
- Samsung SM-J410G
- Android 8.1.0 (API 27)
- Compatibilidade mínima do app: Android 8.0 (API 26)

## Objetivo desta etapa
Validar primeiro o lado Android antes do Receiver/driver Windows.

O app v0.1 deve:
1. instalar e abrir no Android 8.1;
2. solicitar permissão de microfone;
3. exibir estado simples do microfone;
4. ligar/desligar a captura pelo botão principal;
5. tentar ativar a supressão de ruído nativa quando o aparelho oferecer suporte;
6. aplicar ganho digital experimental de +10 dB com saturação segura em PCM16;
7. liberar completamente o microfone ao desligar ou fechar o app.

Nesta etapa o áudio ainda NÃO é enviado ao Windows. Primeiro validamos captura, ganho e supressão no aparelho real. A ponte ADB/Receiver será a etapa seguinte.

## Regra de segurança do projeto
O Wells Mic não deve alterar, reiniciar ou reconfigurar RNDIS/ancoragem USB, interfaces de rede ou rotas do Windows/Android. O compartilhamento de internet USB do aparelho deve permanecer independente.

## Estrutura
`android/` contém um projeto Android Studio mínimo em Kotlin.
