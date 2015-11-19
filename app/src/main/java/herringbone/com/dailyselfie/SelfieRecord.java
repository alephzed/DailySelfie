package herringbone.com.dailyselfie;

public class SelfieRecord {
    private String mRecordLocation;
    private String dateModified;

    public String getmRecordLocation() {
        return mRecordLocation;
    }

    public void setmRecordLocation(String mRecordLocation) {
        this.mRecordLocation = mRecordLocation;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public boolean equals(Object other) {
        SelfieRecord otherSelfie = (SelfieRecord)other;
        if (otherSelfie.getmRecordLocation().equals(this.getmRecordLocation())) {
            return true;
        }
        return false;
    }
}
