package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.beans.property.*;
import java.io.FileOutputStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;


public class InvoiceGenerator extends Application {
    private TableView<Item> table;
    private javafx.scene.control.TextField customerInput, invoiceNumberInput;
    private Label totalLabel;
    private ObservableList<Item> itemList = FXCollections.observableArrayList();

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis(); // Generates a unique invoice number
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Invoice Generator");

        // Layout for Customer Details
        GridPane customerGrid = new GridPane();
        customerGrid.setPadding(new Insets(10));
        customerGrid.setHgap(10);
        customerGrid.setVgap(8);

        Label invoiceLabel = new Label("Invoice Number:");
        invoiceNumberInput = new javafx.scene.control.TextField();
        invoiceNumberInput.setText(generateInvoiceNumber());
        invoiceNumberInput.setEditable(false);



        Label customerLabel = new Label("Customer Name:");
        customerInput = new javafx.scene.control.TextField();
        customerInput.setPromptText("Enter customer name");

        customerGrid.add(invoiceLabel, 0, 0);
        customerGrid.add(invoiceNumberInput, 1, 0);
        customerGrid.add(customerLabel, 0, 1);
        customerGrid.add(customerInput, 1, 1);

        // Table for Items
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Auto resize
        table.setPrefHeight(200); // Set height for visibility
        VBox.setVgrow(table, Priority.ALWAYS); // Allow table to expand

        TableColumn<Item, String> itemNameCol = new TableColumn<>("Item Name");
        itemNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Item, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<Item, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        TableColumn<Item, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty().asObject());


        table.getColumns().addAll(itemNameCol, quantityCol, priceCol, subtotalCol);


        // Inputs for Adding Items
        javafx.scene.control.TextField itemNameInput = new javafx.scene.control.TextField();
        itemNameInput.setPromptText("Item Name");

        javafx.scene.control.TextField quantityInput = new javafx.scene.control.TextField();
        quantityInput.setPromptText("Quantity");

        javafx.scene.control.TextField priceInput = new javafx.scene.control.TextField();
        priceInput.setPromptText("Price");

        Button addButton = new Button("Add Item");
        Button removeButton = new Button("Remove Selected");

        addButton.setOnAction(e -> addItem(itemNameInput, quantityInput, priceInput));
        removeButton.setOnAction(e -> removeItem());

        HBox itemControls = new HBox(10, itemNameInput, quantityInput, priceInput, addButton, removeButton);
        itemControls.setPadding(new Insets(10));

        // Total and Generate PDF
        totalLabel = new Label("Total: ₹0.00");
        Button generatePdfButton = new Button("Generate PDF");
        generatePdfButton.setOnAction(e -> generatePDF());

        HBox bottomControls = new HBox(20, totalLabel, generatePdfButton);
        bottomControls.setPadding(new Insets(10));
        bottomControls.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, customerGrid, table, itemControls, bottomControls);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void addItem(javafx.scene.control.TextField itemName, javafx.scene.control.TextField quantity, javafx.scene.control.TextField price) {
        try {
            String name = itemName.getText();
            int qty = Integer.parseInt(quantity.getText());
            double pricePerUnit = Double.parseDouble(price.getText());
            double subtotal = qty * pricePerUnit;

            Item newItem = new Item(name, qty, pricePerUnit, subtotal);
            itemList.add(newItem);
            table.setItems(itemList);
            table.refresh();

            updateTotal();

            itemName.clear();
            quantity.clear();
            price.clear();
        } catch (NumberFormatException e) {
            showAlert("Invalid input! Please enter valid numbers.");
        }
    }

    private void removeItem() {
        Item selectedItem = table.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            table.getItems().remove(selectedItem);
            itemList.remove(selectedItem);
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = itemList.stream().mapToDouble(Item::getSubtotal).sum();
        totalLabel.setText("Total: ₹" + String.format("%.2f", total));
    }

    private void generatePDF() {
        try {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            String savePath = System.getProperty("user.home") + "/Documents/Invoice_" + System.currentTimeMillis() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(savePath));

            document.open();

            // Add Company Header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLUE);
            Paragraph header = new Paragraph("ABC Enterprises", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
            Paragraph subHeader = new Paragraph("123 Business St, Pune, India | Phone: 123-456-7890", subHeaderFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);
            document.add(new Paragraph("\n"));
            document.add(new LineSeparator());
            document.add(new Paragraph("\n"));

            // Invoice Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Invoice Details
            Font detailsFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            PdfPTable invoiceDetailsTable = new PdfPTable(2);
            invoiceDetailsTable.setWidthPercentage(100);
            invoiceDetailsTable.setWidths(new float[]{2, 4});
            invoiceDetailsTable.setSpacingBefore(10f);

            invoiceDetailsTable.addCell(getStyledCell("Invoice Number:", true));
            invoiceDetailsTable.addCell(getStyledCell(invoiceNumberInput.getText(), false));
            invoiceDetailsTable.addCell(getStyledCell("Customer Name:", true));
            invoiceDetailsTable.addCell(getStyledCell(customerInput.getText(), false));
            invoiceDetailsTable.addCell(getStyledCell("Date:", true));
            invoiceDetailsTable.addCell(getStyledCell(java.time.LocalDate.now().toString(), false));

            document.add(invoiceDetailsTable);
            document.add(new Paragraph("\n"));

            // Table Headers
            PdfPTable pdfTable = new PdfPTable(4);
            pdfTable.setWidthPercentage(100);
            pdfTable.setSpacingBefore(10f);
            pdfTable.setSpacingAfter(10f);
            pdfTable.setWidths(new float[]{3, 1, 2, 2});

            Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            BaseColor headerColor = new BaseColor(0, 102, 204);
            String[] headers = {"Item Name", "Quantity", "Price", "Subtotal"};
            for (String headerText : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(headerText, tableHeaderFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                pdfTable.addCell(cell);
            }

            // Table Data with Alternating Row Colors
            Font tableFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
            boolean alternate = false;
            for (Item item : itemList) {
                BaseColor rowColor = alternate ? new BaseColor(230, 230, 250) : BaseColor.WHITE;
                pdfTable.addCell(getStyledCell(item.getName(), tableFont, rowColor));
                pdfTable.addCell(getStyledCell(String.valueOf(item.getQuantity()), tableFont, rowColor));
                pdfTable.addCell(getStyledCell("₹" + item.getPrice(), tableFont, rowColor));
                pdfTable.addCell(getStyledCell("₹" + item.getSubtotal(), tableFont, rowColor));
                alternate = !alternate;
            }
            document.add(pdfTable);

            // Total Amount
            Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph totalAmount = new Paragraph("Total: ₹" + String.format("%.2f", itemList.stream().mapToDouble(Item::getSubtotal).sum()), totalFont);
            totalAmount.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalAmount);
            document.add(new Paragraph("\n"));

            document.add(new Chunk(new DottedLineSeparator()));

            document.add(new Paragraph("\nThank you for your business!", new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.DARK_GRAY)));

            document.close();
            showAlert("PDF Saved Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error generating PDF!");
        }
    }

    private PdfPCell getStyledCell(String text, boolean isBold) {
        Font font = new Font(Font.FontFamily.HELVETICA, 12, isBold ? Font.BOLD : Font.NORMAL);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell getStyledCell(String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        return cell;
    }



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Item Class (Separate File or Inner Class)
class Item {
    private final StringProperty name;
    private final IntegerProperty quantity;
    private final DoubleProperty price;
    private final DoubleProperty subtotal;

    public Item(String name, int quantity, double price, double subtotal) {
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.subtotal = new SimpleDoubleProperty(subtotal);
    }

    public String getName() { return name.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getPrice() { return price.get(); }
    public double getSubtotal() { return subtotal.get(); }

    public StringProperty nameProperty() { return name; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty subtotalProperty() { return subtotal; }
}
