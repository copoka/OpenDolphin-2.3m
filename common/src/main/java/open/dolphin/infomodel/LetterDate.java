package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Entity
@Table(name = "d_letter_date")
public class LetterDate extends InfoModel {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(name = "c_value")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date value;

    @JsonIgnore
    //@JsonBackReference
    @ManyToOne
    @JoinColumn(name="module_id", nullable=false)
    private LetterModule module;

    public LetterDate() {
    }
    
    public LetterDate(String name, Date value) {
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
        if (!(object instanceof LetterDate)) {
            return false;
        }
        LetterDate other = (LetterDate) object;
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

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    public LetterModule getModule() {
        return module;
    }

    public void setModule(LetterModule module) {
        this.module = module;
    }
}
