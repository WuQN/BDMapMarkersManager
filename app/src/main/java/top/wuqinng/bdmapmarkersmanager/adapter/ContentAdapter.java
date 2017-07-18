package top.wuqinng.bdmapmarkersmanager.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import top.wuqinng.bdmapmarkersmanager.R;
import top.wuqinng.bdmapmarkersmanager.entity.LocItem;

/**
 * Created by MD-WuQN on 2017/7/18.
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.MyViewHolder> {
    List<LocItem> mData;

    public ContentAdapter(List<LocItem> mData) {
        this.mData = mData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_content, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.txtSerial.setText("标注" + mData.get(position).getSerial());
    }

    public void setNewData(List<LocItem> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtSerial;

        public MyViewHolder(View itemView) {
            super(itemView);
            txtSerial = (TextView) itemView.findViewById(R.id.txt_serial);
        }
    }
}
