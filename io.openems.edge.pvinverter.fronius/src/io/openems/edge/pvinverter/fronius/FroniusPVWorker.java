package io.openems.edge.pvinverter.fronius;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.worker.AbstractWorker;

public class FroniusPVWorker extends AbstractWorker {
	
	private final Logger log = LoggerFactory.getLogger(FroniusPVWorker.class);
	private final FroniusPV parent;
	private final SolarwebAPIClientPV client;
	private final int requestIntervalSeconds;
	
	public FroniusPVWorker(FroniusPV parent, Config config) {
		this.parent = parent;
		this.requestIntervalSeconds = Integer.parseInt(config.refreshInterval());
		this.client = new SolarwebAPIClientPV(config.accessKeyID(), config.accessKeyValue(), config.pvSystemID());
		try {
			this.parent._setMaxActivePower(this.client.getMaxPowerPV());
			this.forever();
		} catch (OpenemsNamedException e) {
			this.parent.logError(this.log, "Could not retrieve FroniusESS capacity: " + e.getMessage());
		}
	}

	@Override
	protected void forever() {
		try {
			this.client.updateFlowdata();
			this.parent._setActivePower(this.client.getPowerPV());
		} catch (OpenemsNamedException e) {
			this.parent.logError(this.log, "Could not update FroniusPV channels: " + e.getMessage());
		}
	}

	@Override
	protected int getCycleTime() {
		return this.requestIntervalSeconds * 1000;
	}
}
