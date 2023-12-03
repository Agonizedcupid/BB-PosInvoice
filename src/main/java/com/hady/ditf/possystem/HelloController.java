package com.hady.ditf.possystem;

import com.hady.ditf.possystem.model.Product;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.printing.PDFPageable;


import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;
import static org.apache.pdfbox.pdmodel.font.PDType1Font.*;

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

    @FXML
    private Label totalField;
    @FXML
    private Label discountField;
    @FXML
    private Label payableField;
    @FXML
    private TextField cashReceivedField;
    @FXML
    private Label changeField;

    @FXML
    private void printStudentInvoice() {
        if (validateInput()) {
            runPrintTask(true); // true for student invoice
        } else {
            // Show error message or handle invalid input
            showAlert("Invalid Input", "Invalid input or insufficient cash received.");
        }
    }

    @FXML
    private void printInventoryInvoice() {
        if (validateInput()) {
            runPrintTask(false); // false for inventory invoice
        } else {
            // Show error message or handle invalid input
            showAlert("Invalid Input", "Invalid input or insufficient cash received.");
        }
    }

    private double total = 0.0; // Total amount
    private static final double DISCOUNT_RATE = 0.05; // 5% discount

    private int productCount = 0;



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

        double fixedWidth = 200.0;

        double numColumns = 4.0; // Adjust this number based on your actual number of columns
        //serialNoColumn.prefWidthProperty().bind(tableView.widthProperty().divide(10));
        serialNoColumn.setPrefWidth(fixedWidth);
        mrpColumn.setPrefWidth(fixedWidth);
        quantityColumn.setPrefWidth(fixedWidth);
        // Dynamic widths for name and total columns
        double remainingWidthFactor = 2; // Adjust this factor based on your layout needs
        nameColumn.prefWidthProperty().bind(tableView.widthProperty()
                .subtract(fixedWidth * 3) // Subtracting the fixed widths
                .divide(remainingWidthFactor)); // Divide the remaining space

        totalColumn.prefWidthProperty().bind(tableView.widthProperty()
                .subtract(fixedWidth * 3) // Subtracting the fixed widths
                .divide(remainingWidthFactor));

        tableView.setItems(data);

        barcodeInput.setOnAction(event -> {
            String barcode = barcodeInput.getText();
            String prefixCode = barcode.substring(0, 2);
            String name = productName.getOrDefault(prefixCode, "Unknown Product");

            double mrp = 100.0; // Example MRP
            int quantity = 1; // Example quantity
            String serialNo = String.valueOf(data.size() + 1);

            Product newProduct = new Product(serialNo, mrp, quantity, name);
            data.add(newProduct);
            tableView.setItems(data);

            addProduct(prefixCode, mrp); // Update totals and discounts
            barcodeInput.clear(); // Clear the field for the next scan
        });

        cashReceivedField.setOnAction(event -> {
            double cashReceived = Double.parseDouble(cashReceivedField.getText());
            calculateChange(cashReceived);
        });

    }

    public void addProduct(String productCode, double productPrice) {
        productCount++;
        total += productPrice;

        totalField.setText(String.format("%.2f", total));

        double discount = calculateDiscount(productCode);
        discountField.setText(String.format("%.2f", discount));

        double payable = total - discount;
        payableField.setText(String.format("%.2f", payable));
    }

    private double calculateDiscount(String productCode) {
        if ("16".equals(productCode)) {
            return total * 0.10; // 10% discount for specific product
        }

        return total * (productCount > 1 ? 0.10 : 0.05); // 10% for more than one product, otherwise 5%
    }

    private void calculateChange(double cashReceived) {
        try {
            double payable = Double.parseDouble(payableField.getText());
            if (cashReceived < payable) {
                // Display warning message
                changeField.setText("Insufficient cash received!");
            } else {
                // Calculate and display change
                double change = cashReceived - payable;
                changeField.setText(String.format("%.2f", change));
            }
        } catch (NumberFormatException e) {
            // Handle invalid input
            changeField.setText("Invalid input");
        }
    }

    private String createInvoiceContent(boolean isStudentInvoice) {
        StringBuilder invoiceContent = new StringBuilder();
        invoiceContent.append(isStudentInvoice ? "Student Invoice\n" : "Inventory Invoice\n");
        invoiceContent.append("--------------------------------------------------\n");

        // Debugging: Check if 'data' is empty or null
        System.out.println("Number of products: " + (data == null ? "null" : data.size()));

        for (Product product : data) {
            invoiceContent.append(String.format("Serial No: %s, Name: %s, Price: %.2f, Quantity: %d, Total: %.2f\n",
                    product.getSerialNo(), product.getProductName(),
                    product.getMrp(), product.getQuantity(),
                    product.getTotal()));
        }

        // Add totals, discounts, etc.
        invoiceContent.append(String.format("Total: %.2f\n", total));
        invoiceContent.append(String.format("Discount: %.2f\n", Double.parseDouble(discountField.getText())));
        invoiceContent.append(String.format("Payable: %.2f\n", Double.parseDouble(payableField.getText())));

        // Debugging: Print the invoice content
        System.out.println("Invoice Content: \n" + invoiceContent);

        return invoiceContent.toString();
    }

    private void printInvoice(boolean isStudentInvoice) {
        new Thread(() -> {
            String savedFilePath = saveAsPdf(isStudentInvoice);
            if (savedFilePath != null) {
                printSavedPdfInvoice(savedFilePath);
            }
        }).start();
    }


    private void printSavedPdfInvoice(String filePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document));
            if (job.printDialog()) {
                job.print();
                showAlert("Printing Success", "Invoice printed successfully.");
            }
        } catch (IOException | PrinterException e) {
            e.printStackTrace();
            showAlert("Printing Error", "An error occurred while printing the invoice.");
        }
    }

    private void showPrintOrSaveDialog(boolean isStudentInvoice) {
        Platform.runLater(() -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Print", "Print", "Save as PDF");
            dialog.setTitle("Print or Save");
            dialog.setHeaderText("Select an option:");
            dialog.setContentText("Choose whether to print the invoice or save it as a PDF:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(choice -> {
                if ("Print".equals(choice)) {
                    //printInvoice(isStudentInvoice);
                    printDirectly(isStudentInvoice);
                } else if ("Save as PDF".equals(choice)) {
                    saveAsPdf(isStudentInvoice);
                }
            });
        });
    }


//    private String saveAsPdf(boolean isStudentInvoice) {
//        String invoiceContent = createInvoiceContent(isStudentInvoice);
//
//        // Create a FileChooser dialog to let the user choose the save location and file name
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setInitialFileName(""+System.currentTimeMillis()); // Default file name
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
//
//        // Show the save dialog and get the selected file
//        File file = fileChooser.showSaveDialog(null); // You can pass a parent stage if available
//
//        if (file != null) {
//            String fileName = file.getAbsolutePath();
//
//            // Create PDF with the selected file name
//            createPdfInvoice(invoiceContent, fileName);
//
//            Platform.runLater(() -> showAlert("Invoice Saved", "Invoice saved as PDF: " + fileName));
//            return fileName;
//        }
//        return null;
//    }

    private String saveAsPdf(boolean isStudentInvoice) {
        final String[] filePath = new String[1];
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("Invoice_" + System.currentTimeMillis());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                new Thread(() -> {
                    createPdfInvoice(createInvoiceContent(isStudentInvoice), file.getAbsolutePath());
                    filePath[0] = file.getAbsolutePath();
                    Platform.runLater(() -> showAlert("Invoice Saved", "Invoice saved as PDF: " + file.getAbsolutePath()));
                }).start();
            }
        });
        while (filePath[0] == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return filePath[0];
    }


    private void saveAndThenPrint(boolean isStudentInvoice) {
        Platform.runLater(() -> {
            String filePath = saveAsPdf(isStudentInvoice); // Save the PDF
            if (filePath != null) {
                printSavedPdfInvoice(filePath); // Print the saved PDF
            }
        });
    }

    private void printNode(Node node) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(null)) {
            boolean success = job.printPage(node);
            if (success) {
                job.endJob();
                showAlert("Printing Success", "Invoice printed successfully.");
            }
        }
    }

    private Node createPrintableInvoiceNode(boolean isStudentInvoice) {
        // Create a Node representation of your invoice
        // TODO: Build the invoice layout here
        VBox invoiceLayout = new VBox();
        invoiceLayout.getChildren().add(new Label(createInvoiceContent(isStudentInvoice)));
        return invoiceLayout;
    }


    private void printDirectly(boolean isStudentInvoice) {
        Node invoiceNode = createPrintableInvoiceNode(isStudentInvoice);
        printNode(invoiceNode);
    }
    private void createPdfInvoice(String invoiceContent, String fileName) {
        float rowHeight = 20f; // Define the height of each row in the table

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDFont fontBold = PDType1Font.HELVETICA_BOLD;
                PDFont fontPlain = PDType1Font.HELVETICA;
                float margin = 50;
                PDRectangle pageSize = page.getMediaBox();
                float yStart = pageSize.getHeight() - margin;

                // Draw the header
                drawHeader(contentStream, fontBold, fontPlain, pageSize, yStart);

                // Adjust startY for the table start
                float tableStartY = yStart - 80; // Assuming the header takes 80 points height

                // Draw the table
                drawTable(contentStream, fontBold, fontPlain, margin, tableStartY, pageSize.getWidth() - 2 * margin);

                // Calculate the startY for summary and footer
                float lastRowY = tableStartY - ((data.size() + 1) * rowHeight); // +1 for the header row
                float summaryStartY = lastRowY - margin; // Summary starts after the table
                float footerStartY = summaryStartY - margin; // Footer starts after the summary

                // Draw summary and footer
                drawSummary(contentStream, fontPlain, margin, summaryStartY);
                drawFooter(contentStream, fontPlain, pageSize, margin, footerStartY);

                // Close the content stream and save the PDF
                contentStream.close();
                document.save(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


// Other methods drawHeader, drawTable, drawSummary, and drawFooter should be adjusted accordingly

    private float calculateStringWidth(String text, PDFont font, float fontSize) throws IOException {
        return font.getStringWidth(text) * fontSize / 1000;
    }


    private void drawHeader(PDPageContentStream contentStream, PDFont fontBold, PDFont fontPlain, PDRectangle pageSize, float yStart) throws IOException {
        String title = "OnnoRokom EdTech Limited";
        float titleFontSize = 18;
        float titleWidth = calculateStringWidth(title, fontBold, titleFontSize);
        float startX = (pageSize.getWidth() - titleWidth) / 2; // Center the title

        // Draw the company header in the middle
        contentStream.beginText();
        contentStream.setFont(fontBold, titleFontSize);
        contentStream.newLineAtOffset(startX, yStart);
        contentStream.showText(title);
        contentStream.endText();

        // Company details below the header
        String details = "2/1/E, Eden Center, Motijheel, Dhaka";
        float detailFontSize = 12;
        float detailWidth = calculateStringWidth(details, fontPlain, detailFontSize);
        float detailStartX = (pageSize.getWidth() - detailWidth) / 2;
        contentStream.beginText();
        contentStream.setFont(fontPlain, detailFontSize);
        contentStream.newLineAtOffset(detailStartX, yStart - 20); // Adjust Y offset as needed
        contentStream.showText(details);
        contentStream.endText();

        // Draw a separator line
        contentStream.moveTo(startX, yStart - 35); // Adjust Y offset as needed
        contentStream.lineTo(startX + titleWidth, yStart - 35);
        contentStream.stroke();
    }



    private void drawTable(PDPageContentStream contentStream, PDFont fontBold, PDFont fontPlain, float margin, float startY, float tableWidth) throws IOException {
        // Set up the table columns
        float[] columnWidths = {30, 300, 50, 50, 80}; // SL, Product Name, QTY, Rate, Total
        String[] headers = {"SL", "Product Name", "QTY", "Rate", "Total"};
        float nextx = margin;

        // Draw table headers
        contentStream.setFont(fontBold, 8);
        for (int i = 0; i < headers.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(nextx, startY);
            contentStream.showText(headers[i]);
            contentStream.endText();
            nextx += columnWidths[i];
        }

        // Draw the line below the header
        contentStream.moveTo(margin, startY - 15);
        contentStream.lineTo(margin + tableWidth, startY - 15);
        contentStream.stroke();

        // Now add the product rows below the header
        contentStream.setFont(fontPlain, 8);
        float nexty = startY - 30;
        for (Product product : data) {
            nextx = margin;
            String[] productDetails = {
                    product.getSerialNo(),
                    product.getProductName(),
                    String.valueOf(product.getQuantity()),
                    String.format("%.2f", product.getMrp()),
                    String.format("%.2f", product.getTotal())
            };

            for (int i = 0; i < productDetails.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(nextx, nexty);
                contentStream.showText(productDetails[i]);
                contentStream.endText();
                nextx += columnWidths[i];
            }
            nexty -= 20;
        }

        // Draw line after all products
        contentStream.moveTo(margin, nexty);
        contentStream.lineTo(margin + tableWidth, nexty);
        contentStream.stroke();
    }


    private void drawSummary(PDPageContentStream contentStream, PDFont fontPlain, float startX, float startY) throws IOException {
        // Draw the total, discount, and payable amount
        contentStream.setFont(fontPlain, 8);
        String[] summaryLabels = {"Total", "Discount", "Payable Amount", "Cash Paid", "Change Amount"};
        double[] summaryValues = {
                total, // Calculate this as the sum of all product totals
                Double.parseDouble(discountField.getText()), // Replace with actual discount
                Double.parseDouble(payableField.getText()), // Replace with actual payable amount
                Double.parseDouble(cashReceivedField.getText()), // Replace with actual cash received
                Double.parseDouble(changeField.getText()) // Replace with actual change given
        };

        float nexty = startY;
        for (int i = 0; i < summaryLabels.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, nexty);
            contentStream.showText(summaryLabels[i] + ": " + String.format("%.2f", summaryValues[i]));
            contentStream.endText();
            nexty -= 20;
        }
    }


    private void drawFooter(PDPageContentStream contentStream, PDFont fontPlain, PDRectangle pageSize, float margin, float startY) throws IOException {
        String footerText = "In Word: Taka Nine Thousand Six Hundred Only.";

        // Calculate the width of the footer text to position it on the right
        float footerTextWidth = calculateStringWidth(footerText, fontPlain, 12);
        float footerStartX = pageSize.getWidth() - margin - footerTextWidth; // Right align the footer text

        // Draw the footer text
        contentStream.beginText();
        contentStream.setFont(fontPlain, 12);
        contentStream.newLineAtOffset(footerStartX, startY);
        contentStream.showText(footerText);
        contentStream.endText();
    }

    // Method to convert numbers to words - You need to implement this method according to your needs.
    private String numberToWords(int number) {
        // You can use an existing library or write your own logic to convert numbers to words.
        // ...
        return ""; // Return the number in words
    }





    private void runPrintTask(boolean isStudentInvoice) {
        Task<Void> printTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                showPrintOrSaveDialog(isStudentInvoice);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                //Platform.runLater(() -> showAlert("Printing Complete", "Student Invoice printed successfully."));
            }

            @Override
            protected void failed() {
                super.failed();
                getException().printStackTrace();  // Print the stack trace of the exception
                Platform.runLater(() -> showAlert("Printing Failed", "Failed to print Student Invoice."));
            }
        };

        new Thread(printTask).start();
    }


    private boolean validateInput() {
        try {
            double cashReceived = Double.parseDouble(cashReceivedField.getText());
            double payable = Double.parseDouble(payableField.getText());

            return cashReceived >= payable;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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