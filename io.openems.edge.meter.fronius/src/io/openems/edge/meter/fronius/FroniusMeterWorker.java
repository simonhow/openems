package io.openems.edge.meter.fronius;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.worker.AbstractWorker;

public class FroniusMeterWorker extends AbstractWorker {
	
	private final Logger log = LoggerFactory.getLogger(FroniusMeterWorker.class);
	private final FroniusMeter parent;
	private final SolarwebAPIClientMeter client;
	private final int requestIntervalSeconds;
	
	public FroniusMeterWorker(FroniusMeter parent, Config config) {
		this.parent = parent;
		this.requestIntervalSeconds = Integer.parseInt(config.refreshInterval());
		this.client = new SolarwebAPIClientMeter(config.accessKeyID(), config.accessKeyValue(), config.pvSystemID());
		this.forever();
	}

	@Override
	protected void forever() {
		try {
			this.client.updateFlowdata();
			this.parent._setActivePower(this.client.getPowerFeedIn());
		} catch (OpenemsNamedException e) {
			this.parent.logError(this.log, "Could not update FroniusMeter channels: " + e.getMessage());
		}
	}

	@Override
	protected int getCycleTime() {
		return this.requestIntervalSeconds * 1000;
	}
}
