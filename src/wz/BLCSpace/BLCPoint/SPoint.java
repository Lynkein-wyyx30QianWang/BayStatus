package wz.BLCSpace.BLCPoint;

public final class SPoint extends BLCPoint {
    public SPoint() {
        super();
    }

    public SPoint(int bay, int row, int tier) {
        super(bay, row, tier, tier < 82);
        if (isValidBay(bay) && isValidRow(row) && isValidTier(tier) ) ;
        else throw new IllegalArgumentException("Invalid SPoint");
    }

    public boolean isValidBay(int bay) {
        return bay > 0;
    }

    public boolean isValidRow(int row) {
        return row >= 0;
    }

    public boolean isValidTier(int tier) {
        return tier > 0 && tier % 2 == 0;
    }

    @Override
    public char getType() {
        return 'S';
    }

    @Override
    public SPoint copy() {
        return new SPoint(bay, row, tier);
    }

}

