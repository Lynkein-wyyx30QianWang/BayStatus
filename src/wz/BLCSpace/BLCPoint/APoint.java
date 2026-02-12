package wz.BLCSpace.BLCPoint;

public final class APoint extends BLCPoint {
    public APoint() { super(); }
    public APoint(int bay, int row, int tier) { this(bay, row, tier, false); }
    public APoint(int bay, int row, int tier, boolean underDeck) {
        super(bay, row, tier, underDeck);
    }

    @Override
    public char getType() { return 'A'; }

    @Override
    public APoint copy() {
        return new APoint(bay, row, tier, underDeck);
    }
}

