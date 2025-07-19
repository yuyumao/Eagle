package com.eagle.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "account_sequence")
@Getter
public class AccountSequence {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acc_seq_gen")
        @SequenceGenerator(name = "acc_seq_gen", initialValue = 1, sequenceName = "acc_seq", allocationSize = 1)
        private Long id;
}
