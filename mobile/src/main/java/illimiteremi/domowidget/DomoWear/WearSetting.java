package illimiteremi.domowidget.DomoWear;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.DEFAULT_WEAR_TIMEOUT;

/**
 * Created by rcouturi on  24/11/2016.
 */
public class WearSetting {

    static final String TAG       = "[DOMO_GLOBAL_SETTING]";

    private Integer id;                   // Identifiant SQL
    private Integer boxId;                // Identifiant de la box
    private Integer wearTimeOut;          // TimeOut avant envoi

    public WearSetting() {
        this.boxId = 0;
        this.wearTimeOut = DEFAULT_WEAR_TIMEOUT;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBoxId() {
        return boxId == null ? 0 : boxId;
    }

    public void setBoxId(Integer boxId) {
        this.boxId = boxId;
    }

    public Integer getWearTimeOutTimeOut() {
        return wearTimeOut = (wearTimeOut == null) ? DEFAULT_WEAR_TIMEOUT : wearTimeOut;
    }

    public void setWearTimeOutTimeOut(Integer domoTimeOut) {
        this.wearTimeOut = domoTimeOut;
    }

}
