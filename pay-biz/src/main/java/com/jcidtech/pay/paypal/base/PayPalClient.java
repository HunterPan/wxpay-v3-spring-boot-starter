package com.jcidtech.pay.paypal.base;

import com.jcidtech.pay.paypal.properties.PayPalProperties;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
public class PayPalClient {

	/**
	 * Setting up PayPal SDK environment with PayPal Access credentials. For demo
	 * purpose, we are using SandboxEnvironment. In production this will be
	 * LiveEnvironment.
	 */
	private PayPalProperties payPalProperties;

	private PayPalEnvironment environment = null;

	public PayPalClient(PayPalProperties payPalProperties) {
		this.payPalProperties = payPalProperties;
		if(payPalProperties.getMode().equals("sandbox")){
			this.environment = new PayPalEnvironment.Sandbox(payPalProperties.getClientId(),payPalProperties.getSecret());
		}else{
			this.environment = new PayPalEnvironment.Live(payPalProperties.getClientId(),payPalProperties.getSecret());
		}
	}
	/**
	 * Method to get client object
	 *
	 * @return PayPalHttpClient client
	 */
	public PayPalHttpClient client() {
		return new PayPalHttpClient(environment);
	}

	public PayPalProperties properties() {
		return payPalProperties;
	}
}
