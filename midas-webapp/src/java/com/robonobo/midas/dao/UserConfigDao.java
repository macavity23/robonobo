package com.robonobo.midas.dao;

import java.util.List;

import com.robonobo.midas.model.MidasUserConfig;

public interface UserConfigDao {

	public abstract MidasUserConfig getUserConfig(long userId);

	public MidasUserConfig getUserConfig(String key, String value);
	
	public abstract void saveUserConfig(MidasUserConfig config);

	public abstract void deleteUserConfig(long userId);

	public abstract List<MidasUserConfig> getUserConfigsWithKey(String key);

}