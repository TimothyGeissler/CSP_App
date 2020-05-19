package za.co.cspapp.viewholders;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import za.co.cspapp.R;
import za.co.cspapp.StockActivity;
import za.co.cspapp.objects.DealerObject;
import za.co.cspapp.objects.StockObject;


public class StocksViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public StockObject stock;
    public int stock_id;
    public TextView trim;
    public TextView stock_num;
    public TextView yrMileage;
    public TextView price;
    public ImageView thumb;
    public ArrayList<DealerObject> dealers;
    public DealerObject current_dealer;

    public StocksViewHolders(View itemView) {
        super(itemView);

        trim = itemView.findViewById(R.id.txt_trim);
        stock_num = itemView.findViewById(R.id.txt_stockNo);
        yrMileage = itemView.findViewById(R.id.txt_yrMileage);
        price = itemView.findViewById(R.id.txt_price);
        thumb = itemView.findViewById(R.id.thumb);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("stock", stock);
        bundle.putSerializable("dealers", dealers);
        bundle.putSerializable("current_dealer", current_dealer);
        Intent intent = new Intent(view.getContext(), StockActivity.class);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }
}