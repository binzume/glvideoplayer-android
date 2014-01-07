package net.binzume.android.nicovideo.webapi;

@SuppressWarnings("serial")
public class PaymentRequiredException extends WebApiException {
	
	public final int price;
	public final int availablePoint;

	public PaymentRequiredException(String message, int price, int availablePoint) {
		super(PAYMENT_REQUIRED, message);
		this.price = price;
		this.availablePoint = availablePoint;
	}

}
