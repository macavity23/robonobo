package com.robonobo.midas.client;

interface Request {
	int remaining();
	Params getNextParams();
	void success(Object obj);
	void error(Params p, Exception e);
}
