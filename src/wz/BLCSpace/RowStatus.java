package wz.BLCSpace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RowStatus implements Serializable {
    private final int layers; // 层数
    private final int maxBay;
    private final List<BayStatus> bayStatusList; // 每层的仓位状态
    private int bayRule = BayStatus.ANY_RULE; // 所有层共享的规则
    private boolean allowOverhang = false; // 是否允许悬空，默认禁止

    public RowStatus(int layers, int maxBay) {
        if (layers < 1) {
            throw new IllegalArgumentException("层数必须大于0");
        }
        this.layers = layers;
        this.maxBay = maxBay;
        this.bayStatusList = new ArrayList<>();
        for (int i = 0; i < layers; i++) {
            bayStatusList.add(new BayStatus(maxBay));
        }
    }

    public RowStatus(int layers, int maxBay, int bayRule) {
        this(layers, maxBay);
        setBayRule(bayRule);
    }

    // 设置所有层的规则
    public void setBayRule(int rule) {
        this.bayRule = rule;
        for (BayStatus bayStatus : bayStatusList) {
            bayStatus.setBayRule(rule);
        }
    }

    // 开启/关闭悬空模式
    public void setAllowOverhang(boolean allow) {
        this.allowOverhang = allow;
    }

    // 占用指定层和仓位（0-based）
    public boolean occupy(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        if (isNonSuspended(layer, bay)) {
            return bayStatusList.get(layer).occupy(bay);
        }
        return false;
    }

    // 预订指定层和仓位
    public boolean book(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        return bayStatusList.get(layer).book(bay);
    }

    // 将已预订的仓位转为占用状态
    public boolean occupyBooked(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        if (isNonSuspended(layer, bay)) {
            return bayStatusList.get(layer).occupyBooked(bay);
        }
        return false;
    }

    // 取消指定层和仓位的预订状态
    public void cancelBooked(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return; // 层号或仓位无效，直接返回
        }
        bayStatusList.get(layer).cancelBooked(bay);
    }

    // 释放指定层和仓位
    public void release(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return;
        }
        bayStatusList.get(layer).release(bay);
    }

    // 查询指定层和仓位是否悬空
    public boolean isNonSuspended(int layer, int bay) {
        if (!allowOverhang && layer > 0) { // 注意：layer > 0 表示不是最底层
            BayStatus lowerBay = bayStatusList.get(layer - 1); // 下方层
            return lowerBay.canBear(bay); // 下方层有货物，才能放货，禁止悬空
        }
        return true;
    }

    // 查询指定层和仓位是否可用
    public boolean isAvailable(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }

        if (!bayStatusList.get(layer).isAvailable(bay)) {
            return false; // 仓位不可用
        }

        // 检查悬空限制
        return isNonSuspended(layer, bay);
    }

    public boolean isIdle(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        return bayStatusList.get(layer).isAvailable(bay);
    }

    // 查询指定层和仓位是否可以取货
    public boolean isFetchable(int layer, int bay) {
        if (!isValidLayerBay(layer, bay)) {
            return false;
        }
        if (!bayStatusList.get(layer).hasGoods(bay)) {
            return false;
        }

        if (!allowOverhang && layer < layers - 1) { // 注意：layer < layers - 1 表示不是最顶层
            BayStatus upperBay = bayStatusList.get(layer + 1); // 上方层
            return !upperBay.canCover(bay); // 上方层没有货物，才能取货，禁止悬空
        }
        return true;
    }

    // 返回指定层的已用仓位列表
    public List<Integer> getUsedBays(int layer) {
        if (!isValidLayer(layer)) {
            return new ArrayList<>(); // 层号无效时返回空列表
        }
        return bayStatusList.get(layer).getUsedBays();
    }

    // 返回所有层的已用仓位列表
    public List<List<Integer>> getUsedBays() {
        List<List<Integer>> usedBays = new ArrayList<>();
        for (int i = 0; i < layers; i++) {
            usedBays.add(bayStatusList.get(i).getUsedBays());
        }
        return usedBays;
    }

    // 获取指定层的可用仓位列表
    public List<Integer> getAvailableBays(int layer) {
        if (!isValidLayer(layer)) {
            return new ArrayList<>(); // 层号无效时返回空列表
        }

        List<Integer> availableBays = new ArrayList<>();
        for (int bay = 1; bay <= maxBay; bay++) {
            if (isAvailable(layer, bay)) {
                availableBays.add(bay);
            }
        }

        return availableBays;
    }

    // 获取指定 Bay 的可用仓位列表
    public int[][] getAvailableBaysByLargeBay(int bay) {
        if (bay < 1 || bay > maxBay || bay % 2 != 0) {
            return null;
        }

        int[][] availableBays = new int[3][2]; // 最多3个仓位，每条记录layer和 bay
        for (int i = 0; i < 3; i++) {
            availableBays[i][0] = -1;
            availableBays[i][1] = -1;
        }
        boolean found_large_bay = false;
        boolean found_left_bay = false;
        boolean found_right_bay = false;
        int count = 0;

        for (int layer = 0; layer < layers; layer++) {
            BayStatus bayStatus = bayStatusList.get(layer);
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
            if (found_large_bay || (found_left_bay && found_right_bay))
                break;
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

    // 获取所有层的可用仓位列表
    public List<List<Integer>> getAvailableBays() {
        List<List<Integer>> layerBays = new ArrayList<>();
        for (int layer = 0; layer < layers; layer++) {
            List<Integer> bays = getAvailableBays(layer);
            // 层为空时，结束循环
            if (bays.isEmpty() && bayStatusList.get(layer).isEmpty()) {
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
            if (bays.isEmpty() && bayStatusList.get(layer).isEmpty()) {
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
            if (bays.isEmpty() && bayStatusList.get(layer).isEmpty()) {
                break;
            }
            layerBays.add(bays);
        }
        return layerBays;
    }

    public int[] getFirstAvailableSmallBay() {
        // 先从最低层查找，如果找不到，则从上层层继续查找，此时需要检查仓位可用性
        int first0 = bayStatusList.getFirst().getFirstAvailableSmallBay();
        if (first0 != 0) {
            return new int[]{0, first0};
        }
        for (int layer = 1; layer < layers; layer++) {
            List<Integer> availableBays = bayStatusList.get(layer).getAvailableBays();
            availableBays = availableBays.stream().filter(bay -> bay % 2 == 1).toList();
            BayStatus lowerBay = bayStatusList.get(layer - 1);
            for (int bay : availableBays) {
                if (lowerBay.canBear(bay)) {
                    return new int[]{layer, bay};
                }
            }
        }
        return new int[]{-1, -1};
    }

    public int[] getFirstAvailableLargeBay() {
        int first0 = bayStatusList.getFirst().getFirstAvailableLargeBay();
        if (first0 != 0) {
            return new int[]{0, first0};
        }
        for (int layer = 1; layer < layers; layer++) {
            List<Integer> availableBays = bayStatusList.get(layer).getAvailableBays();
            availableBays = availableBays.stream().filter(bay -> bay % 2 == 0).toList();
            BayStatus lowerBay = bayStatusList.get(layer - 1);
            for (int bay : availableBays) {
                if (lowerBay.canBear(bay)) {
                    return new int[]{layer, bay};
                }
            }
        }
        return new int[]{-1, -1};
    }

    // 获取所有可取货的仓位列表
    public List<List<Integer>> getAllFetchableBays() {
        List<List<Integer>> fetchableBays = new ArrayList<>();
        for (int bay = 1; bay <= maxBay; bay++) {
            for (int layer = 0; layer < layers; layer++) {
                if (bayStatusList.get(layer).canBear(bay)) {
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

    public List<List<Integer>> getFetchableBays(int bay) {
        if (bay < 1 || bay > maxBay || bay % 2 == 1) return new ArrayList<>();
        List<List<Integer>> fetchableBays = new ArrayList<>();
        for (int b = bay - 1; b <= bay + 1; b += 1) {
            for (int layer = 0; layer < layers; layer++) {
                if (bayStatusList.get(layer).canBear(b)) {
                    if (isFetchable(layer, b)) {
                        fetchableBays.add(new ArrayList<>(Arrays.asList(layer, b)));
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return fetchableBays;
    }

    // 判断指定层是否为空
    public boolean isEmpty(int layer) {
        if (!isValidLayer(layer)) {
            return true; // 层号无效时，默认为空
        }
        return bayStatusList.get(layer).isEmpty();
    }

    // 获取所有层的状态详情
    public String getAllStatusDetails() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < layers; i++) {
            builder.append(String.format("第%d层:\n%s\n", i + 1, bayStatusList.get(i).getStatusDetails()));
        }
        return builder.toString();
    }

    // 校验层号有效性
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
