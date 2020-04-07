import java.sql.SQLException;

public class OrderException extends Exception {
	int orderNumber;

	public OrderException(int orderid, String errorMessage)
	{
		super(errorMessage);
		this.orderNumber=orderid;
	}
	
	public int getReference() {
		return orderNumber;
	}
}
