USE singh4;

DROP TABLE IF EXISTS `PurchaseOrderDetails`;
DROP TABLE IF EXISTS `PurchaseOrder`;

CREATE TABLE `singh4`.`PurchaseOrder` (
  `OrderID` INT NOT NULL AUTO_INCREMENT,
  `SupplierID` INT NULL,
  `ArrivedDate` DATE NULL,
  `Track_ID` VARCHAR(40) NULL,
  `Shipper_ID` INT NULL,
  PRIMARY KEY (`OrderID`),
  CONSTRAINT `SupplierID`
    FOREIGN KEY (`SupplierID`)
    REFERENCES `singh4`.`suppliers` (`SupplierID`),
    CONSTRAINT `Shipper_ID` FOREIGN KEY (`Shipper_ID`) REFERENCES `singh4`.`shippers`(`ShipperID`));
	
	CREATE TABLE `singh4`.`PurchaseOrderDetails` (
  `OrderID` INT NOT NULL,
  `ProductID` INT NOT NULL,
  `PurchasePrice` DOUBLE NULL,
  `QuantityPerUnit` VARCHAR(45) NULL,
  `OrderedUnits` INT NULL,
  PRIMARY KEY (`OrderID`, `ProductID`),
  CONSTRAINT `OrderID`
    FOREIGN KEY (`OrderID`)
    REFERENCES `singh4`.`PurchaseOrder` (`OrderID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ProductID`
    FOREIGN KEY (`ProductID`)
    REFERENCES `singh4`.`products` (`ProductID`));
    
    
    