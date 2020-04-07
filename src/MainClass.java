import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

public class MainClass implements inventoryControl {
	String dbName;

	public MainClass() {
		this.dbName = "singh4";
	}

//	public void setup_db() throws ClassNotFoundException, SQLException, FileNotFoundException {
//		Connection connection = getConnection();
//		Statement statement = connection.createStatement();
//		statement.executeQuery("use " + this.dbName + ";");
////		Scanner sc = new Scanner(new File("src/create_db.txt"));
////		statement.execute(sc.next("\\z"));
//		//statement.execute("drop table if exists PurchaseOrder,PurchaseOrderDetails;");
////		statement.execute("create table PurchaseOrder(" + "OrderID INT primary key auto_increment,"
////				+ "SupplierID INT references suppliers.SupplierID," + "ArrivedDate Date,"
////				+ "Shipper_ID INT References shippers.ShipperID," + "Track_ID VARCHAR(40));");
////		statement.execute("create table PurchaseOrderDetails(" + " OrderID INT  references PurchaseOrder.OrderID,"
////				+ "ProductID INT references products.ProductID," + "OrderedUnits INT," + "PurchasePrice DOUBLE,"
////				+ "CONSTRAINT PK primary key(OrderID,ProductID)" + ");");
//	}

	public Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");

		Connection connect = null;
		Properties identity = new Properties(); // Using a properties structure, just to hide info from other users.
		MyIdentity me = new MyIdentity(); // My own class to identify my credentials. Ideally load Properties from a
											// file instead and this class disappears.

		// final String xmlFilePath = "C:\\Users\\User\\Desktop\\xml1.xml";

		String user;
		String password;
		String dbName;

		me.setIdentity(identity); // Fill the properties structure with my info. Ideally, load properties from a
		// file instead to replace this bit.
		user = identity.getProperty("user");
		password = identity.getProperty("password");
		dbName = identity.getProperty("database");

		// Setup the connection with the DB
		connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?serverTimezone=UTC&useSSL=false", user,
				password);

		return connect;
	}

	// this methodreturns the priice of product at which it is bought from supplier
	public double get_price(int product_id, int year, int month, int day) throws ClassNotFoundException, SQLException {
		// this query gives the Unit price of product
		// joins orderdetails and orders

		String q = "select UnitPrice " + "from orderdetails, orders "
				+ "where orderdetails.orderID=orders.orderID and orderdetails.productid=" + product_id
				+ " order by orders.OrderID desc" + " limit 1;";

		Connection connection = getConnection();
		Statement s = connection.createStatement();
		s.execute("use " + this.dbName + " ;");
		ResultSet resultset = s.executeQuery(q);

		while (resultset.next())
			return Float.parseFloat(resultset.getString("UnitPrice")) / 1.15;// as the price at which company sells
																				// the product is 15 per more
		return 0;
	}

	// this method updates shipped date in orders table and Units in Stock in
	// products table
	// parameter order number is OrderId in orderdetails table

	public void Ship_order(int orderNumber) throws OrderException {
		try {
			
			

			Connection connect = getConnection();// creating connection
			Statement statement = connect.createStatement();
			Statement statement2 = connect.createStatement();
			
			
			statement.execute("use " + this.dbName + " ;");
			statement2.execute("use " + this.dbName + " ;");
            String ship="select *from orders where orderid=" +orderNumber+";";
			
			ResultSet resultSet=statement2.executeQuery(ship);
			if(resultSet.next())
			{
				if(resultSet.getString("ShippedDate")!=null)
				{
					throw new OrderException(orderNumber, "Order Already Shipped");
				}
			}
			
			else {
				throw new OrderException(orderNumber,"Invalid Order ID");
			}

			
			Statement statement1=connect.createStatement();
			statement1.executeQuery("use" + dbName + ";");
			
			String query1="select UnitsInstock - Quantity as left from products natural join orderdetails where orderid="+orderNumber+";";
			
			ResultSet resultSet1=statement1.executeQuery(query1);
			while(resultSet1.next())
			{
				int left=resultSet1.getInt("left");
				if(left<0)
				{
					throw new OrderException(orderNumber , "Not Sufficient stock");
				}
			}
			// this query updates the shipped date in orders table to current date where
			// oredre id is ordernumber
			String q = "Update orders " + "SET ShippedDate=Now() " + "where orderid=" + orderNumber + "; ";
			
			statement.execute(q);
			
			
			// this query updates Units in stock column of products table
			// joins products and orderdetails
			// orderNumber is orderID(orderdetails)

			q = "update products,orderdetails " + "set UnitsInStock = UnitsInStock-Quantity "
					+ "where products.ProductId=orderdetails.ProductID and orderID=" + orderNumber + ";";
			statement.execute(q);
			

		} catch (SQLException e) {
			
		} catch (ClassNotFoundException e) {
			
		}

	}

	public String randomTrack(int length) {
		ArrayList<String> chars = new ArrayList();
		for (int i = 0; i < 10; i++)
			chars.add("" + i);
		for (int i = 0; i < 26; i++)
			chars.add("" + ('A' + i));
		Collections.shuffle(chars);
		return String.join("", chars.subList(0, length));

	}

	public int Issue_reorders(int year, int month, int day) {
		try {
			Connection connect = getConnection();
			Statement statement = connect.createStatement();

			statement.execute("use " + this.dbName + " ;");

			// this query joins three tables products,orderdetails,orders
			// we get ProductID,Reorder-level, SupplierId of the products
			// whose order-date is <= given date or shiiped date is <= given date
			// and UnitsInstock<=ReorderLevel and orders which have not been issued but are
			// placed (no need to re-issue)

			String q = "select SupplierId,ReorderLevel,products.ProductID from products  where  "
					+ "UnitsInStock+UnitsOnOrder<=ReorderLevel and products.Discontinued=0;";

//			String q = "select distinct products.ProductID,ReorderLevel,SupplierID "
//					+ "from products,orderdetails,orders "
//					+ "where products.ProductID=orderdetails.ProductID and orderdetails.OrderID=orders.OrderID and "
//					+ "(OrderDate<=STR_TO_DATE('" + day + "," + month + "," + year + "','%d,%m,%Y')"
//					+ "OR ShippedDate<=STR_TO_DATE('" + day + "," + month + "," + year + "','%d,%m,%Y'))"
//					+ " and UnitsInStock<=ReorderLevel and products.Discontinued=0 and "
//					+ "products.ProductID NOT IN (select ProductID from PurchaseOrder Natural Join PurchaseOrderDetails where isnull(ArrivedDate));";
//
			// storing result of query in result set
			ResultSet resultSet = statement.executeQuery(q);
			HashMap<String, ArrayList<String[]>> store = new HashMap();

			while (resultSet.next()) {
				String productID = resultSet.getString("products.ProductID");
				String units2order = resultSet.getString("ReorderLevel");
				units2order = units2order.trim().equals("0") ? "5" : units2order;
				String supplierID = resultSet.getString("SupplierID");
				store.put(supplierID, store.getOrDefault(supplierID, new ArrayList<>()));
				store.get(supplierID).add(new String[] { productID, units2order,
						"" + get_price(Integer.parseInt(productID), year, month, day) });
			}
			//System.out.println(store);
			q = "select ShipperID from shippers;";
			resultSet = statement.executeQuery(q);
			// to add shipper ids
			ArrayList<String> shipperID = new ArrayList<String>();
			while (resultSet.next()) {
				shipperID.add(resultSet.getString("ShipperID"));
			}

			// shuffle shipper ids
			Collections.shuffle(shipperID);
			int shipperid_idx = 0;
			for (String supplierID : store.keySet()) {
				
				
				//System.out.println(supplierID);

				// inserting values in purchase order table
				q = "insert into PurchaseOrder(SupplierID,Shipper_ID,Track_ID) values(" + supplierID + ","
						+ shipperID.get(shipperid_idx++) + " ,'" + randomTrack(20) + "');";
				if (shipperid_idx == shipperID.size())
					shipperid_idx = 0;

				statement.execute(q);
				resultSet = statement.executeQuery("select last_insert_id() as A;");
				resultSet.next();
				String orderID = resultSet.getString("A");
				//System.out.println(q);
				System.out.println(orderID);
//				statement.execute(q);
				for (String[] pid_units_price : store.get(supplierID)) {
				if(Integer.parseInt(pid_units_price[1])==0) {
					pid_units_price[1]="5";
				}
					// inserting values into Purchase Order Details
					statement.execute("insert into PurchaseOrderDetails(OrderID,ProductID,OrderedUnits,PurchasePrice) "
							+ "values(" + orderID + "," + pid_units_price[0] + "," + pid_units_price[1] + " ,"
							+ pid_units_price[2] + ");");
					
					
					
					statement.execute("update products set unitsonorder=unitsonorder+"+Integer.parseInt(pid_units_price[1])+" where productid="+pid_units_price[0]+";");
				}
				
			}
			return shipperid_idx ;
			
			
			//
		} catch (SQLException | ClassNotFoundException e) {
			return 0;
		} 
		
	}

	public void Receive_order(int internal_order_reference) throws OrderException {
		try {
			Connection connect = getConnection();
			Statement statement = connect.createStatement();

			statement.execute("use " + this.dbName + " ;");
             Statement statement2 = connect.createStatement();
             Statement statement3 = connect.createStatement();
         	statement3.execute("use " + this.dbName + " ;");
			
			
			//statement.execute("use " + this.dbName + " ;");
			statement2.execute("use " + this.dbName + " ;");
            String ship="select *from PurchaseOrder where OrderID=" +internal_order_reference+";";
			
			ResultSet resultSet=statement2.executeQuery(ship);
			if(!resultSet.next())
			{
				
				throw new OrderException(internal_order_reference,"Invalid Order ID");
			}

			// set the arrive order in purchase order table to current date where order id
			// is internal_order_ref

			String q = "Update PurchaseOrder " + "SET ArrivedDate=Now() " + "where orderid=" + internal_order_reference
					+ "; ";

			statement.execute(q);

			//

			q = "update products,PurchaseOrder,PurchaseOrderDetails " + "set UnitsInStock = UnitsInStock+OrderedUnits "
					+ "where products.ProductId=PurchaseOrderDetails.ProductID and PurchaseOrder.OrderID=PurchaseOrderDetails.OrderID and "
					+ "PurchaseOrder.OrderID=" + internal_order_reference + ";";
			statement.execute(q);

			q = "update products,PurchaseOrder,PurchaseOrderDetails " + "set UnitPrice = PurchasePrice "
					+ "where products.ProductId=PurchaseOrderDetails.ProductID and PurchaseOrder.OrderID=PurchaseOrderDetails.OrderID and "
					+ "PurchaseOrder.OrderID=" + internal_order_reference + ";";
			statement.execute(q);
			
			q = "update products,PurchaseOrder,PurchaseOrderDetails " + "set UnitsOnOrder = UnitsOnOrder-OrderedUnits "
					+ "where products.ProductId=PurchaseOrderDetails.ProductID and PurchaseOrder.OrderID=PurchaseOrderDetails.OrderID and "
					+ "PurchaseOrder.OrderID=" + internal_order_reference + ";";
			statement.execute(q);
			//
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//	public static void main(String args[]) throws ClassNotFoundException, SQLException, FileNotFoundException {
//		MainClass mc = new MainClass();
//		//mc.setup_db();
//		try {
//			mc.Ship_order(11070);
//		}
//
//catch(Exception e) {
//	e.printStackTrace();
//}
//		System.out.println(mc.Issue_reorders(2020, 04, 06));
//		// mc.Receive_order(1);
//		//mc.Receive_order(2);
////	System.out.println(mc.get_price(1, 1997, 07, 24));
//
//	}

}
