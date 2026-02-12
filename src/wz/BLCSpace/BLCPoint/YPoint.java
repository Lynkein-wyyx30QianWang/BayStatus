package wz.BLCSpace.BLCPoint;

public final class YPoint extends BLCPoint {
    public YPoint() {
        super();
    }

    public YPoint(int bay, int row, int tier) {
        this(bay, row, tier, false);
    }

    public YPoint(int bay, int row, int tier, boolean underDeck) {
        super(bay, row, tier, underDeck);
    }

    @Override
    public char getType() {
        return 'Y';
    }

    @Override
    public YPoint copy() {
        return new YPoint(bay, row, tier, underDeck);
    }
}
