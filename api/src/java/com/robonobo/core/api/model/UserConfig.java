package com.robonobo.core.api.model;

import java.util.HashMap;
import java.util.Map;

import com.robonobo.core.api.proto.CoreApi.UserConfigItem;
import com.robonobo.core.api.proto.CoreApi.UserConfigMsg;

public class UserConfig {
	private long userId;
	private Map<String, String> items = new HashMap<String, String>();
	
	public UserConfig() {
	}

	public UserConfig(UserConfigMsg msg) {
		this.userId = msg.getUserId();
		for (UserConfigItem item : msg.getItemList()) {
			items.put(item.getItemName(), item.getItemValue());
		}
	}
	
	public UserConfigMsg toMsg() {
		UserConfigMsg.Builder b = UserConfigMsg.newBuilder();
		b.setUserId(userId);
		for (String iName : items.keySet()) {
			UserConfigItem.Builder ib = UserConfigItem.newBuilder();
			ib.setItemName(iName);
			ib.setItemValue(items.get(iName));
			b.addItem(ib.build());
		}
		return b.build();
	}
	
	public void mergeFrom(UserConfig cfg) {
		for (String keyName : cfg.items.keySet()) {
			putItem(keyName, cfg.getItem(keyName));
		}
	}
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Map<String, String> getItems() {
		return items;
	}

	public void setItems(Map<String, String> items) {
		this.items = items;
	}
	
	public String getItem(String key) {
		return items.get(key);
	}
	
	public void putItem(String key, String value) {
		items.put(key, value);
	}
}
