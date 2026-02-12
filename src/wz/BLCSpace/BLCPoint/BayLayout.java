package wz.BLCSpace.BLCPoint;

public class BayLayout {
    private int rowCount;
    public static final int DECK_TIER = 82;
    public BayLayout(int rowCount) {
        this.rowCount = rowCount;
    }

    public BayLayout() {
    }

    public int getRowCount() { return rowCount; }
    public int getDeckTier() {
        return DECK_TIER;
    }
}
