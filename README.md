<div align="center">

# Purity

</div>

* * *

# 소개

**Purity**는 접속한 클라이언트를 감지해 비순정 클라이언트, 모드 로더, 모드를 가려내는 Paper 플러그인입니다.

감지 규칙은 플러그인에 내장되어 있고, 서버 관리자는 `allow` 목록으로 허용할 항목을 고를 수 있습니다.

- 접속 전: 월드에 들어오기 전 configuration 단계에서 판정해 추방합니다.
- 접속 후: 채널 등록, 시야 거리 변경, 늦게 도착한 payload가 있을 시 재판정합니다.
- 번역 키: 클라이언트만 해석할 수 있는 키로 숨은 모드를 확인합니다.
- 다국어 킥 메시지: 클라이언트 언어에 맞춰 MiniMessage 메시지를 보냅니다.
- Velocity: 프록시 뒤에서도 추방이 다른 서버로 넘어가지 않고 접속을 끊습니다.

## 감지 방식

| 신호             | 내용                                                                     |
|------------------|--------------------------------------------------------------------------|
| 브랜드           | 클라이언트가 보내는 brand 문자열 (`vanilla`, `fabric`, `lunarclient` 등) |
| 채널             | `minecraft:register`로 등록한 플러그인 채널                              |
| Payload          | 서버로 보낸 custom payload 식별자                                        |
| `c:version` 응답 | Fabric·Quilt·NeoForge가 응답하는 공통 채널로 로더를 드러냅니다           |
| 시야 거리        | 바닐라 상한(32)을 넘는 값                                                |
| 번역 키          | 월드 아래 보이지 않는 표지판으로 키바인드, 번역 키 해석 결과를 읽습니다  |

어느 단서에도 해당하지 않는 브랜드나 `minecraft` 외 네임스페이스는 `UNKNOWN`으로 분류합니다.

<details>
<summary>감지 목록</summary>

- **클라이언트**: Lunar, Badlion, Feather(Dawn), LabyMod, AxolotlClient, 5zig, NoRisk, Alpine, Meteor, Wurst, Aristois, Impact, LiquidBounce, Inertia, BleachHack, RusherHack, Lambda, Future, Sigma, Novoline, Vape, Raven, Coffee, KAMI Blue, Lumina
- **로더**: Fabric, Quilt, Forge, NeoForge, `MODDED_LOADER`
- **모드**: Litematica, Syncmatica, WorldEdit CUI, Xaero's Minimap·World Map, JourneyMap, VoxelMap, Baritone, ReplayMod, Carpet, Vivecraft, Simple Voice Chat, Essential, Distant Horizons, X-Ray, Freecam, ChestESP, KillAura, AutoFish, AutoSwitch, AutoClicker, AntiAFK, World Downloader, Better Sprinting, OpSec, No Chat Reports, Tweakeroo, Extended Render Distance, Iris, Sodium, OptiFine

전체 ID는 게임 안에서 `/purity ids`로 확인할 수 있습니다.
</details>

## 한계

클라이언트가 보내는 정보를 바탕으로 판정합니다. 브랜드, 채널, 번역 키를 모두 순정처럼 위조하는 클라이언트는 잡지 못할 수 있으며, 안티치트를 대체하지 않습니다.

번역 키 감지는 해당 키를 번역하는 리소스팩이 적용되어 있으면 순정 클라이언트도 대상이 될 수 있습니다.

# 시작하기

### Velocity

Velocity는 백엔드에서 추방당한 플레이어를 `try` 목록의 다음 서버로 보냅니다. 프록시 `plugins/`에 `Purity-Velocity.jar`를 함께 적용하면 Purity가 보낸 신호를 받아 접속을 차단합니다.

## 설정

```yaml
# false: 로그만 남깁니다.
enforce: true

# 허용할 ID (예: [VOICECHAT, REPLAYMOD])
# Fabric, Quilt, NeoForge 클라이언트를 허용하려면 MODDED_LOADER가 필요합니다.
allow: []

# 기본 제공에 없는 브랜드, 채널 및 payload 처리 방식을 고릅니다. (kick 또는 log)
unknown: kick

# 추방하지 않을 플레이어 UUID
bypass: []

probes:
  enabled: true
  timeout-ticks: 40
```

메시지는 `messages`에서 언어별로 설정합니다. 클라이언트 언어와 일치하는 항목, 같은 언어, `default` 순으로 적용됩니다.

## 명령어 및 권한

| 명령어                   | 권한            | 설명                               |
|--------------------------|-----------------|------------------------------------|
| `/purity info <player>`  | `purity.info`   | 감지 정보, 근거, 판정 결과         |
| `/purity ids`            | `purity.info`   | 감지 가능한 ID 목록과 허용 여부    |
| `/purity probe <player>` | `purity.probe`  | 번역 키 감지를 수동으로 실행       |
| `/purity reload`         | `purity.reload` | 설정 다시 불러오기, 접속자 재검사  |
| -                        | `purity.bypass` | 입장 후 재검사 제외 (기본값 false) |

## 빌드

```bash
./gradlew buildPlugin
```

`build/dist/`에 `Purity-<version>.jar`(Paper)와 `Purity-Velocity-<version>.jar`(Velocity)로 생성됩니다.

### 라이브러리

- [Kotlin stdlib](https://github.com/JetBrains/kotlin)
- [Paper API](https://papermc.io)
- [Velocity API](https://papermc.io/software/velocity)
