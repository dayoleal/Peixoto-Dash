package br.mackenzie;

public class RehabMetrics {

    public final LevelConfig level;

    public int    score              = 0;
    public int    obstaclesAvoided   = 0;
    public int    foodsCollected     = 0;
    public int    collisionsTaken    = 0;
    public float  totalCenterTime    = 0f;
    public float  totalActiveTime    = 0f;
    public int    directionChanges   = 0;
    public float  fastestReactionSec = Float.POSITIVE_INFINITY;
    public float  averageReactionSec = 0f;
    public int    reactionSamples    = 0;
    public float  durationSec        = 0f;

    public RehabMetrics(LevelConfig level) {
        this.level = level;
    }

    public void onObstacleAvoided() { obstaclesAvoided++; }
    public void onFoodCollected()   { foodsCollected++; }
    public void onCollision()       { collisionsTaken++; }

    public void recordReaction(float reactionSec) {
        averageReactionSec = (averageReactionSec * reactionSamples + reactionSec)
                             / (reactionSamples + 1);
        reactionSamples++;
        if (reactionSec < fastestReactionSec) {
            fastestReactionSec = reactionSec;
        }
    }

    public void syncFromInput(InputController input) {
        this.totalCenterTime  = input.getTotalCenterTime();
        this.totalActiveTime  = input.getTotalActiveTime();
        this.directionChanges = input.getDirectionChanges();
    }

    public float getCenterRatio() {
        float total = totalCenterTime + totalActiveTime;
        return total > 0f ? totalCenterTime / total : 0f;
    }

    public float getPerformanceScore() {
        float scoreFrac    = Math.min(1f, score / (float) level.targetScore);
        float centerFrac   = Math.min(1f, getCenterRatio() / Math.max(0.01f, level.minCenterRatio));
        float avoidFrac    = obstaclesAvoided == 0 ? 0f
                             : obstaclesAvoided / (float)(obstaclesAvoided + collisionsTaken);
        float reactionFrac = reactionSamples == 0 ? 0.5f
                             : Math.max(0f, Math.min(1f, 1f - averageReactionSec / 1.5f));

        return 0.25f * scoreFrac
             + 0.25f * centerFrac
             + 0.25f * avoidFrac
             + 0.25f * reactionFrac;
    }

    public boolean passedLevel() {
        return score >= level.targetScore
            && getCenterRatio() >= level.minCenterRatio;
    }

    public String summaryLine(String label, float value) {
        return String.format("%-28s %.2f", label, value);
    }
}
