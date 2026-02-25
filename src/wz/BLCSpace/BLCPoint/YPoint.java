package wz.BLCSpace.BLCPoint;

public final class YPoint extends BLCPoint {
    public YPoint() {
        super();
    }

    public YPoint(int bay, int row, int tier) {
        super(bay, row, tier, false);
        if (bay < 1 || row < 1 || tier < 1)
            throw new IllegalArgumentException("Invalid YPoint");
    }


    @Override
    public char getType() {
        return 'Y';
    }

    @Override
    public YPoint copy() {
        return new YPoint(bay, row, tier);
    }
}
