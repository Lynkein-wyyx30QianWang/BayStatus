package wz.BLCSpace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BayStatus implements Serializable {
    // 状态常量
    public static final int FORBIDDEN = -1;
    public static final int ACTIVE = 0;
    public static final int USED = 1;
    // 因共用仓位被使用导致自身暂时失效
    public static final int INACTIVE = 2;
    public static final int BOOKED = 4;

    // 规则常量
    public static final int ANY_RULE = 0;
    public static final int ONLY_ODD_RULE = 1;
    public static final int ONLY_EVEN_RULE = 2;
    public static final int FOUR_N_RULE = 4;
    public static final int FOUR_N_MINUS_TWO_RULE = 6;

    private final int maxBay;
    private final int[] _status;
    private int bayRule = ANY_RULE;

    public BayStatus() {
        this(9, 6);
    }

    public BayStatus(int maxBay){
        validateMaxBay(maxBay);
        this.maxBay = maxBay;
        _status = new int[maxBay + 1];
    }

    public BayStatus(int maxBay, int bayRule){
        this(maxBay);
        setBayRule(bayRule);
    }

    private void validateMaxBay(int maxBay) {
        if (maxBay < 1 || maxBay % 2 == 0) {
            throw new IllegalArgumentException(
                    "maxBay must be a positive odd number, but got: " + maxBay);
        }
    }

    public void setBayRule(int rule) {
        if (rule != ANY_RULE
                && rule != FOUR_N_MINUS_TWO_RULE
                && rule != ONLY_EVEN_RULE
                && rule != ONLY_ODD_RULE
                && rule != FOUR_N_RULE
        ) {
            throw new IllegalArgumentException("Invalid bay rule: " + rule);
        }
        this.bayRule = rule;
        for(int i = 1; i <= maxBay; i++) {
            if(shouldSkipBasedOnRule(i))
                _status[i] = FORBIDDEN;
        }
    }

    public int getMaxBay() {
        return maxBay;
    }

    public int getBayRule() {
        return bayRule;
    }

    public int[] getStatusArray() {
        return _status;
    }
    public List<Integer> getUsedBays() {
        List<Integer> used = new ArrayList<>();
        for (int i = 1; i <= maxBay; i++) {
            if (hasGoods(i)) {
                used.add(i);
            }
        }
        return used;
    }

    public List<Integer> getAvailableBays() {
        List<Integer> available = new ArrayList<>();
        for (int i = 1; i <= maxBay; i++) {
            if (isAvailable(i)) {
                available.add(i);
            }
        }
        return available;
    }

    public List<Integer> getBookedBays() {
        List<Integer> booked = new ArrayList<>();
        for (int i = 1; i <= maxBay; i++) {
            if (isBooked(i)) {
                booked.add(i);
            }
        }
        return booked;
    }

    public int getFirstAvailableSmallBay() {
        for (int i = 1; i <= maxBay; i += 2) {
            if (isAvailable(i)) {
                return i;
            }
        }
        return 0;
    }

    public int getFirstAvailableLargeBay() {
        for (int i = 2; i <= maxBay; i += 2) {
            if (isAvailable(i)) {
                return i;
            }
        }
        return 0;
    }

    public int getRandomAvailableSmallBay() {
        List<Integer> available = new ArrayList<>();
        for (int i = 1; i <= maxBay; i += 2) {
            if (isAvailable(i)) {
                available.add(i);
            }
        }
        if (available.isEmpty()) return  0;

        Random random = new Random();
        int randomIndex = random.nextInt(available.size());
        return available.get(randomIndex);
    }

    public int getRandomAvailableLargeBay() {
        List<Integer> available = new ArrayList<>();
        for (int i = 2; i <= maxBay; i += 2) {
            if (isAvailable(i)) {
                available.add(i);
            }
        }
        if (available.isEmpty()) return  0;

        Random random = new Random();
        int randomIndex = random.nextInt(available.size());
        return available.get(randomIndex);
    }

    public boolean occupy(int bay) {
        if (!isAvailable(bay)) {
            return false;
        }

        _status[bay] = USED;

        if (bay % 2 == 0) {
            setAdjacentBaysToINACTIVE(bay, 2);
        } else {
            setAdjacentBaysToINACTIVE(bay, 1);
        }

        return true;
    }

    public boolean book(int bay) {
        if (!isAvailable(bay)) return false;

        _status[bay] = BOOKED;

        if (bay % 2 == 0) {
            setAdjacentBaysToINACTIVE(bay, 2);
        } else {
            setAdjacentBaysToINACTIVE(bay, 1);
        }

        return true;
    }
    public boolean occupyBooked(int bay) {
        if (_status[bay] != BOOKED) {
            return false;
        }

        _status[bay] = USED;

        return true;
    }

    public void release(int bay) {
        if (!hasGoods(bay)) {
            return ;
        }

        if (bay % 2 == 0) {
            releaseEvenBay(bay);
        } else {
            releaseOddBay(bay);
        }

    }

    public void cancelBooked(int bay) {
        if (_status[bay] != BOOKED) {
            return ;
        }

        if (bay % 2 == 0) {
            releaseEvenBay(bay);
        } else {
            releaseOddBay(bay);
        }
    }

    public boolean canBear(int bay) {
        if (!isValidBay(bay)) return false;

        if (bay % 2 == 0) {
            return _status[bay] == USED || (_status[bay-1] == USED && _status[bay+1] == USED);
        } else {
            return _status[bay] == USED || (_status[bay-1] == USED && _status[bay+1] == USED);
        }
    }

    public boolean canCover(int bay) {
        return canBear(bay);
    }

    private void setAdjacentBaysToINACTIVE(int bay, int range) {
        for (int i = Math.max(1, bay - range); i <= Math.min(maxBay, bay + range); i++) {
            if (_status[i] == ACTIVE) {
                _status[i] = INACTIVE;
            }
        }
    }
    // 注意不要更改处于‘FORBIDDEN’状态的位置
    private void releaseEvenBay(int bay) {
        for (int i = Math.max(1, bay - 1); i <= Math.min(maxBay, bay + 1); i++) {
            if (_status[i] != FORBIDDEN) _status[i] = ACTIVE;
        }

        if (bay - 3 > 0 && _status[bay - 3] == ACTIVE) {
            if (_status[bay - 2] != FORBIDDEN) _status[bay - 2] = ACTIVE;
        }
        if (bay + 2 < maxBay && _status[bay + 3] == ACTIVE) {
            if (_status[bay + 2] != FORBIDDEN) _status[bay + 2] = ACTIVE;
        }
    }

    private void releaseOddBay(int bay) {
        _status[bay] = ACTIVE;

        if (_status[bay - 1] != FORBIDDEN && bay - 2 > 0 && _status[bay - 2] == ACTIVE) {
            _status[bay - 1] = ACTIVE;
        }
        if (_status[bay + 1] != FORBIDDEN && bay + 1 < maxBay && _status[bay + 2] == ACTIVE) {
            _status[bay + 1] = ACTIVE;
        }
    }

    public boolean isEmpty() {
        for (int i = 1; i <= maxBay; i++) {
            if (_status[i] == USED) {
                return false; // 存在货物，返回 false
            }
        }
        return true; // 完全无货物，返回 true
    }
    private boolean isValidBay(int bay) {
        return bay > 0 && bay <= maxBay;
    }

    public boolean isAvailable(int bay) {
        return isValidBay(bay) && _status[bay] == ACTIVE;
    }

    public boolean isBooked(int bay) {
        return isValidBay(bay) && _status[bay] == BOOKED;
    }
    public boolean hasGoods(int bay) {
        return isValidBay(bay) && _status[bay] == USED;
    }
    private boolean shouldSkipBasedOnRule(int bay) {
        if(bayRule == ANY_RULE) return false;
        if(bayRule == ONLY_EVEN_RULE) return bay % 2 != 0;
        if(bayRule == ONLY_ODD_RULE) return bay % 2 == 0;
        if(bayRule == FOUR_N_MINUS_TWO_RULE) return bay % 2 == 0 && (bay + 2) % 4 != 0;
        if(bayRule == FOUR_N_RULE) return bay % 2 == 0 &&  (bay + 2) % 4 == 0;
        return true;
    }

    public String getStatusDetails() {
        return getStatusDetails(0);
    }

    public String getStatusDetails(int detail) {
        StringBuilder smallBuf = new StringBuilder();
        StringBuilder largeBuf = new StringBuilder();

        for (int i = 1; i <= maxBay; i++) {
            if (detail == 0) {
                if (_status[i] == FORBIDDEN || _status[i] == INACTIVE) continue;
            }else if (detail == 1) {
                if (_status[i] == FORBIDDEN) continue;
            }
            if (i % 2 == 1) {
                appendStatus(smallBuf, i);
            } else {
                appendStatus(largeBuf, i);
            }
        }

        return String.format("Bay状态：\n-小仓位:\n%s\n-大仓位:\n%s",
                smallBuf, largeBuf);
    }

    private void appendStatus(StringBuilder buffer, int bay) {
        if (!buffer.isEmpty()) {
            buffer.append(",");
        }
        buffer.append(String.format("Bay[%d]=%d", bay, _status[bay]));
    }

    @Override
    public String toString() {
        return String.format("wz.BLCSpace.BayStatus {maxBay=%d, rule=%d}", maxBay, bayRule);
    }

}
