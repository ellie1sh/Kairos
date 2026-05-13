package jdbc.demo.model;

public class ItemRecord {
    private final String itemId;
    private final String itemName;
    private final String itemType;
    private final String description;
    private final String model;
    private final String conditionStatus;
    private final String availabilityStatus;
    private final String dateAcquired;

    public ItemRecord(String itemId, String itemName, String itemType,
                      String description, String model,
                      String conditionStatus, String availabilityStatus,
                      String dateAcquired) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemType = itemType;
        this.description = description;
        this.model = model;
        this.conditionStatus = conditionStatus;
        this.availabilityStatus = availabilityStatus;
        this.dateAcquired = dateAcquired;
    }

    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getItemType() { return itemType; }
    public String getDescription() { return description; }
    public String getModel() { return model; }
    public String getConditionStatus() { return conditionStatus; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public String getDateAcquired() { return dateAcquired; }

    @Override
    public String toString() {
        return String.format(
                "ItemID: %s | Name: %s | Type: %s | Condition: %s | Availability: %s | Acquired: %s",
                itemId, itemName, itemType, conditionStatus, availabilityStatus, dateAcquired
        );
    }
}
