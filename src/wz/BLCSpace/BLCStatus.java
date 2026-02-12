package wz.BLCSpace;

import java.util.ArrayList;
import java.util.List;
import wz.BLCSpace.BLCPoint.*;

public class BLCStatus {
    private final int layers;
    private final int maxBay;
    private final int rows;
    private int bayRule = BayStatus.FOUR_N_MINUS_TWO_RULE; // 所有层共享的规则
    private boolean allowOverhang = false;
    private List<RowStatus> totalStatus;

    public BLCStatus(int maxBay, int rows, int layers) {
        if (rows < 1) {
            throw new IllegalArgumentException("行数必须大于0");
        }
        this.layers = layers;
        this.maxBay = maxBay;
        this.rows = rows;
        this.totalStatus = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            RowStatus rowStatus = new RowStatus(layers, maxBay);
            rowStatus.setBayRule(bayRule);
            totalStatus.add(rowStatus);
        }
    }

    public void setBayRule(int bayRule) {
        this.bayRule = bayRule;
        for (RowStatus rowStatus : totalStatus) {
            rowStatus.setBayRule(bayRule);
        }
    }

    public int getBayRule() {
        return bayRule;
    }

    public void setAllowOverhang(boolean allowOverhang) {
        this.allowOverhang = allowOverhang;
        for (RowStatus rowStatus : totalStatus) {
            rowStatus.setAllowOverhang(allowOverhang);
        }
    }

    public boolean isAllowOverhang() {
        return allowOverhang;
    }

    // 获取指定行的 wz.BLCSpace.RowStatus 对象
    public RowStatus getRowStatus(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows) {
            throw new IndexOutOfBoundsException("行号超出范围");
        }
        return totalStatus.get(rowIndex);
    }

    // 占用指定行、层和仓位
    public boolean occupy(int rowIndex, int layer, int bay) {
        return getRowStatus(rowIndex).occupy(layer, bay);
    }

    public boolean occupy(APoint point) {
        return occupy(point.getRow(), point.getTier(), point.getBay());
    }

    // 预订指定行、层和仓位
    public boolean book(int rowIndex, int layer, int bay) {
        return getRowStatus(rowIndex).book(layer, bay);
    }

    public boolean book(APoint point) {
        return book(point.getRow(), point.getTier(), point.getBay());
    }

    // 将已预订的仓位转为占用状态
    public boolean occupyBooked(int rowIndex, int layer, int bay) {
        return getRowStatus(rowIndex).occupyBooked(layer, bay);
    }

    public boolean occupyBooked(APoint point) {
        return occupyBooked(point.getRow(), point.getTier(), point.getBay());
    }

    // 取消指定行、层和仓位的预订状态
    public void cancelBooked(int rowIndex, int layer, int bay) {
        getRowStatus(rowIndex).cancelBooked(layer, bay);
    }

    public void cancelBooked(APoint point) {
        cancelBooked(point.getRow(), point.getTier(), point.getBay());
    }

    // 释放指定行、层和仓位
    public void release(int rowIndex, int layer, int bay) {
        getRowStatus(rowIndex).release(layer, bay);
    }

    public void release(APoint point) {
        release(point.getRow(), point.getTier(), point.getBay());
    }

    // 查询指定行、层和仓位是否可用
    public boolean isAvailable(int rowIndex, int layer, int bay) {
        return getRowStatus(rowIndex).isAvailable(layer, bay);
    }

    public boolean isAvailable(APoint point) {
        return isAvailable(point.getRow(), point.getTier(), point.getBay());
    }

    // 返回第一个可用的小仓位，三维坐标表示
    public APoint getFirstAvailableSmallBay() {
        for (int i = 0; i < rows; i++) {
            int[] firstAvailableSmallBay = getRowStatus(i).getFirstAvailableSmallBay();
            if (firstAvailableSmallBay[0] >= 0) {
                return new APoint(firstAvailableSmallBay[1], i, firstAvailableSmallBay[0]);
            }
        }
        return null;
    }

    // 获取第一个可用的大仓位，三维坐标表示
    public APoint getFirstAvailableLargeBay() {
        for (int i = 0; i < rows; i++) {
            int[] firstAvailableLargeBay = getRowStatus(i).getFirstAvailableLargeBay();
            if (firstAvailableLargeBay[0] >= 0) {
                return new APoint(firstAvailableLargeBay[1], i, firstAvailableLargeBay[0]);
            }
        }
        return null;
    }

    // 返回所有可用的小仓位，三维坐标表示
    public List<APoint> getAllAvailableSmallBays() {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<List< Integer>> smallBays = totalStatus.get(i).getAvailableSmallBays();
            for (int j = 0; j < smallBays.size(); j++) {
                List< Integer> smallBay = smallBays.get(j);
                if (!smallBay.isEmpty()) {
                    for (int b : smallBay) {
                        availableBays.add(new APoint(b, i, j));
                    }
                }
            }
        }
        return availableBays;
    }

    // 获取所有可用的大仓位，三维坐标表示
    public List<APoint> getAllAvailableLargeBays() {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<List< Integer>> largeBays = totalStatus.get(i).getAvailableLargeBays();
            for (int j = 0; j < largeBays.size(); j++) {
                List< Integer> largeBay = largeBays.get(j);
                if (!largeBay.isEmpty()) {
                    for (int b : largeBay) {
                        availableBays.add(new APoint(b, i, j));
                    }
                }
            }
        }
        return availableBays;
    }

    // 获取指定 Bay 的可用仓位列表，三维坐标表示。注意：Bay 限制为偶数，检索时包括左右相邻的奇数Bay。
    public List<APoint> getAvailableBays(int bay) {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            int[][] rowBays = totalStatus.get(i).getAvailableBaysByLargeBay(bay);
            for (int[] row : rowBays) {
                if (row[0] < 0) break;

                availableBays.add(new APoint(row[1], i, row[0]));
            }
        }
        return availableBays;
    }

    // 获取所有没有被上层压住的货物仓位列表，三维坐标表示
    public List<APoint> getAllFetchableBays() {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<List< Integer>> fetchableBays = totalStatus.get(i).getAllFetchableBays();
            for (List<Integer> fetchableBay : fetchableBays) {
                availableBays.add(new APoint(fetchableBay.get(1), i, fetchableBay.get(0)));
            }
        }
        return availableBays;
    }

    public List<APoint> getFetchableBays(int bay) {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<List< Integer>> fetchableBays = totalStatus.get(i).getFetchableBays(bay);
            for (List<Integer> fetchableBay : fetchableBays) {
                availableBays.add(new APoint(fetchableBay.get(1), i, fetchableBay.get(0)));
            }
        }
        return availableBays;
    }

    // 获取所有行的状态详情
    public String getAllRowsStatusDetails() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            builder.append(String.format("第%d行:\n%s\n", i + 1, totalStatus.get(i).getAllStatusDetails()));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return String.format("wz.BLCSpace.BLCStatus {rows=%d, layers=%d, maxBay=%d}", rows, layers, maxBay);
    }
}
