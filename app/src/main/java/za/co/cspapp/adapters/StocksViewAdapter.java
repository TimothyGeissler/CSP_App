package za.co.cspapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import za.co.cspapp.R;
import za.co.cspapp.objects.DealerObject;
import za.co.cspapp.objects.PhotoObject;
import za.co.cspapp.objects.StockObject;
import za.co.cspapp.utils.DownloadImage;
import za.co.cspapp.viewholders.StocksViewHolders;

public class StocksViewAdapter extends RecyclerView.Adapter<StocksViewHolders> {


    private ArrayList<StockObject> itemList;
    private ArrayList<DealerObject> dealers;
    private DealerObject current_dealer;
    private Context context;

    public StocksViewAdapter(Context context, ArrayList<StockObject> itemList, ArrayList<DealerObject> dealers, DealerObject current_dealer) {
        this.itemList = itemList;
        this.dealers = dealers;
        this.current_dealer = current_dealer;
        this.context = context;
    }

    @Override
    public StocksViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_stock_item, null);
        return new StocksViewHolders(layoutView);
    }

    @Override
    public void onBindViewHolder(StocksViewHolders holder, int position) {
        holder.stock = itemList.get(position);
        if(itemList.get(position).getHeroPic() != null){
            new DownloadImage(holder.thumb).execute(itemList.get(position).getHeroPic().getThumb());
        } else {

        }
        ArrayList<PhotoObject> photos = itemList.get(position).getPhotos();
        holder.stock_id = itemList.get(position).getId();
        holder.dealers = dealers;
        holder.current_dealer = current_dealer;

        holder.trim.setText(itemList.get(position).getTrim());
        holder.stock_num.setText(itemList.get(position).getStockNum());
        String yrM = itemList.get(position).getYear()+" - "+itemList.get(position).getMileage()+"km";
        holder.yrMileage.setText(yrM);
        String pr = "R"+NumberFormat.getNumberInstance(Locale.UK).format(itemList.get(position).getPrice());
        holder.price.setText(pr);
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}