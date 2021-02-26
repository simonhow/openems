package io.openems.edge.meter.fronius;

import io.openems.edge.common.test.AbstractComponentConfig;
import io.openems.edge.meter.api.MeterType;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
		private String accessKeyID;
		private String accessKeyValue;
		private String pvSystemID;
		public MeterType type;
		private String refreshInterval;
//		private String setting0;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}
		
		public Builder setAccessKeyID(String accessKeyID) {
			this.accessKeyID = accessKeyID;
			return this;
		}
		
		public Builder setAccessKeyValue(String accessKeyValue) {
			this.accessKeyValue = accessKeyValue;
			return this;
		}
		
		public Builder setPvSystemID(String pvSystemID) {
			this.pvSystemID = pvSystemID;
			return this;
		}
		
		public Builder setRefreshInterval(String refreshInterval) {
			this.refreshInterval = refreshInterval;
			return this;
		}

//		public Builder setSetting0(String setting0) {
//			this.setting0 = setting0;
//			return this;
//		}

		public MyConfig build() {
			return new MyConfig(this);
		}
	}

	/**
	 * Create a Config builder.
	 * 
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}
	
	@Override
	public String accessKeyID() {
		return this.accessKeyID();
	}

	@Override
	public String accessKeyValue() {
		return this.accessKeyValue();
	}

	@Override
	public String pvSystemID() {
		return this.pvSystemID();
	}

	@Override
	public String refreshInterval() {
		return this.refreshInterval();
	}

//	@Override
//	public String setting0() {
//		return this.builder.setting0;
//	}

}