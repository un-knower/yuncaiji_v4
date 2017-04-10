package cn.uway.ucloude.configuration.auto;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;

public class AutoConfigContextBuilder {
	private static final ILogger LOGGER = LoggerManager.getLogger(AutoConfigContextBuilder.class);

    private Map<String, PropertyDescriptor> nameDescriptorMap;
    private Map<PropertyDescriptor, Set<String>> descriptorNameMap;
    private Object targetObj;
    private Map<String, String> propMap;
    private String prefix;
    private List<PropertyDescriptor> propertyDescriptors;

    public AutoConfigContextBuilder setTargetObj(Object targetObj) {
        this.targetObj = targetObj;
        return this;
    }

    public AutoConfigContextBuilder setPropMap(Map<String, String> propMap) {
        this.propMap = propMap;
        return this;
    }

    public AutoConfigContextBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public AutoConfigContext build() {

        buildPropertyDescriptors();

        buildNameDescriptorMap(prefix, propertyDescriptors);

        return new AutoConfigContext(nameDescriptorMap, descriptorNameMap, propMap, targetObj);
    }

    private void buildNameDescriptorMap(String prefix, List<PropertyDescriptor> propertyDescriptors) {

        Iterable<String> prefixes = (StringUtil.hasLength(prefix) ? new RelaxedNames(prefix) : null);

        nameDescriptorMap = new LinkedHashMap<String, PropertyDescriptor>();
        descriptorNameMap = new HashMap<PropertyDescriptor, Set<String>>();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            String name = descriptor.getName();
            if (!name.equals("class")) {
                RelaxedNames relaxedNames = RelaxedNames.forCamelCase(name);
                if (prefixes == null) {
                    for (String relaxedName : relaxedNames) {
                        nameDescriptorMap.put(relaxedName, descriptor);
                        Set<String> names = CollectionUtil.newHashSetOnNull(descriptorNameMap.get(descriptor));
                        names.add(relaxedName);
                        if (!descriptorNameMap.containsKey(descriptor)) {
                            descriptorNameMap.put(descriptor, names);
                        }
                    }
                } else {
                    for (String _prefix : prefixes) {
                        for (String relaxedName : relaxedNames) {
                            String name1 = _prefix + "." + relaxedName;
                            String name2 = _prefix + "_" + relaxedName;
                            nameDescriptorMap.put(name1, descriptor);
                            nameDescriptorMap.put(name2, descriptor);

                            Set<String> names = CollectionUtil.newHashSetOnNull(descriptorNameMap.get(descriptor));
                            names.add(name1);
                            names.add(name2);
                            if (!descriptorNameMap.containsKey(descriptor)) {
                                descriptorNameMap.put(descriptor, names);
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildPropertyDescriptors() {
        propertyDescriptors = new ArrayList<PropertyDescriptor>();
        Class<?> clazz = targetObj.getClass();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Introspector.IGNORE_ALL_BEANINFO);
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (Class.class == clazz &&
                        ("classLoader".equals(pd.getName()) || "protectionDomain".equals(pd.getName()))) {
                    // Ignore Class.getClassLoader() and getProtectionDomain() methods - nobody needs to bind to those
                    continue;
                }
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Found bean property '" + pd.getName() + "'" +
                            (pd.getPropertyType() != null ? " of type [" + pd.getPropertyType().getName() + "]" : "") +
                            (pd.getPropertyEditorClass() != null ?
                                    "; editor [" + pd.getPropertyEditorClass().getName() + "]" : ""));
                }
                propertyDescriptors.add(pd);
            }

        } catch (IntrospectionException e) {
            throw new PropertiesConfigurationResolveException(e);
        }
    }
}
