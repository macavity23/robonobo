package com.robonobo.core.api.config;

import java.io.Serializable;

public class PluginConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	Class pluginClass;
	boolean enabled = true;

	public PluginConfig() {
	}

	public Class getPluginClass() {
		return pluginClass;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setPluginClass(Class pluginClass) {
		this.pluginClass = pluginClass;
	}
}
