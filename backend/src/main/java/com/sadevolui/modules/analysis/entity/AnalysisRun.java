package com.sadevolui.modules.analysis.entity;

import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.project.entity.Project;
import com.sadevolui.modules.user.entity.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "analysis_runs") @Getter @NoArgsConstructor
public class AnalysisRun {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requested_by", nullable = false) private UserAccount requestedBy;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id") private Project project;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "application_id") private ManagedApplication application;
    @Column(name = "method_version", nullable = false, length = 40) private String methodVersion;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    public AnalysisRun(UserAccount requestedBy, Project project, ManagedApplication application, String methodVersion) {
        this.requestedBy = requestedBy; this.project = project; this.application = application; this.methodVersion = methodVersion;
    }
}
