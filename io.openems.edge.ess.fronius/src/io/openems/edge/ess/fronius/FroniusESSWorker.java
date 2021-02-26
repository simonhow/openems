package io.openems.edge.ess.fronius;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.worker.AbstractWorker;

public class FroniusESSWorker extends AbstractWorker {
	
	private final Logger log = LoggerFactory.getLogger(FroniusESSWorker.class);
	private final FroniusESS parent;
	private final SolarwebAPIClientESS client;
	private final int requestIntervalSeconds;
	
	public FroniusESSWorker(FroniusESS parent, Config config) {
		this.parent = parent;
		this.requestIntervalSeconds = Integer.parseInt(config.refreshInterval());
		this.client = new SolarwebAPIClientESS(config.accessKeyID(), config.accessKeyValue(), config.pvSystemID());
		try {
			this.parent._setCapacity(this.client.getCapacity());
			this.forever();
		} catch (OpenemsNamedException e) {
			this.parent.logError(this.log, "Could not retrieve FroniusESS capacity: " + e.getMessage());
		}
	}

	@Override
	protected void forever() {
		try {
			this.client.updateFlowdata();
			this.parent._setActivePower(this.client.getActivePower());
			this.parent._setSoc(this.client.getSoC());
			this.parent._setGridMode(this.client.getGridMode());
		} catch (OpenemsNamedException e) {
			this.parent.logError(this.log, "Could not update FroniusESS channels: " + e.getMessage());
		}
	}

	@Override
	protected int getCycleTime() {
		return this.requestIntervalSeconds * 1000;
	}
}
