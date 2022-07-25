package guess.dto.statistics;

public class SpeakerMetricsDtoDegrees {
    private final boolean javaChampion;
    private final boolean mvp;
    private final boolean mvpReconnect;
    private final boolean anyMvp;

    public SpeakerMetricsDtoDegrees(boolean javaChampion, boolean mvp, boolean mvpReconnect, boolean anyMvp) {
        this.javaChampion = javaChampion;
        this.mvp = mvp;
        this.mvpReconnect = mvpReconnect;
        this.anyMvp = anyMvp;
    }

    public boolean isJavaChampion() {
        return javaChampion;
    }

    public boolean isMvp() {
        return mvp;
    }

    public boolean isMvpReconnect() {
        return mvpReconnect;
    }

    public boolean isAnyMvp() {
        return anyMvp;
    }
}
