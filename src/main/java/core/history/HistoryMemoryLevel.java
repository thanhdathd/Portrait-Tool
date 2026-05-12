package core.history;

/**
 * Defines the RAM budget levels for the Undo/Redo history.
 */
public enum HistoryMemoryLevel {
    LOW(512),
    MEDIUM(1024),
    HIGH(2048);

    private final long ramBudgetMb;

    HistoryMemoryLevel(long ramBudgetMb) {
        this.ramBudgetMb = ramBudgetMb;
    }

    /**
     * @return The RAM budget in Megabytes.
     */
    public long getRamBudgetMb() {
        return ramBudgetMb;
    }

    /**
     * @return The RAM budget in Bytes.
     */
    public long getRamBudgetBytes() {
        return ramBudgetMb * 1024 * 1024;
    }
}
