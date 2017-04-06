package edu.calpoly.apacheprojectdata.metrics;

import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

/**
 * Snapshot
 */
@Entity
@Data
public class Snapshot {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long id;

    @Column ZonedDateTime started;
    @Column ZonedDateTime finished;

    public Snapshot() {
        started = ZonedDateTime.now();
    }

    public void finish() {
        finished = ZonedDateTime.now();
    }
}
