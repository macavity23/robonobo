package com.robonobo.midas.dao;

import java.util.List;

import com.robonobo.midas.model.MidasStream;

public interface StreamDao {

	public abstract void deleteStream(MidasStream stream);

	public abstract List<MidasStream> findLatest(int limit);

	public abstract MidasStream loadStream(String streamId);

	public abstract void saveStream(MidasStream stream);

}