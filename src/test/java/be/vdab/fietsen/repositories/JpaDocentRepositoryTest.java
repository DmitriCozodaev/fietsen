package be.vdab.fietsen.repositories;

import be.vdab.fietsen.domain.Docent;
import be.vdab.fietsen.domain.Geslacht;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import javax.persistence.EntityManager;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/insertDocent.sql")
@Import(JpaDocentRepository.class)
class JpaDocentRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {
    private final JpaDocentRepository repository;
    private static final String DOCENTEN = "docenten";
    private static final String DOCENTEN_BIJNAMEN = "docentenbijnamen";
    private Docent docent;
    private final EntityManager manager;

    public JpaDocentRepositoryTest(JpaDocentRepository repository, EntityManager manager) {
        this.repository = repository;
        this.manager = manager;
    }

    private long idVanTestMan() {
        return jdbcTemplate.queryForObject("select id from docenten where voornaam = 'testM'", Long.class);
    }

    private long idVanTestVrouw() {
        return jdbcTemplate.queryForObject(
                "select id from docenten where voornaam = 'testV'", Long.class);
    }

    @BeforeEach
    void beforeEach() {
        docent = new Docent("test", "test", BigDecimal.TEN, "test@test.be", Geslacht.MAN);
    }

    @Test
    void findById() {
        assertThat(repository.findById(idVanTestMan()).get().getVoornaam()).isEqualTo("testM");
    }

    @Test
    void findByOnbestaandeId() {
        assertThat(repository.findById(-1)).isNotPresent();
    }

    @Test
    void man() {
        assertThat(repository.findById(idVanTestMan()).get().getGeslacht()).isEqualTo(Geslacht.MAN);
    }

    @Test
    void vrouw() {
        assertThat(repository.findById(idVanTestVrouw()).get().getGeslacht()).isEqualTo(Geslacht.VROUW);
    }

    @Test
    void create() {
        repository.create(docent);
        assertThat(docent.getId()).isPositive();
        assertThat(countRowsInTableWhere(DOCENTEN, "id=" + docent.getId())).isOne();
    }

    @Test
    void delete() {
        var id = idVanTestMan();
        repository.delete(id);
        manager.flush();
        assertThat(countRowsInTableWhere(DOCENTEN, "id =" + id)).isZero();
    }

    @Test
    void findAll() {
        assertThat(repository.findAll()).hasSize(countRowsInTable(DOCENTEN)).extracting(Docent::getWedde).isSorted();
    }

    @Test
    void findByWeddeBetween() {
        var duizend = BigDecimal.valueOf(1_000);
        var tweeduizend = BigDecimal.valueOf(2_000);
        var docenten = repository.findByWeddeBetween(duizend, tweeduizend);
        assertThat(docenten).hasSize(countRowsInTableWhere(DOCENTEN, "wedde between 1000 and 2000")).allSatisfy(
                docent -> assertThat(docent.getWedde()).isBetween(duizend, tweeduizend));
    }

    @Test
    void findEmailAdressen() {
        assertThat(repository.findEmailAdressen()).hasSize(countRowsInTable(DOCENTEN))
                .allSatisfy(emailAdres -> assertThat(emailAdres).contains("@"));
    }

    @Test
    void findIdsEnEmailAdressen() {
        assertThat(repository.findIdsEnEmailAdressen()).hasSize(countRowsInTable(DOCENTEN));
    }

    @Test
    void findGrootsteWedde() {
        assertThat(repository.findGrootsteWedde()).isEqualByComparingTo(jdbcTemplate.queryForObject("select max(wedde) from docenten", BigDecimal.class));
    }

    @Test
    void findAantalDocentenPerWedde() {
        var duizend = BigDecimal.valueOf(1_000);
        assertThat(repository.findAantalDocentenPerWedde()).hasSize(jdbcTemplate.queryForObject("select count(distinct wedde) from docenten", Integer.class)).
                filteredOn(aantalPerWedde -> aantalPerWedde.getWedde().compareTo(duizend) == 0).hasSize(1).element(0).
                satisfies(aantalPerWedde -> assertThat(aantalPerWedde.getAantal()).isEqualTo(countRowsInTableWhere(DOCENTEN, "wedde = 1000")));
    }

    @Test
    void algemeneOpslag() {
        assertThat(repository.algemeneOpslag(BigDecimal.TEN)).isEqualTo(countRowsInTable(DOCENTEN));
        assertThat(countRowsInTableWhere(DOCENTEN, "wedde = 1100 and id =" + idVanTestMan())).isOne();
    }

    @Test
    void bijnamenLezen() {
        assertThat(repository.findById(idVanTestMan()).get().getBijnamen()).containsOnly("test");
    }

    @Test
    void bijnaamToevoegen() {
        repository.create(docent);
        docent.addBijnaam("test");
        manager.flush();
        assertThat(countRowsInTableWhere(DOCENTEN_BIJNAMEN, "bijnaam = 'test' and docentId =" + docent.getId())).isOne();
    }
}