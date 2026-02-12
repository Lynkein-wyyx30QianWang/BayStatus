package wz.BLCSpace.BLCPoint;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public abstract class BLCPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int bay;
    protected int row;
    protected int tier;
    protected boolean underDeck;

    // 构造器
    protected BLCPoint() {}

    protected BLCPoint(int bay, int row, int tier, boolean underDeck) {
        this.bay = bay;
        this.row = row;
        this.tier = tier;
        this.underDeck = underDeck;
    }

    // getter / setter（建议添加）
    public int getBay() { return bay; }
    public int getRow() { return row; }
    public int getTier() { return tier; }
    public boolean isUnderDeck() { return underDeck; }

    // 抽象方法：获取类型标识
    public abstract char getType();

    // 通用 copy（子类可 override）
    public BLCPoint copy() {
        try {
            return (BLCPoint) super.clone(); // 需实现 Cloneable
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BLCPoint that = (BLCPoint) o;
        return bay == that.bay && row == that.row && tier == that.tier
                && underDeck == that.underDeck;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bay, row, tier, underDeck);
    }

    @Override
    public String toString() {
        return String.format("%c(%d,%d,%d,%s)", getType(), bay, row, tier, underDeck);
    }

    // 比较器移到外部或静态工具类
    public static Comparator<BLCPoint> tierRowComparator() {
        return Comparator.comparingInt(BLCPoint::getTier)
                .thenComparingInt(BLCPoint::getRow);
    }
}
