package br.mackenzie;

public class LevelConfig {

    public final int    number;
    public final String name;
    public final String description;
    public final String backgroundAsset;

    public final float  initialSpeedMul;
    public final float  maxSpeedMul;
    public final float  speedRamp;
    public final float  baseSpawnInterval;
    public final float  minSpawnInterval;

    public final int    targetScore;       
    public final float  targetDurationSec;  
    public final float  minCenterRatio;     

    private LevelConfig(int number, String name, String description, String bg,
                        float initialSpeedMul, float maxSpeedMul, float speedRamp,
                        float baseSpawnInterval, float minSpawnInterval,
                        int targetScore, float targetDurationSec, float minCenterRatio) {
        this.number = number;
        this.name = name;
        this.description = description;
        this.backgroundAsset = bg;
        this.initialSpeedMul = initialSpeedMul;
        this.maxSpeedMul = maxSpeedMul;
        this.speedRamp = speedRamp;
        this.baseSpawnInterval = baseSpawnInterval;
        this.minSpawnInterval = minSpawnInterval;
        this.targetScore = targetScore;
        this.targetDurationSec = targetDurationSec;
        this.minCenterRatio = minCenterRatio;
    }

    public static final LevelConfig LEVEL_1 = new LevelConfig(
        1, "Praia Inicial",
        "Movimentos lentos. Foco em aprender o controle.",
        "scene.png",
        1.0f, 1.6f, 0.03f,
        2.4f, 1.4f,
        300, 45f, 0.50f
    );

    public static final LevelConfig LEVEL_2 = new LevelConfig(
        2, "Praia Intermediária",
        "Velocidade média. Reaja com mais agilidade.",
        "scene.png",
        1.4f, 2.4f, 0.05f,
        1.8f, 1.0f,
        500, 60f, 0.55f
    );

    public static final LevelConfig LEVEL_3 = new LevelConfig(
        3, "Praia Avançada",
        "Velocidade alta. Coordenação refinada.",
        "scene.png",
        1.8f, 3.2f, 0.07f,
        1.4f, 0.75f,
        700, 75f, 0.60f
    );

    public static final LevelConfig[] ALL = { LEVEL_1, LEVEL_2, LEVEL_3 };

    public static LevelConfig get(int oneBasedNumber) {
        if (oneBasedNumber < 1 || oneBasedNumber > ALL.length) {
            throw new IllegalArgumentException("Fase inexistente: " + oneBasedNumber);
        }
        return ALL[oneBasedNumber - 1];
    }
}
