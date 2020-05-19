package za.co.cspapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import za.co.cspapp.R;
import za.co.cspapp.objects.DealerObject;
import za.co.cspapp.objects.PhotoObject;
import za.co.cspapp.objects.StockObject;
import za.co.cspapp.utils.DownloadImage;
import za.co.cspapp.viewholders.PhotoViewHolders;

public class PhotoViewAdapter extends RecyclerView.Adapter<PhotoViewHolders> {

    private ArrayList<PhotoObject> photos;
    private StockObject stock;
    private ArrayList<DealerObject> dealers;
    private DealerObject current_dealer;

    public PhotoViewAdapter(StockObject stock, ArrayList<DealerObject> dealers, DealerObject current_dealer) {
        this.stock = stock;
        this.photos = stock.getPhotos();
        this.dealers = dealers;
        this.current_dealer = current_dealer;
    }

    @Override
    public PhotoViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_photo_item, null);
        return new PhotoViewHolders(layoutView);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolders holder, int position) {
        holder.stock = stock;
        holder.photo_id = photos.get(position).getId();
        holder.dealers = dealers;
        holder.current_dealer = current_dealer;
        holder.stock_id = stock.getId();
        new DownloadImage(holder.photo).execute(photos.get(position).getThumb());
    }

    @Override
    public int getItemCount() {
        return this.photos.size();
    }
}