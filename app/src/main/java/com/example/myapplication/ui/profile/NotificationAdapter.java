package com.example.myapplication.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.model.Notification;

import java.util.List;

/**
 * 通知列表适配器
 */
public class NotificationAdapter extends BaseAdapter {

    private Context context;
    private List<Notification> notificationList;
    private LayoutInflater inflater;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }

    @Override
    public Object getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_notification, parent, false);
            holder = new ViewHolder();
            holder.ivIcon = convertView.findViewById(R.id.iv_notification_icon);
            holder.tvTitle = convertView.findViewById(R.id.tv_notification_title);
            holder.tvContent = convertView.findViewById(R.id.tv_notification_content);
            holder.tvTime = convertView.findViewById(R.id.tv_notification_time);
            holder.ivReadStatus = convertView.findViewById(R.id.iv_read_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        // 获取当前通知
        Notification notification = notificationList.get(position);
        
        // 设置通知图标
        switch (notification.getNotificationType()) {
            case "TASK":
                holder.ivIcon.setImageResource(R.drawable.ic_task);
                break;
            case "ABNORMAL":
                holder.ivIcon.setImageResource(R.drawable.ic_warning);
                break;
            case "FOLLOW_UP":
                holder.ivIcon.setImageResource(R.drawable.ic_follow_up);
                break;
            default:
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
                break;
        }
        
        // 设置通知标题和内容
        holder.tvTitle.setText(notification.getTitle());
        holder.tvContent.setText(notification.getContent());
        holder.tvTime.setText(notification.getCreateTime());
        
        // 设置已读/未读状态
        if (notification.getIsRead() == 0) {
            // 未读
            holder.ivReadStatus.setVisibility(View.VISIBLE);
        } else {
            // 已读
            holder.ivReadStatus.setVisibility(View.GONE);
        }
        
        return convertView;
    }
    
    /**
     * ViewHolder模式，优化ListView性能
     */
    static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        ImageView ivReadStatus;
    }
} 