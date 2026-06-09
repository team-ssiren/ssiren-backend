package com.ssaika.ssiren.domain.report.entity;

import com.ssaika.ssiren.global.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "report_category_merge_rules")
public class ReportCategoryMergeRule extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ReportCategory category;

    @Column(name = "link_radius_meters", nullable = false)
    private Integer linkRadiusMeters;

    @Column(name = "max_group_diameter_meters", nullable = false)
    private Integer maxGroupDiameterMeters;

    @Column(name = "min_embedding_similarity", nullable = false, precision = 4, scale = 3)
    private BigDecimal minEmbeddingSimilarity;

    @Column(name = "auto_merge_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal autoMergeThreshold;

    public static ReportCategoryMergeRule create(
        ReportCategory category,
        Integer linkRadiusMeters,
        Integer maxGroupDiameterMeters,
        BigDecimal minEmbeddingSimilarity,
        BigDecimal autoMergeThreshold) {
        return ReportCategoryMergeRule.builder()
            .category(category)
            .linkRadiusMeters(linkRadiusMeters)
            .maxGroupDiameterMeters(maxGroupDiameterMeters)
            .minEmbeddingSimilarity(minEmbeddingSimilarity)
            .autoMergeThreshold(autoMergeThreshold)
            .build();
    }
}
