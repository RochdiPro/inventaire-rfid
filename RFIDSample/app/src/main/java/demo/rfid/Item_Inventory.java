package demo.rfid;

/**
 * Created by NG on 2016-12-15.
 */
public class Item_Inventory {

    private String mDataType;
    private String mDataValue;
    private int mDataCount;

    Item_Inventory(String sDataType, String sDataValue, int nCount) {
        mDataType = sDataType;
        mDataValue = sDataValue;
        mDataCount = nCount;
    }

    public String getDataType() {
        return mDataType;
    }

    public String getDataValue() {
        return mDataValue;
    }

    public int getDataCount() {
        return mDataCount;
    }

    public void setDataCount(int nCount) {
        mDataCount = nCount;
    }
}
