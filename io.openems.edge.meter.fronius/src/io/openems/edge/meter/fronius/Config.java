package io.openems.edge.meter.fronius;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Meter Fronius", //
		description = "")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "froniusGridMeter0";
	
	@AttributeDefinition(name = "Access Key ID", description = "Access Key ID for Fronius Solarweb")
	String accessKeyID() default "";
	
	@AttributeDefinition(name = "Access Key Value", description = "Access Key Value for Fronius Solarweb")
	String accessKeyValue() default "";
	
	@AttributeDefinition(name = "PV System ID", description = "PV System ID in Fronius Solarweb")
	String pvSystemID() default "";
	
	@AttributeDefinition(name = "Refresh Interval", description = "Interval [seconds] to refresh power values (recommended: 60s)")
	String refreshInterval() default "60";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "Fronius Meter0";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	String webconsole_configurationFactory_nameHint() default "Fronius GridMeter [{id}]";

}