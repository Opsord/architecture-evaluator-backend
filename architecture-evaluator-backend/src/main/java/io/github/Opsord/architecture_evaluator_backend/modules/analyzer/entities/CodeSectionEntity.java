package io.github.Opsord.architecture_evaluator_backend.modules.analyzer.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CODE_SECTION")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeSectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String code;

    private CodeTypes type;


    /* Needed only for the equals and hashCode methods
    /// The getId is created to be used in the equals and hashCode methods
    /// This is because lombok generates the getId method correctly
    public Long getId() {
        return id;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CodeSectionEntity that = (CodeSectionEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

     */
}