package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Stamp 及び Module の属性を保持するクラス。
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Embeddable
public class ModuleInfoBean extends InfoModel implements Comparable {
    
    /** Module 名: StampTree、 オーダ履歴当に表示する名前 */
    @Column(nullable=false)
    private String name;
    
    /** SOA または P の役割 */
    @Column(nullable=false)
    private String role;
    
    /** ドキュメントに出現する順番 */
    @Column(nullable=false)
    private int stampNumber;
    
    /** 情報の実体名 */
    @Column(nullable=false)
    private String entity;
    
    /** 編集可能かどうか */
    @JsonIgnore
    @Transient
    private boolean editable = true;
    
    /** ASP 提供か */
    @JsonIgnore
    @Transient
    private boolean asp;
    
    /** DB 保存されている場合、そのキー */
    @JsonIgnore
    @Transient
    private String stampId;
    
    /** Memo の内容説明 */
    @JsonIgnore
    @Transient
    private String memo;
    
    /** 折り返し表示するかどうか */
    @JsonIgnore
    @Transient
    private boolean turnIn;
    
    /**
     * ModuleInfoオブジェクトを生成する。
     */
    public ModuleInfoBean() {
    }
    
    /**
     * スタンプ名を返す。
     * @return スタンプ名
     */
    public String getStampName() {
        return name;
    }
    
    /**
     * スタンプ名を設定する。
     * @param name スタンプ名
     */
    public void setStampName(String name) {
        this.name = name;
    }
    
    /**
     * スタンプのロールを返す。
     * @return スタンプのロール
     */
    public String getStampRole() {
        return role;
    }
    
    /**
     * スタンプのロールを設定する。
     * @param role スタンプのロール
     */
    public void setStampRole(String role) {
        this.role = role;
    }
    
    /**
     * スタンプのエンティティ名を返す。
     * @return エンティティ名
     */
    public String getEntity() {
        return entity;
    }
    
    /**
     * スタンプのエンティティ名を設定する。
     * @param entity エンティティ名
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }
    
    /**
     * シリアライズされているかどうかを返す。
     * @return シリアライズされている時 true
     */
    public boolean isSerialized() {
        return stampId != null;
    }
    
    /**
     * ASP提供かどうかを返す。
     * @return ASP提供の時 true
     */
    public boolean isASP() {
        return asp;
    }
    
    /**
     * ASP提供を設定する。
     * @param asp ASP提供の真偽値
     */
    public void setASP(boolean asp) {
        this.asp = asp;
    }
    
    /**
     * Databseに保存されている時の PK を変えす。
     * @return Primary Key
     */
    public String getStampId() {
        return stampId;
    }
    
    /**
     * Databseに保存される時の PK を設定する。
     * @param id Primary Key
     */
    public void setStampId(String id) {
        stampId = id;
    }
    
    /**
     * スタンプのメモを返す。
     * @return スタンプのメモ
     */
    public String getStampMemo() {
        return memo;
    }
    
    /**
     * スタンプのメモを設定する。
     * @param memo スタンプのメモ
     */
    public void setStampMemo(String memo) {
        this.memo = memo;
    }
    
    /**
     * このスタンプが編集可能かどうかを設定する。
     * @param editable 編集可能かどうかの真偽値
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    /**
     * このスタンプが編集可能かどうかを返す。
     * @return 編集可能の時 true
     */
    public boolean isEditable() {
        return editable;
    }
    
    public void setTurnIn(boolean turnIn) {
        this.turnIn = turnIn;
    }
    
    public boolean isTurnIn() {
        return turnIn;
    }
    
    /**
     * 文字列表現を返す。
     * @return スタンプ名
     */
    @Override
    public String toString() {
        // 病名でエイリアスがあればそれを返す
        if (this.entity.equals(ENTITY_DIAGNOSIS)) {
            String alias =  ModelUtils.getDiagnosisAlias(name);
            return alias != null ? alias : name;
        }
        return name;
    }
    
    /**
     * ドキュメント内の出現番号を設定する。
     * @param stampNumber　出現する番号
     */
    public void setStampNumber(int stampNumber) {
        this.stampNumber = stampNumber;
    }
    
    /**
     * ドキュメント内の出現番号を返す。
     * @return ドキュメント内の出現番号
     */
    public int getStampNumber() {
        return stampNumber;
    }
    
    /**
     * スタンプ番号で比較する。
     * @return 比較値
     */
    @Override
    public int compareTo(Object other) {
        if (other != null && getClass() == other.getClass()) {
            int result = getStampNumber() - ((ModuleInfoBean)other).getStampNumber();
            return result;
        }
        return -1;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ModuleInfoBean ret = new ModuleInfoBean();
        ret.setASP(this.isASP());
        ret.setEditable(this.isEditable());
        ret.setEntity(this.getEntity());
        ret.setStampId(this.getStampId());
        ret.setStampMemo(this.getStampMemo());
        ret.setStampName(this.getStampName());
        ret.setStampNumber(this.getStampNumber());
        ret.setStampRole(this.getStampRole());
        ret.setTurnIn(this.isTurnIn());
        return ret;
    }
}
