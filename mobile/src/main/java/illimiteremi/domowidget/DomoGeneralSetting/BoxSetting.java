package illimiteremi.domowidget.DomoGeneralSetting;

/**
 * Created by rcouturi on  24/11/2016.
 */
public class BoxSetting {

    static final String TAG       = "[DOMO_GLOBAL_SETTING]";

    private Integer boxId         = 0;           // Identifiant de la box
    private String  boxName       = "";          // Nom de la box
    private String  boxKey        = "";          // Clée de la box
    private String  boxUrlInterne = "";          // URL Interne
    private String  boxUrlExterne = "";          // URL Externe

    public BoxSetting() {
    }

    public Integer getBoxId() {
        return boxId == null ? 0 : boxId;
    }

    public void setBoxId(Integer boxId) {
        this.boxId = boxId;
    }

    public String getBoxKey() {
        return boxKey;
    }

    public void setBoxKey(String boxKey) {
        this.boxKey = boxKey;
    }

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public String getBoxUrlExterne() {
        return boxUrlExterne;
    }

    public void setBoxUrlExterne(String boxUrlExterne) {
        this.boxUrlExterne = boxUrlExterne;
    }

    public String getBoxUrlInterne() {
        return boxUrlInterne;
    }

    public void setBoxUrlInterne(String boxUrlInterne) {
        this.boxUrlInterne = boxUrlInterne;
    }
}
