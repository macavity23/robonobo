package com.robonobo.common.serialization;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.exceptions.SeekInnerCalmException;

/**
 * [De]Serializes a config bean to/from an easy-to-read text file. Note, config
 * beans can only have string, int, long, double or boolean properties
 */
public class ConfigBeanSerializer {
	Log log;

	public ConfigBeanSerializer() {
		this(true);
	}
	
	public ConfigBeanSerializer(boolean useLogging) {
		if(useLogging)
			log = LogFactory.getLog(getClass());
	}

	public void serializeConfig(Object bean, File outFile) throws IOException {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
			StringBuffer sb = new StringBuffer();
			for (PropertyDescriptor propDesc : beanInfo.getPropertyDescriptors()) {
				if(propDesc.getName().equals("class"))
					continue;
				if(!propTypeOk(propDesc.getPropertyType()))
					throw new IOException("Invalid property type for property '"+propDesc.getName()+"' in bean class "+bean.getClass());
				Object propVal = propDesc.getReadMethod().invoke(bean);
				if(propVal == null)
					continue;
				sb.append(propDesc.getName());
				sb.append("=");
				sb.append(propVal);
				sb.append("\n");
			}
			PrintWriter out = new PrintWriter(outFile);
			out.print(sb.toString());
			out.close();
		} catch (IntrospectionException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (InvocationTargetException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		}
	}

	public <T> T deserializeConfig(Class<T> beanClass, File inFile) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
		T bean;
		Pattern cfgPat = Pattern.compile("^(.+?)=(.*)$");
		Pattern commentPat = Pattern.compile("^\\s*#.*$");
		try {
			bean = beanClass.newInstance();
			Map<String, PropertyDescriptor> propsByName = mapPropsByName(beanClass);
			String line;
			while((line = in.readLine()) != null) {
				if(line.trim().length() == 0)
					continue;
				if(commentPat.matcher(line).matches())
					continue;
				Matcher m = cfgPat.matcher(line);
				if(!m.matches())
					throw new IOException("File "+inFile.getAbsolutePath()+" is in unexpected format, line: "+line);
				String propName = m.group(1);
				String propVal = m.group(2);
				if(propVal.length() == 0)
					continue;
				PropertyDescriptor prop = propsByName.get(propName);
				if(prop == null) {
					if(log != null)
						log.error("Error deserializing config "+inFile.getAbsolutePath()+": unknown property name "+propName);
					continue;
				}
				if(!propTypeOk(prop.getPropertyType()))
					throw new IOException("Invalid property type for property '"+prop.getName()+"' in bean class "+bean.getClass());
				prop.getWriteMethod().invoke(bean, getPropValue(prop.getPropertyType(), propVal));
			}
		} catch (InstantiationException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (IntrospectionException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (InvocationTargetException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		}
		return bean;
	}

	/**
	 * Overrides this config bean with environment variables of the form 'cfg_<beanName>_<propName>'
	 */
	public void overrideCfgFromEnv(Object bean, String beanName) throws IOException {
		Map<String, String> env = System.getenv();
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
			Map<String, PropertyDescriptor> propsByName = mapPropsByName(bean.getClass());
			Pattern cfgNamePat = Pattern.compile("^cfg_(.+)_(.+)$");
			for (String envName : env.keySet()) {
				Matcher m = cfgNamePat.matcher(envName);
				if(!m.matches())
					continue;
				String thisBeanName = m.group(1);
				if(!thisBeanName.equalsIgnoreCase(beanName))
					continue;
				String propName = m.group(2);
				String propVal = env.get(envName);
				PropertyDescriptor prop = propsByName.get(propName);
				if(prop == null)
					throw new SeekInnerCalmException("Failed to overwrite config: unknown property "+propName+" in bean "+beanName);
				if(!propTypeOk(prop.getPropertyType()))
					throw new IOException("Invalid property type for property '"+prop.getName()+"' in bean class "+bean.getClass());
				prop.getWriteMethod().invoke(bean, getPropValue(prop.getPropertyType(), propVal));
				if(log != null)
					log.info("Overriding '"+thisBeanName+"' cfg prop '"+propName+"' with value '"+propVal+"'");
			}
		} catch (IllegalAccessException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (IntrospectionException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		} catch (InvocationTargetException e) {
			throw new IOException("Caught "+e.getClass().getName()+" getting beanInfo: "+e.getMessage());
		}
	}

	private Object getPropValue(Class<?> propType, String propValStr) throws IOException {
		Object propVal;
		if (propType.equals(Integer.TYPE) || Integer.class.isAssignableFrom(propType))
			propVal = Integer.valueOf(propValStr);
		else if (propType.equals(Long.TYPE) || Long.class.isAssignableFrom(propType))
			propVal = Long.valueOf(propValStr);
		else if(propType.equals(Double.TYPE) || Double.class.isAssignableFrom(propType))
			propVal = Double.valueOf(propValStr);
		else if (propType.equals(Boolean.TYPE) || Boolean.class.isAssignableFrom(propType))
			propVal = Boolean.valueOf(propValStr);
		else if (String.class.isAssignableFrom(propType))
			propVal = propValStr;
		else
			throw new SeekInnerCalmException();
		return propVal;
	}

	private Map<String, PropertyDescriptor> mapPropsByName(Class<?> clazz) throws IntrospectionException {
		Map<String, PropertyDescriptor> result = new HashMap<String, PropertyDescriptor>();
		BeanInfo bi = Introspector.getBeanInfo(clazz);
		for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
			result.put(pd.getName(), pd);
		}
		return result;
	}

	private boolean propTypeOk(Class<?> propType) {
		if (propType.equals(Integer.TYPE) || Integer.class.isAssignableFrom(propType))
			return true;
		if (propType.equals(Long.TYPE) || Long.class.isAssignableFrom(propType))
			return true;
		if (propType.equals(Boolean.TYPE) || Boolean.class.isAssignableFrom(propType))
			return true;
		if(propType.equals(Double.TYPE) || Double.class.isAssignableFrom(propType))
			return true;
		if (String.class.isAssignableFrom(propType))
			return true;
		return false;
	}

}
