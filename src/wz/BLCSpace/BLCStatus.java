package wz.BLCSpace;

import java.util.ArrayList;
import java.util.List;
import wz.BLCSpace.BLCPoint.*;

public class BLCStatus {
    private final int layers;
    private final int maxBay;
    private final int rows;
    private int bayRule = BayStatus.FOUR_N_MINUS_TWO_RULE;
    private boolean allowOverhang = false;
    private BayStatus[][] blcSpace; // 行、层的二维数组
    private RowStatus[] rowStatus;

    public BLCStatus(int maxBay, int rows, int layers) {
        if (rows < 1) {
            throw new IllegalArgumentException("行数必须大于0");
        }
        this.layers = layers;
        this.maxBay = maxBay;
        this.rows = rows;
        this.blcSpace = new BayStatus[rows][layers];
        this.rowStatus = new RowStatus[rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < layers; j++) {
                blcSpace[i][j] = new BayStatus(maxBay);
            }
            rowStatus[i] = new RowStatus(blcSpace, i);
            rowStatus[i].setBayRule(bayRule);
        }
    }

    public void setBayRule(int bayRule) {
        this.bayRule = bayRule;
        for (int i = 0; i < rows; i++) {
            rowStatus[i].setBayRule(bayRule);
        }
    }

    public int getBayRule() {
        return bayRule;
    }

    public void setAllowOverhang(boolean allowOverhang) {
        this.allowOverhang = allowOverhang;
        for (int i = 0; i < rows; i++) {
            rowStatus[i].setAllowOverhang(allowOverhang);
        }
    }

    public boolean isAllowOverhang() {
        return allowOverhang;
    }

    public RowStatus getRowStatus(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows) {
            throw new IndexOutOfBoundsException("行号超出范围");
        }
        return rowStatus[rowIndex];
    }

    public int getCapacity(boolean smallBased) {
        if(smallBased) {
            return layers * rows * (maxBay+1) / 2;
        }else {
            return layers * rows * ((maxBay+1) / 4);
        }
    }

    public int size() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            count += rowStatus[i].size();
        }
        return count;
    }

    public int sizeBasedSpace() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            count += rowStatus[i].sizeBasedSpace();
        }
        return count;
    }

    public boolean occupy(int rowIndex, int layer, int bay) {
        if (rowIndex < 0 || rowIndex >= rows || layer < 0 || layer >= layers) {
            return false;
        }
        return blcSpace[rowIndex][layer].occupy(bay);
    }

    public boolean occupy(APoint point) {
        return occupy(point.getRow(), point.getTier(), point.getBay());
    }

    public boolean book(int rowIndex, int layer, int bay) {
        if (rowIndex < 0 || rowIndex >= rows || layer < 0 || layer >= layers) {
            return false;
        }
        return blcSpace[rowIndex][layer].book(bay);
    }

    public boolean book(APoint point) {
        return book(point.getRow(), point.getTier(), point.getBay());
    }

    public boolean occupyBooked(int rowIndex, int layer, int bay) {
        if (rowIndex < 0 || rowIndex >= rows || layer < 0 || layer >= layers) {
            return false;
        }
        return blcSpace[rowIndex][layer].occupyBooked(bay);
    }

    public boolean occupyBooked(APoint point) {
        return occupyBooked(point.getRow(), point.getTier(), point.getBay());
    }

    public void cancelBooked(int rowIndex, int layer, int bay) {
        if (rowIndex < 0 || rowIndex >= rows || layer < 0 || layer >= layers) {
            return;
        }
        blcSpace[rowIndex][layer].cancelBooked(bay);
    }

    public void cancelBooked(APoint point) {
        cancelBooked(point.getRow(), point.getTier(), point.getBay());
    }

    public void release(int rowIndex, int layer, int bay) {
        if (rowIndex < 0 || rowIndex >= rows || layer < 0 || layer >= layers) {
            return;
        }
        blcSpace[rowIndex][layer].release(bay);
    }

    public void release(APoint point) {
        release(point.getRow(), point.getTier(), point.getBay());
    }

    public boolean isAvailable(int rowIndex, int layer, int bay) {
        if (rowIndex < 0 || rowIndex >= rows || layer < 0 || layer >= layers) {
            return false;
        }
        return blcSpace[rowIndex][layer].isAvailable(bay);
    }

    public boolean isAvailable(APoint point) {
        return isAvailable(point.getRow(), point.getTier(), point.getBay());
    }

    public APoint getFirstAvailableSmallBay() {
        for (int i = 0; i < rows; i++) {
            int[] firstAvailableSmallBay = getRowStatus(i).getFirstAvailableSmallBay();
            if (firstAvailableSmallBay[0] >= 0) {
                return new APoint(firstAvailableSmallBay[1], i, firstAvailableSmallBay[0]);
            }
        }
        return null;
    }

    public APoint getFirstAvailableLargeBay() {
        for (int i = 0; i < rows; i++) {
            int[] firstAvailableLargeBay = getRowStatus(i).getFirstAvailableLargeBay();
            if (firstAvailableLargeBay[0] >= 0) {
                return new APoint(firstAvailableLargeBay[1], i, firstAvailableLargeBay[0]);
            }
        }
        return null;
    }

    public List<APoint> getAllAvailableSmallBays() {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<List<Integer>> smallBays = getRowStatus(i).getAvailableSmallBays();
            for (int j = 0; j < smallBays.size(); j++) {
                List<Integer> smallBay = smallBays.get(j);
                if (!smallBay.isEmpty()) {
                    for (int b : smallBay) {
                        availableBays.add(new APoint(b, i, j));
                    }
                }
            }
        }
        return availableBays;
    }

    public List<APoint> getAllAvailableLargeBays() {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<List<Integer>> largeBays = getRowStatus(i).getAvailableLargeBays();
            for (int j = 0; j < largeBays.size(); j++) {
                List<Integer> largeBay = largeBays.get(j);
                if (!largeBay.isEmpty()) {
                    for (int b : largeBay) {
                        availableBays.add(new APoint(b, i, j));
                    }
                }
            }
        }
        return availableBays;
    }

    public List<APoint> getAvailableBays(int bay) {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            int tier = getRowStatus(i).getAvailableBaysByBay( bay);
            if (tier >= 0)
                availableBays.add(new APoint(bay, i, tier));
        }
        return availableBays;
    }

    public List<APoint> getAllFetchableBays() {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<List<Integer>> fetchableBays = getRowStatus(i).getAllFetchableBays();
            for (List<Integer> fetchableBay : fetchableBays) {
                availableBays.add(new APoint(fetchableBay.get(1), i, fetchableBay.get(0)));
            }
        }
        return availableBays;
    }

    public List<APoint> getFetchableBays(int bay) {
        List<APoint> availableBays = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<Integer> fetchableBays = getRowStatus(i).getFetchableBays(bay);
            for (int layer : fetchableBays) {
                availableBays.add(new APoint(bay, i, layer));
            }
        }
        return availableBays;
    }

    public String getAllRowsStatusDetails() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            builder.append(String.format("第%d行:\n%s\n", i + 1, getRowStatus(i).getAllStatusDetails()));
        }
        return builder.toString();
    }

    public String getAllStatusDetails(int detail) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            builder.append(String.format("第%d行:\n%s\n", i + 1, getRowStatus(i).getStatusDetails(detail)));
        }
        return builder.toString();
    }

    public String getRowStatusDetails(int rowIndex, int detail) {
        if (rowIndex < 0 || rowIndex >= rows) {
            return null;
        }
        return getRowStatus(rowIndex).getStatusDetails( detail);
    }

    @Override
    public String toString() {
        return String.format("wz.BLCSpace.BLCStatus {rows=%d, layers=%d, maxBay=%d}", rows, layers, maxBay);
    }
}