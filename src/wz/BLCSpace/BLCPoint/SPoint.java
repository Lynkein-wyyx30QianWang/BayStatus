package wz.BLCSpace.BLCPoint;

public final class SPoint extends BLCPoint {
    public SPoint() {
        super();
    }

    public SPoint(int bay, int row, int tier) {
        this(bay, row, tier, false);
    }

    public SPoint(int bay, int row, int tier, boolean underDeck) {
        super(bay, row, tier, underDeck);
    }

    @Override
    public char getType() {
        return 'S';
    }

    @Override
    public SPoint copy() {
        return new SPoint(bay, row, tier, underDeck);
    }

}

