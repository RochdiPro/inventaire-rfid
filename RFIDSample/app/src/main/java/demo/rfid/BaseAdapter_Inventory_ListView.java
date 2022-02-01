package demo.rfid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by NG on 2016-12-15.
 */
public class BaseAdapter_Inventory_ListView extends BaseAdapter {
    private Item_Inventory itemInventory;
    private Context mContext;
    private TextView textViewDataType;
    private TextView textViewDataValue;
    private TextView textViewDataCount;

    private ArrayList<Item_Inventory> inventoryArrayList;

    public BaseAdapter_Inventory_ListView(Context context) {
        super();
        mContext = context;
        inventoryArrayList = new ArrayList<Item_Inventory>();
    }

    @Override
    public int getCount() {
        return inventoryArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return inventoryArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            v = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_inventory_row, null);
        }

        textViewDataType = (TextView) v.findViewById(R.id.textView_type);
        textViewDataValue = (TextView) v.findViewById(R.id.textView_data);
        textViewDataCount = (TextView) v.findViewById(R.id.textView_count);

        itemInventory = (Item_Inventory) getItem(position);

        if (itemInventory != null) {
            textViewDataType.setText(itemInventory.getDataType());
            textViewDataValue.setText(itemInventory.getDataValue());
            textViewDataCount.setText(String.valueOf(itemInventory.getDataCount()));
        }
        return v;
    }

    public void addInventoryItem(Item_Inventory item) {
        for (int i = 0; i < getCount(); i++) {
            if (item.getDataValue().equals(inventoryArrayList.get(i).getDataValue())) {
                int nCount = Integer.parseInt(String.valueOf(inventoryArrayList.get(i).getDataCount()));
                inventoryArrayList.get(i).setDataCount(nCount + 1);
                return;
            }
        }
        inventoryArrayList.add(item);
    }

    public void clear() {
        inventoryArrayList.clear();
    }
}
