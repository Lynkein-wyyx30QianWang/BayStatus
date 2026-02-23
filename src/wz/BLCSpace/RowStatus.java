package wz.BLCSpace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RowStatus implements Serializable {
    private final int layers; // 层数
    private final int maxBay; // 最大仓位
    private final BayStatus[] tierList; // 该行的各层状态，引用 BLC-Status 的二维数组
    private int bayRule = BayStatus.ANY_RULE; // 所有层共享的规则
    private boolean allowOverhang = false; // 是否允许悬空，默认禁止

    public RowStatus(BayStatus[][] blcSpace, int rowIndex) {
        this.tierList = blcSpace[rowIndex];
        this.layers = blcSpace[0].length;
        this.maxBay = blcSpace[0][0].getMaxBay();
    }

    public void setBayRule(int rule) {
        this.bayRule = rule;
        for (BayStatus bayStatus : tierList) {
            bayStatus.setBayRule(rule);
        }
    }

    public void setAllowOverhang(boolean allow) {
        this.allowOverhang = allow;
    }

    public int size() {
        int count = 0;
        for (int i = 0; i < layers; i++) {
            count += tierList[i].size();
        }
        return count;
    }

    public int sizeBasedSpace() {
        int count = 0;
        for (int i = 0; i < layers; i++) {
            count += tierList[i].sizeBasedSpace();
        }
        return count;
    }

    public boolean occupy(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        if (isNonSuspended(layer, bay)) {
            return tierList[layer].occupy(bay);
        }
        return false;
    }

    public boolean book(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        return tierList[layer].book(bay);
    }

    public boolean occupyBooked(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        if (isNonSuspended(layer, bay)) {
            return tierList[layer].occupyBooked(bay);
        }
        return false;
    }

    public void cancelBooked(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return;
        }
        tierList[layer].cancelBooked(bay);
    }

    public void release(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return;
        }
        tierList[layer].release(bay);
    }

    public boolean isNonSuspended(int layer, int bay) {
        if (!allowOverhang && layer > 0) {
            BayStatus lowerBay = tierList[layer - 1];
            return lowerBay.canBear(bay);
        }
        return true;
    }

    public boolean isAvailable(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        if (!tierList[layer].isAvailable(bay)) {
            return false;
        }
        return isNonSuspended(layer, bay);
    }

    public boolean isIdle(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        return tierList[layer].isAvailable(bay);
    }

    public boolean isUsed(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        return tierList[layer].hasGoods(bay);
    }

    public boolean isFetchable(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        if (!tierList[layer].hasGoods(bay)) {
            return false;
        }
        if (!allowOverhang && layer < layers - 1) {
            BayStatus upperBay = tierList[layer + 1];
            return !upperBay.canCover(bay);
        }
        return true;
    }

    public List<Integer> getUsedBays(int layer) {
        if (!isValidLayer(layer)) {
            return new ArrayList<>();
        }
        return tierList[layer].getUsedBays();
    }

    public List<List<Integer>> getUsedBays() {
        List<List<Integer>> usedBays = new ArrayList<>();
        for (int i = 0; i < layers; i++) {
            usedBays.add(tierList[i].getUsedBays());
        }
        return usedBays;
    }

    public List<Integer> getAvailableBays(int layer) {
        if (!isValidLayer(layer)) {
            return new ArrayList<>();
        }
        List<Integer> availableBays = new ArrayList<>();
        for (int bay = 1; bay <= maxBay; bay++) {
            if (isAvailable(layer, bay)) {
                availableBays.add(bay);
            }
        }
        return availableBays;
    }

    public int getAvailableBaysByBay(int bay) {
        if (bay < 1 || bay > maxBay) {
            return -1;
        }
        int availableBays = -1;
        for (int layer = 0; layer < layers; layer++) {
            if (isAvailable(layer, bay)) {
                availableBays = layer;
                break;
            }
        }
        return availableBays;
    }
    public int[][] getAvailableBaysByLargeBay(int bay) {
        if (bay < 1 || bay > maxBay || bay % 2 != 0) {
            return null;
        }
        int[][] availableBays = new int[3][2];
        for (int i = 0; i < 3; i++) {
            availableBays[i][0] = -1;
            availableBays[i][1] = -1;
        }
        boolean found_large_bay = false;
        boolean found_left_bay = false;
        boolean found_right_bay = false;
        int count = 0;
        for (int layer = 0; layer < layers; layer++) {
            BayStatus bayStatus = tierList[layer];
            if (bayStatus.isAvailable(bay)) {
                availableBays[count][0] = layer;
                availableBays[count][1] = bay;
                found_large_bay = true;
                count++;
            }
            if (bayStatus.isAvailable(bay - 1)) {
                availableBays[count][0] = layer;
                availableBays[count][1] = bay - 1;
                found_left_bay = true;
                count++;
            }
            if (bayStatus.isAvailable(bay + 1)) {
                availableBays[count][0] = layer;
                availableBays[count][1] = bay + 1;
                found_right_bay = true;
                count++;
            }
            if (found_large_bay || (found_left_bay && found_right_bay)) break;
        }
        return availableBays;
    }

    public List<Integer> getAvailableSmallBays(int layer) {
        if (!isValidLayer(layer)) return new ArrayList<>();
        List<Integer> availableSmallBays = new ArrayList<>();
        for (int bay = 1; bay <= maxBay; bay += 2) {
            if (isAvailable(layer, bay)) {
                availableSmallBays.add(bay);
            }
        }
        return availableSmallBays;
    }

    public List<Integer> getAvailableLargeBays(int layer) {
        if (!isValidLayer(layer)) return new ArrayList<>();
        List<Integer> availableLargeBays = new ArrayList<>();
        for (int bay = 2; bay <= maxBay; bay += 2) {
            if (isAvailable(layer, bay)) {
                availableLargeBays.add(bay);
            }
        }
        return availableLargeBays;
    }

    public List<List<Integer>> getAvailableBays() {
        List<List<Integer>> layerBays = new ArrayList<>();
        for (int layer = 0; layer < layers; layer++) {
            List<Integer> bays = getAvailableBays(layer);
            if (bays.isEmpty() && tierList[layer].isEmpty()) {
                break;
            }
            layerBays.add(bays);
        }
        return layerBays;
    }

    public List<List<Integer>> getAvailableSmallBays() {
        List<List<Integer>> layerBays = new ArrayList<>();
        for (int layer = 0; layer < layers; layer++) {
            List<Integer> bays = getAvailableSmallBays(layer);
            if (bays.isEmpty() && tierList[layer].isEmpty()) {
                break;
            }
            layerBays.add(bays);
        }
        return layerBays;
    }

    public List<List<Integer>> getAvailableLargeBays() {
        List<List<Integer>> layerBays = new ArrayList<>();
        for (int layer = 0; layer < layers; layer++) {
            List<Integer> bays = getAvailableLargeBays(layer);
            if (bays.isEmpty() && tierList[layer].isEmpty()) {
                break;
            }
            layerBays.add(bays);
        }
        return layerBays;
    }

    public int[] getFirstAvailableSmallBay() {
        int first0 = tierList[0].getFirstAvailableSmallBay();
        if (first0 != 0) {
            return new int[]{0, first0};
        }
        for (int layer = 1; layer < layers; layer++) {
            List<Integer> availableBays = tierList[layer].getAvailableBays();
            availableBays = availableBays.stream().filter(bay -> bay % 2 == 1).toList();
            BayStatus lowerBay = tierList[layer - 1];
            for (int bay : availableBays) {
                if (lowerBay.canBear(bay)) {
                    return new int[]{layer, bay};
                }
            }
        }
        return new int[]{-1, -1};
    }

    public int[] getFirstAvailableLargeBay() {
        int first0 = tierList[0].getFirstAvailableLargeBay();
        if (first0 != 0) {
            return new int[]{0, first0};
        }
        for (int layer = 1; layer < layers; layer++) {
            List<Integer> availableBays = tierList[layer].getAvailableBays();
            availableBays = availableBays.stream().filter(bay -> bay % 2 == 0).toList();
            BayStatus lowerBay = tierList[layer - 1];
            for (int bay : availableBays) {
                if (lowerBay.canBear(bay)) {
                    return new int[]{layer, bay};
                }
            }
        }
        return new int[]{-1, -1};
    }

    public List<List<Integer>> getAllFetchableBays() {
        List<List<Integer>> fetchableBays = new ArrayList<>();
        for (int bay = 1; bay <= maxBay; bay++) {
            for (int layer = 0; layer < layers; layer++) {
                if (tierList[layer].canBear(bay)) {
                    if (isFetchable(layer, bay)) {
                        fetchableBays.add(new ArrayList<>(Arrays.asList(layer, bay)));
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return fetchableBays;
    }

    public List<Integer> getFetchableBays(int bay) {
        if (bay < 1 || bay > maxBay) return new ArrayList<>();

        List<Integer> fetchableBays = new ArrayList<>();
        for (int layer = 0; layer < layers; layer++) {
            if (tierList[layer].canBear(bay)) {
                if (isFetchable(layer, bay)) {
                    fetchableBays.add(layer);
                }
            } else {
                if (!allowOverhang) break;
            }
        }

        return fetchableBays;
    }

    public boolean isEmpty(int layer) {
        if (!isValidLayer(layer)) {
            return true;
        }
        return tierList[layer].isEmpty();
    }

    public String getAllStatusDetails() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < layers; i++) {
            builder.append(String.format("  第%d层: %s\n", i + 1, tierList[i].getStatusDetails()));
        }
        return builder.toString();
    }

    public String getStatusDetails(int detail) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < layers; i++) {
            builder.append(String.format("  第%d层: %s\n", i + 1, tierList[i].getStatusDetails(detail)));
        }
        return builder.toString();
    }

    private boolean isValidLayer(int layer) {
        return layer >= 0 && layer < layers;
    }

    private boolean isValidLayerBay(int layer, int bay) {
        return isValidLayer(layer) && bay > 0 && bay <= maxBay;
    }

    @Override
    public String toString() {
        return String.format("wz.BLCSpace.RowStatus {layers=%d, rule=%d, allowOverhang=%s}", layers, bayRule, allowOverhang);
    }
}