package wz.BLCSpace.BLCPoint;

public class PointConverter {
    private final BayLayout layout;
    public PointConverter(BayLayout layout) {
        this.layout = layout;
    }

    // 将 Y 型坐标转为 A 型
    public APoint convertYToA(YPoint y) {
        APoint a = new APoint();
        a.bay = y.bay;
        a.underDeck = y.underDeck;
        a.tier = y.tier - 1;
        a.row = y.row - 1;
        return a;
    }

    public YPoint convertAToY(APoint a) {
        YPoint y = new YPoint();
        y.bay = a.getBay();
        y.underDeck = a.isUnderDeck();
        y.row = a.getRow() + 1;
        y.tier = a.getTier() + 1;
        return y;
    }

    public APoint convertSToA(SPoint s) {
        APoint a = new APoint();
        a.bay = s.getBay();
        a.underDeck = s.isUnderDeck();

        int rowCount = layout.getRowCount();
        a.row = shipToArray(rowCount, s.getRow());

        // Tier 转换
        if (s.isUnderDeck()) {
            a.tier = s.getTier() / 2 - 1;
        } else {
            a.tier = (s.getTier() - layout.getDeckTier()) / 2;
        }

        return a;
    }

    public SPoint convertAToS(APoint a) {
        SPoint s = new SPoint();
        s.bay = a.getBay();
        s.underDeck = a.isUnderDeck();

        int rowCount = layout.getRowCount();
        s.row = arrayToShip(rowCount, a.getRow());

        // Tier 转换：根据是否在甲板下
        if (a.isUnderDeck()) {
            s.tier = (a.getTier() + 1) * 2;
        } else {
            s.tier = a.getTier() * 2 + layout.getDeckTier();
        }

        return s;
    }

    public static int arrayToShip(int n, int x) {
        if (n <= 0) {
            throw new IllegalArgumentException("Sequence length n must be positive");
        }
        if (x < 0 || x >= n) {
            throw new IllegalArgumentException("x must be in range [0, " + (n - 1) + "]");
        }

        int mid = n / 2; // 0 所在的位置

        if (x < mid) {
            // 左侧：降序奇数
            return 2 * (mid - x) - 1;
        } else if (x == mid) {
            // 中心位置
            return 0;
        } else {
            // 右侧：升序偶数（2, 4, 6, ...）
            return 2 * (x - mid);
        }
    }

    public static int shipToArray(int n, int y) {
        if (n <= 0) {
            throw new IllegalArgumentException("Sequence length n must be positive");
        }
        if (y < 0 || y >= n) {
            throw new IllegalArgumentException("y must be in range [0, " + (n - 1) + "]");
        }

        int mid = n / 2;

        if (y == 0) {
            return mid;
        } else if (y % 2 == 1) {
            // y 是奇数 → 位于左侧
            return mid - (y + 1) / 2;
        } else {
            // y 是正偶数 → 位于右侧
            return mid + y / 2;
        }
    }

}


