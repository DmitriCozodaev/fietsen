package be.vdab.fietsen.domain;

import javax.persistence.*;
import java.util.UUID;

@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@Inheritance(strategy = InheritanceType.JOINED)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "cursussen")
//@DiscriminatorColumn(name = "soort")
public abstract class Cursus {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    // private long id;
    @Column(columnDefinition = "binary(16")
    private UUID id;
    private String naam;

//    public Cursus(String naam) {
//        this.naam = naam;
//    }

    public Cursus(String naam) {
        id = UUID.randomUUID();
        this.naam = naam;
    }

    protected Cursus(){}

//    public long getId() {
//        return id;
//    }


    public UUID getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }
}
