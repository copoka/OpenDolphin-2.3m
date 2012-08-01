package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author modified by masuda, Masuda Naika
 */
@Entity
@Table(name = "d_letter_item")
public class LetterItem extends InfoModel {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(name = "c_value")
    private String value;

    @JsonIgnore // bi-directional references
    @ManyToOne
    @JoinColumn(name="module_id", nullable=false)
    private LetterModule module;

    public LetterItem() {
    }
    
    public LetterItem(String name, String value) {
        this();
        this.name = name;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LetterItem)) {
            return false;
        }
        LetterItem other = (LetterItem) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "open.dolphin.infomodel.DocumentItem[id=" + getId() + "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LetterModule getModule() {
        return module;
    }

    public void setModule(LetterModule module) {
        this.module = module;
    }

}
