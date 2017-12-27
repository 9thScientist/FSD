package rmi;

public abstract class Exportable {
    private int exportId = -1;

    public boolean isExported() {
        return exportId >= 0;
    }

    public void setExportId(int id) {
        this.exportId = id;
    }

    public int getExportId() {
        return exportId;
    }
}
