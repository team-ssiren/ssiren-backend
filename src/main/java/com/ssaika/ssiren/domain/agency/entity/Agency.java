package com.ssaika.ssiren.domain.agency.entity;

import com.ssaika.ssiren.global.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "agencies")
public class Agency extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_name", nullable = false, length = 100)
    private String agencyName;

    @Column(nullable = false, length = 20)
    private String sido;

    @Column(nullable = false, length = 20)
    private String sigungu;

    @Column(nullable = false, length = 20)
    private String eupmyeondong;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 30)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_type_id", nullable = false)
    private AgencyType agencyType;
}
