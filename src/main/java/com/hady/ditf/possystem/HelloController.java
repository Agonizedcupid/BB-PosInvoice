package com.hady.ditf.possystem;

import com.hady.ditf.possystem.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.Map;

public class HelloController {

    @FXML
    private TextField barcodeInput;

    @FXML
    private TableView<Product> tableView;

    @FXML
    private TableColumn<Product, String> serialNoColumn;

    @FXML
    private TableColumn<Product, Double> mrpColumn;

    @FXML
    private TableColumn<Product, Integer> quantityColumn;

    @FXML
    private TableColumn<Product, Double> totalColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    private final ObservableList<Product> data = FXCollections.observableArrayList();

    public static final Map<String, String> productName = new HashMap<>();

    @FXML
    public void initialize() {
        matchProductNameByPrefixCode();
        serialNoColumn.setCellValueFactory(new PropertyValueFactory<>("serialNo"));
        mrpColumn.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        double numColumns = 4.0; // Adjust this number based on your actual number of columns
        serialNoColumn.prefWidthProperty().bind(tableView.widthProperty().divide(numColumns));
        mrpColumn.prefWidthProperty().bind(tableView.widthProperty().divide(numColumns));
        quantityColumn.prefWidthProperty().bind(tableView.widthProperty().divide(numColumns));
        totalColumn.prefWidthProperty().bind(tableView.widthProperty().divide(numColumns));
        nameColumn.prefWidthProperty().bind(tableView.widthProperty().divide(numColumns));

        tableView.setItems(data);

        barcodeInput.setOnAction(event -> {
            String barcode = barcodeInput.getText();
            String prefixCode = barcode.substring(0,2);
            System.out.println("Prefix Code: "+ prefixCode);
            String name = productName.get(prefixCode);
            //System.out.println("Product Name: "+name);

            double mrp = 100.0; // Example MRP
            int quantity = 1; // Example quantity
            int serialNo = data.size() + 1; // Generate serial number

            Product newProduct = new Product(String.valueOf(serialNo), mrp, quantity, name);
            data.add(newProduct);

            barcodeInput.clear(); // Clear the field for the next scan
        });
    }

    public static void matchProductNameByPrefixCode() {
        productName.put("10","ONNOROKOM BIGGANBAKSHO (ALOR + JHALAK)");
        productName.put("40","ONNOROKOM SCIENCE BOX (COLOR OF LIGHT)");
        productName.put("50","ONNOROKOM SCIENCE BOX (MAGIC OF MAGNET)");
        productName.put("60","ONNOROKOM SCIENCE BOX (AMAZING ELECTRICITY)");
        productName.put("90","ONNOROKOM SCIENCE BOX (MYSTERY OF CHEMISTRY)");
        productName.put("11","ONNOROKOM SCIENCE BOX (FUN WITH MEASUREMENT)");
        productName.put("13","ONNOROKOM SCIENCE BOX (SOUND BOUND)");
        productName.put("20","ONNOROKOM BIGGANBAKSHO (CHUMBAKER CHAMAK)");
        productName.put("30","ONNOROKOM BIGGANBAKSHO (TARIT TANDOB)");
        productName.put("70","ONNOROKOM BIGGANBAKSHO (RASHAYON RAHOSSHO)");
        productName.put("80","ONNOROKOM BIGGANBAKSHO (ODVUT MAPJOKH)");
        productName.put("12","ONNOROKOM BIGGANBAKSHO (SHOBDO KOLPO)");
        productName.put("17","SMART KiT- (FOCUS CHALLENGE, BANGLA VERSION )");
        productName.put("18","SMART KiT- (FOCUS CHALLENGE, ENGLISH VERSION )");
        productName.put("15","GIANT PIXEL");
        productName.put("14","MAJAR PERISCOPE");
        productName.put("16","ONNOROKOM BIGGANBAKSHO (CLASS FIVE KiT)");
        productName.put("21","SMART KIT (CAPTAIN QUERIES)");
        productName.put("19","ONNOROKOM BIGGANBAGSHO (MAGNETIC TANGRAM)");
    }


}
